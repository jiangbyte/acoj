package auth

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"

	"hei-gin/config"
	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/core/enums"
)

type ClientAuthToolImpl struct {
	secret        string
	algorithm     string
	expire        int
	tokenName     string
	loginType     enums.LoginType
	tokenPrefix   string
	sessionPrefix string
	disablePrefix string
}

var ClientAuthTool *ClientAuthToolImpl

func InitClientAuthTool(cfg config.JWTConfig) {
	ClientAuthTool = &ClientAuthToolImpl{
		secret:        cfg.SecretKey,
		algorithm:     cfg.Algorithm,
		expire:        cfg.ExpireSeconds,
		tokenName:     cfg.TokenName,
		loginType:     enums.LoginTypeConsumer,
		tokenPrefix:   constants.TokenPrefixConsumer,
		sessionPrefix: constants.SessionPrefixConsumer,
		disablePrefix: constants.DisableKeyConsumer,
	}
}

func (a *ClientAuthToolImpl) GetTokenName() string {
	return a.tokenName
}

func (a *ClientAuthToolImpl) GetLoginType() string {
	return string(a.loginType)
}

func (a *ClientAuthToolImpl) GetTokenValue(c *gin.Context) string {
	return c.GetHeader(a.tokenName)
}

func (a *ClientAuthToolImpl) IsLogin(c *gin.Context) bool {
	return a.GetLoginID(c) != ""
}

func (a *ClientAuthToolImpl) GetLoginID(c *gin.Context) string {
	token := a.GetTokenValue(c)
	if token == "" {
		return ""
	}
	ctx := context.Background()
	redisKey := a.tokenPrefix + token
	data, err := db.Redis.Get(ctx, redisKey).Result()
	if err != nil {
		return ""
	}
	var info map[string]interface{}
	if json.Unmarshal([]byte(data), &info) != nil {
		return ""
	}
	if uid, ok := info["user_id"].(string); ok {
		return uid
	}
	return ""
}

func (a *ClientAuthToolImpl) Login(c *gin.Context, id string, extra map[string]interface{}) (string, error) {
	now := time.Now()
	claims := jwt.MapClaims{
		"sub":  id,
		"type": a.loginType,
		"exp":  now.Add(time.Duration(a.expire) * time.Second).Unix(),
		"iat":  now.Unix(),
	}
	for k, v := range extra {
		claims[k] = v
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenStr, err := token.SignedString([]byte(a.secret))
	if err != nil {
		return "", err
	}

	ctx := context.Background()
	tokenData := map[string]interface{}{
		"user_id":    id,
		"type":       string(a.loginType),
		"created_at": now.Format(time.RFC3339),
		"extra":      extra,
	}
	data, _ := json.Marshal(tokenData)
	redisKey := a.tokenPrefix + tokenStr

	err = db.Redis.Set(ctx, redisKey, string(data), time.Duration(a.expire)*time.Second).Err()
	if err != nil {
		return "", fmt.Errorf("redis set failed: %w", err)
	}

	sessionKey := a.sessionPrefix + id
	db.Redis.SAdd(ctx, sessionKey, tokenStr)
	db.Redis.Expire(ctx, sessionKey, time.Duration(a.expire)*time.Second)

	return tokenStr, nil
}

func (a *ClientAuthToolImpl) Logout(c *gin.Context) {
	token := a.GetTokenValue(c)
	if token == "" {
		return
	}

	ctx := context.Background()
	redisKey := a.tokenPrefix + token
	data, err := db.Redis.Get(ctx, redisKey).Result()
	if err == nil {
		var tokenData map[string]interface{}
		if json.Unmarshal([]byte(data), &tokenData) == nil {
			if uid, ok := tokenData["user_id"].(string); ok {
				sessionKey := a.sessionPrefix + uid
				db.Redis.SRem(ctx, sessionKey, token)
			}
		}
	}
	db.Redis.Del(ctx, redisKey)
}

func (a *ClientAuthToolImpl) Kickout(loginID string) {
	ctx := context.Background()
	sessionKey := a.sessionPrefix + loginID
	tokens, _ := db.Redis.SMembers(ctx, sessionKey).Result()
	for _, t := range tokens {
		db.Redis.Del(ctx, a.tokenPrefix+t)
	}
	db.Redis.Del(ctx, sessionKey)
}

func (a *ClientAuthToolImpl) GetLoginIdByToken(token string) string {
	if token == "" {
		return ""
	}
	ctx := context.Background()
	redisKey := a.tokenPrefix + token
	data, err := db.Redis.Get(ctx, redisKey).Result()
	if err != nil {
		return ""
	}
	var info map[string]interface{}
	if json.Unmarshal([]byte(data), &info) != nil {
		return ""
	}
	if uid, ok := info["user_id"].(string); ok {
		return uid
	}
	return ""
}

func (a *ClientAuthToolImpl) GetTokenValueByLoginId(loginID string) string {
	ctx := context.Background()
	sessionKey := a.sessionPrefix + loginID
	tokens, _ := db.Redis.SMembers(ctx, sessionKey).Result()
	if len(tokens) > 0 {
		return tokens[0]
	}
	return ""
}

func (a *ClientAuthToolImpl) GetTokenValuesByLoginID(loginID string) []string {
	ctx := context.Background()
	sessionKey := a.sessionPrefix + loginID
	tokens, _ := db.Redis.SMembers(ctx, sessionKey).Result()
	return tokens
}

func (a *ClientAuthToolImpl) GetExtra(c *gin.Context, key string) interface{} {
	info := a.GetTokenInfo(c)
	if info != nil {
		if extra, ok := info["extra"].(map[string]interface{}); ok {
			return extra[key]
		}
	}
	return nil
}

func (a *ClientAuthToolImpl) GetTokenInfo(c *gin.Context) map[string]interface{} {
	token := a.GetTokenValue(c)
	if token == "" {
		return nil
	}
	ctx := context.Background()
	redisKey := a.tokenPrefix + token
	data, err := db.Redis.Get(ctx, redisKey).Result()
	if err != nil {
		return nil
	}
	var result map[string]interface{}
	if json.Unmarshal([]byte(data), &result) != nil {
		return nil
	}
	return result
}

func (a *ClientAuthToolImpl) RenewTimeout(c *gin.Context, timeout int) {
	token := a.GetTokenValue(c)
	if token == "" {
		return
	}
	if timeout <= 0 {
		timeout = a.expire
	}
	ctx := context.Background()
	redisKey := a.tokenPrefix + token
	db.Redis.Expire(ctx, redisKey, time.Duration(timeout)*time.Second)

	loginID := a.GetLoginID(c)
	if loginID != "" {
		sessionKey := a.sessionPrefix + loginID
		db.Redis.Expire(ctx, sessionKey, time.Duration(timeout)*time.Second)
	}
}

func (a *ClientAuthToolImpl) Disable(loginID string, seconds int) {
	ctx := context.Background()
	key := a.disablePrefix + loginID
	db.Redis.SetEx(ctx, key, "1", time.Duration(seconds)*time.Second)
}

func (a *ClientAuthToolImpl) IsDisable(loginID string) bool {
	ctx := context.Background()
	key := a.disablePrefix + loginID
	n, _ := db.Redis.Exists(ctx, key).Result()
	return n > 0
}

func (a *ClientAuthToolImpl) CheckDisable(loginID string) error {
	if a.IsDisable(loginID) {
		return fmt.Errorf("account is disabled")
	}
	return nil
}

func (a *ClientAuthToolImpl) GetDisableTime(loginID string) int {
	ctx := context.Background()
	key := a.disablePrefix + loginID
	ttl, _ := db.Redis.TTL(ctx, key).Result()
	return int(ttl.Seconds())
}

func (a *ClientAuthToolImpl) UntieDisable(loginID string) {
	ctx := context.Background()
	key := a.disablePrefix + loginID
	db.Redis.Del(ctx, key)
}

func (a *ClientAuthToolImpl) KickoutToken(loginID, token string) {
	ctx := context.Background()
	db.Redis.Del(ctx, a.tokenPrefix+token)
	sessionKey := a.sessionPrefix + loginID
	db.Redis.SRem(ctx, sessionKey, token)
}

func (a *ClientAuthToolImpl) GetTokenTimeout(c *gin.Context) int {
	token := a.GetTokenValue(c)
	if token == "" {
		return 0
	}
	ctx := context.Background()
	ttl, _ := db.Redis.TTL(ctx, a.tokenPrefix+token).Result()
	return int(ttl.Seconds())
}

func (a *ClientAuthToolImpl) GetSessionTimeout(c *gin.Context) int {
	loginID := a.GetLoginID(c)
	if loginID == "" {
		return 0
	}
	ctx := context.Background()
	ttl, _ := db.Redis.TTL(ctx, a.sessionPrefix+loginID).Result()
	return int(ttl.Seconds())
}

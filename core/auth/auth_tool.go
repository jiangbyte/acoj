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

type AuthToolImpl struct {
	secret        string
	algorithm     string
	expire        int
	tokenName     string
	loginType     enums.LoginType
	tokenPrefix   string
	sessionPrefix string
	disablePrefix string
}

var AuthTool *AuthToolImpl

func InitAuthTool(cfg config.JWTConfig) {
	AuthTool = &AuthToolImpl{
		secret:        cfg.SecretKey,
		algorithm:     cfg.Algorithm,
		expire:        cfg.ExpireSeconds,
		tokenName:     cfg.TokenName,
		loginType:     enums.LoginTypeBusiness,
		tokenPrefix:   constants.TokenPrefixBusiness,
		sessionPrefix: constants.SessionPrefixBusiness,
		disablePrefix: constants.DisableKeyBusiness,
	}
}

func (a *AuthToolImpl) GetTokenName() string {
	return a.tokenName
}

func (a *AuthToolImpl) GetLoginType() string {
	return string(a.loginType)
}

func (a *AuthToolImpl) GetTokenValue(c *gin.Context) string {
	return c.GetHeader(a.tokenName)
}

func (a *AuthToolImpl) IsLogin(c *gin.Context) bool {
	loginID := a.GetLoginID(c)
	return loginID != ""
}

func (a *AuthToolImpl) GetLoginID(c *gin.Context) string {
	token := a.GetTokenValue(c)
	if token == "" {
		return ""
	}
	payload, _ := a.decodeToken(token)
	if payload != nil {
		if sub, ok := payload["sub"].(string); ok {
			return sub
		}
	}
	return ""
}

func (a *AuthToolImpl) Login(c *gin.Context, id string, extra map[string]interface{}) (string, error) {
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

	// Store in Redis
	ctx := context.Background()
	tokenData := map[string]interface{}{
		"user_id":    id,
		"type":       a.loginType,
		"created_at": now.Format(time.RFC3339),
		"extra":      extra,
	}
	data, _ := json.Marshal(tokenData)
	redisKey := a.tokenPrefix + tokenStr

	err = db.Redis.Set(ctx, redisKey, string(data), time.Duration(a.expire)*time.Second).Err()
	if err != nil {
		return "", fmt.Errorf("redis set failed: %w", err)
	}

	// Add to user's session set
	sessionKey := a.sessionPrefix + id
	db.Redis.SAdd(ctx, sessionKey, tokenStr)
	db.Redis.Expire(ctx, sessionKey, time.Duration(a.expire)*time.Second)

	return tokenStr, nil
}

func (a *AuthToolImpl) Logout(c *gin.Context) {
	token := a.GetTokenValue(c)
	if token == "" {
		return
	}

	ctx := context.Background()
	redisKey := a.tokenPrefix + token

	// Get token data to find user_id
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

func (a *AuthToolImpl) Kickout(loginID string) {
	ctx := context.Background()
	sessionKey := a.sessionPrefix + loginID
	tokens, _ := db.Redis.SMembers(ctx, sessionKey).Result()
	for _, t := range tokens {
		db.Redis.Del(ctx, a.tokenPrefix+t)
	}
	db.Redis.Del(ctx, sessionKey)
}

func (a *AuthToolImpl) GetTokenInfo(c *gin.Context) map[string]interface{} {
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

func (a *AuthToolImpl) GetExtra(c *gin.Context, key string) interface{} {
	info := a.GetTokenInfo(c)
	if info != nil {
		if extra, ok := info["extra"].(map[string]interface{}); ok {
			return extra[key]
		}
	}
	return nil
}

func (a *AuthToolImpl) Disable(loginID string, seconds int) {
	ctx := context.Background()
	key := a.disablePrefix + loginID
	db.Redis.SetEx(ctx, key, "1", time.Duration(seconds)*time.Second)
}

func (a *AuthToolImpl) IsDisable(loginID string) bool {
	ctx := context.Background()
	key := a.disablePrefix + loginID
	n, _ := db.Redis.Exists(ctx, key).Result()
	return n > 0
}

func (a *AuthToolImpl) RenewTimeout(c *gin.Context, timeout int) {
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

func (a *AuthToolImpl) decodeToken(token string) (jwt.MapClaims, error) {
	ctx := context.Background()
	redisKey := a.tokenPrefix + token

	// Check Redis first
	n, err := db.Redis.Exists(ctx, redisKey).Result()
	if err != nil || n == 0 {
		return nil, fmt.Errorf("token not found in redis")
	}

	parsedToken, err := jwt.Parse(token, func(t *jwt.Token) (interface{}, error) {
		return []byte(a.secret), nil
	})
	if err != nil {
		// Token expired or invalid — clean up Redis
		db.Redis.Del(ctx, redisKey)
		return nil, err
	}

	if claims, ok := parsedToken.Claims.(jwt.MapClaims); ok && parsedToken.Valid {
		return claims, nil
	}
	return nil, fmt.Errorf("invalid token")
}

func (a *AuthToolImpl) GetTokenValuesByLoginID(loginID string) []string {
	ctx := context.Background()
	sessionKey := a.sessionPrefix + loginID
	tokens, _ := db.Redis.SMembers(ctx, sessionKey).Result()
	return tokens
}

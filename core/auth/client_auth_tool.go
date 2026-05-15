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
	payload, _ := a.decodeToken(token)
	if payload != nil {
		if sub, ok := payload["sub"].(string); ok {
			return sub
		}
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

func (a *ClientAuthToolImpl) decodeToken(token string) (jwt.MapClaims, error) {
	ctx := context.Background()
	redisKey := a.tokenPrefix + token

	n, err := db.Redis.Exists(ctx, redisKey).Result()
	if err != nil || n == 0 {
		return nil, fmt.Errorf("token not found in redis")
	}

	parsedToken, err := jwt.Parse(token, func(t *jwt.Token) (interface{}, error) {
		return []byte(a.secret), nil
	})
	if err != nil {
		db.Redis.Del(ctx, redisKey)
		return nil, err
	}

	if claims, ok := parsedToken.Claims.(jwt.MapClaims); ok && parsedToken.Valid {
		return claims, nil
	}
	return nil, fmt.Errorf("invalid token")
}

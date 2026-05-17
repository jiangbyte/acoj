package auth

import (
	"context"
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"errors"
	"time"

	"hei-gin/config"
	"hei-gin/core/constants"
	"hei-gin/core/db"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/redis/go-redis/v9"
)

const loginTypeConsumer = "CONSUMER"

// HeiClientAuthTool provides authentication utilities for consumer (client) users.
type HeiClientAuthTool struct {
	expire    int
	tokenName string
	secret    string
	algorithm string
}

// NewHeiClientAuthTool creates a new HeiClientAuthTool with default settings from the global config.
func NewHeiClientAuthTool() *HeiClientAuthTool {
	t := &HeiClientAuthTool{}
	t.ensureConfig()
	return t
}

// ensureConfig initializes default values from the global config if not already set.
func (h *HeiClientAuthTool) ensureConfig() {
	if h.secret != "" {
		return
	}
	if config.C == nil {
		return
	}
	h.expire = config.C.JWT.ExpireSeconds
	h.tokenName = config.C.JWT.TokenName
	h.secret = config.C.JWT.SecretKey
	h.algorithm = config.C.JWT.Algorithm
}

// tokenURLSafe generates n random bytes and returns them as a URL-safe base64 string without padding.
func (h *HeiClientAuthTool) tokenURLSafe(n int) string {
	b := make([]byte, n)
	_, _ = rand.Read(b)
	return base64.RawURLEncoding.EncodeToString(b)
}

// getRedis returns the shared Redis client.
func (h *HeiClientAuthTool) getRedis() *redis.Client {
	return db.Redis
}

// getTokenKey returns the Redis key for storing token data.
func (h *HeiClientAuthTool) getTokenKey(token string) string {
	return constants.TOKEN_PREFIX_CONSUMER + token
}

// getSessionKey returns the Redis key for storing a user's session tokens.
func (h *HeiClientAuthTool) getSessionKey(userID string) string {
	return constants.SESSION_PREFIX_CONSUMER + userID
}

// getDisableKey returns the Redis key for storing the disable status of a login ID.
func (h *HeiClientAuthTool) getDisableKey(loginID string) string {
	return constants.DISABLE_KEY_CONSUMER + loginID
}

// Init initializes the auth tool with custom expire and token name.
// If expire <= 0 or tokenName is empty, values from the global config are used.
func (h *HeiClientAuthTool) Init(expire int, tokenName string) {
	h.ensureConfig()
	if expire > 0 {
		h.expire = expire
	}
	if tokenName != "" {
		h.tokenName = tokenName
	}
}

// GetLoginType returns the login type identifier for consumer users.
func (h *HeiClientAuthTool) GetLoginType() string {
	h.ensureConfig()
	return loginTypeConsumer
}

// GetTokenName returns the HTTP header name used to carry the token.
func (h *HeiClientAuthTool) GetTokenName() string {
	h.ensureConfig()
	return h.tokenName
}

// GetTokenValue extracts the token from the request header.
func (h *HeiClientAuthTool) GetTokenValue(c *gin.Context) string {
	h.ensureConfig()
	if c == nil {
		return ""
	}
	return c.GetHeader(h.tokenName)
}

// Login authenticates a user by user ID, stores token data in Redis, and returns the signed JWT token.
func (h *HeiClientAuthTool) Login(c *gin.Context, id string, extra map[string]any) (string, error) {
	h.ensureConfig()

	now := time.Now()
	jti := h.tokenURLSafe(32)

	// Build and sign JWT with jti and iat claims
	claims := jwt.MapClaims{
		"jti": jti,
		"iat": jwt.NewNumericDate(now),
	}
	token := jwt.NewWithClaims(jwt.GetSigningMethod(h.algorithm), claims)
	signedToken, err := token.SignedString([]byte(h.secret))
	if err != nil {
		return "", err
	}

	// Build token data to store in Redis
	tokenData := map[string]any{
		"user_id":    id,
		"type":       loginTypeConsumer,
		"created_at": now.Format(time.RFC3339),
		"extra":      extra,
	}
	if extra == nil {
		tokenData["extra"] = map[string]any{}
	}

	tokenDataJSON, err := json.Marshal(tokenData)
	if err != nil {
		return "", err
	}

	redisClient := h.getRedis()
	ctx := context.Background()

	// Store token data in Redis with expiration
	err = redisClient.SetEx(ctx, h.getTokenKey(signedToken), tokenDataJSON, time.Duration(h.expire)*time.Second).Err()
	if err != nil {
		return "", err
	}

	// Add token to user's session set and refresh its expiration
	sessionKey := h.getSessionKey(id)
	err = redisClient.SAdd(ctx, sessionKey, signedToken).Err()
	if err != nil {
		return "", err
	}
	err = redisClient.Expire(ctx, sessionKey, time.Duration(h.expire)*time.Second).Err()
	if err != nil {
		return "", err
	}

	return signedToken, nil
}

// Logout invalidates the current session. If loginID is provided, it kicks out all sessions for that user.
func (h *HeiClientAuthTool) Logout(c *gin.Context, loginID ...string) {
	h.ensureConfig()

	if len(loginID) > 0 {
		h.Kickout(loginID[0])
		return
	}

	token := h.GetTokenValue(c)
	if token == "" {
		return
	}

	data := h.getTokenData(token)
	if data != nil {
		userID, _ := data["user_id"].(string)
		if userID != "" {
			redisClient := h.getRedis()
			ctx := context.Background()
			sessionKey := h.getSessionKey(userID)
			_ = redisClient.SRem(ctx, sessionKey, token).Err()
		}
	}

	redisClient := h.getRedis()
	ctx := context.Background()
	tokenKey := h.getTokenKey(token)
	_ = redisClient.Del(ctx, tokenKey).Err()
}

// Kickout deletes all tokens and session data for the given login ID.
func (h *HeiClientAuthTool) Kickout(loginID string) {
	h.ensureConfig()

	redisClient := h.getRedis()
	ctx := context.Background()
	sessionKey := h.getSessionKey(loginID)

	tokens, err := redisClient.SMembers(ctx, sessionKey).Result()
	if err != nil {
		return
	}

	for _, token := range tokens {
		tokenKey := h.getTokenKey(token)
		_ = redisClient.Del(ctx, tokenKey).Err()
	}

	_ = redisClient.Del(ctx, sessionKey).Err()
}

// KickoutToken removes a specific token from the user's session set and deletes its data.
func (h *HeiClientAuthTool) KickoutToken(loginID, token string) {
	h.ensureConfig()

	redisClient := h.getRedis()
	ctx := context.Background()
	sessionKey := h.getSessionKey(loginID)
	tokenKey := h.getTokenKey(token)

	_ = redisClient.SRem(ctx, sessionKey, token).Err()
	_ = redisClient.Del(ctx, tokenKey).Err()
}

// IsLogin checks whether the current request carries a valid token.
func (h *HeiClientAuthTool) IsLogin(c *gin.Context) bool {
	loginID := h.GetLoginIDDefaultNull(c)
	return loginID != ""
}

// CheckLogin returns an error if the current request is not authenticated.
func (h *HeiClientAuthTool) CheckLogin(c *gin.Context) error {
	if !h.IsLogin(c) {
		return errors.New("未授权/未登录")
	}
	return nil
}

// GetLoginID returns the login ID extracted from the current request's token.
func (h *HeiClientAuthTool) GetLoginID(c *gin.Context) string {
	return h.GetLoginIDDefaultNull(c)
}

// GetLoginIDDefaultNull returns the login ID from the current request's token, or an empty string if not logged in.
func (h *HeiClientAuthTool) GetLoginIDDefaultNull(c *gin.Context) string {
	token := h.GetTokenValue(c)
	if token == "" {
		return ""
	}
	data := h.decodeToken(token)
	if data == nil {
		return ""
	}
	userID, _ := data["user_id"].(string)
	return userID
}

// GetLoginIDByToken extracts the login ID from the given token value.
func (h *HeiClientAuthTool) GetLoginIDByToken(token string) string {
	if token == "" {
		return ""
	}
	data := h.decodeToken(token)
	if data == nil {
		return ""
	}
	userID, _ := data["user_id"].(string)
	return userID
}

// decodeToken retrieves token data from Redis and verifies the JWT signature.
func (h *HeiClientAuthTool) decodeToken(token string) map[string]any {
	if token == "" {
		return nil
	}

	data := h.getTokenData(token)
	if data == nil {
		return nil
	}

	// Verify JWT signature
	_, err := jwt.Parse(token, func(t *jwt.Token) (interface{}, error) {
		if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return []byte(h.secret), nil
	})
	if err != nil {
		return nil
	}

	return data
}

// getTokenData retrieves the token payload from Redis.
func (h *HeiClientAuthTool) getTokenData(token string) map[string]any {
	if token == "" {
		return nil
	}

	redisClient := h.getRedis()
	ctx := context.Background()
	tokenKey := h.getTokenKey(token)

	data, err := redisClient.Get(ctx, tokenKey).Result()
	if err == redis.Nil {
		return nil
	}
	if err != nil {
		return nil
	}

	var result map[string]any
	if err := json.Unmarshal([]byte(data), &result); err != nil {
		return nil
	}
	return result
}

// GetTokenInfo returns the full token data stored in Redis for the current request.
func (h *HeiClientAuthTool) GetTokenInfo(c *gin.Context) map[string]any {
	token := h.GetTokenValue(c)
	if token == "" {
		return nil
	}
	return h.getTokenData(token)
}

// GetExtra returns a specific extra field from the token data.
func (h *HeiClientAuthTool) GetExtra(c *gin.Context, key string) any {
	data := h.GetTokenInfo(c)
	if data != nil {
		extra, ok := data["extra"].(map[string]any)
		if ok {
			return extra[key]
		}
	}
	return nil
}

// GetSession returns the full token payload (stored session) for the current request.
func (h *HeiClientAuthTool) GetSession(c *gin.Context) map[string]any {
	token := h.GetTokenValue(c)
	if token == "" {
		return nil
	}
	return h.getTokenData(token)
}

// RenewTimeout extends the token and session timeouts.
func (h *HeiClientAuthTool) RenewTimeout(c *gin.Context, timeout ...int) {
	h.ensureConfig()

	token := h.GetTokenValue(c)
	if token == "" {
		return
	}

	newTimeout := h.expire
	if len(timeout) > 0 && timeout[0] > 0 {
		newTimeout = timeout[0]
	}

	redisClient := h.getRedis()
	ctx := context.Background()
	tokenKey := h.getTokenKey(token)
	_ = redisClient.Expire(ctx, tokenKey, time.Duration(newTimeout)*time.Second).Err()

	loginID := h.GetLoginIDByToken(token)
	if loginID != "" {
		sessionKey := h.getSessionKey(loginID)
		_ = redisClient.Expire(ctx, sessionKey, time.Duration(newTimeout)*time.Second).Err()
	}
}

// GetTokenTimeout returns the remaining TTL (in seconds) of the current token.
// Returns -1 if there is no token or if Redis returns no TTL.
func (h *HeiClientAuthTool) GetTokenTimeout(c *gin.Context) int {
	token := h.GetTokenValue(c)
	if token == "" {
		return -1
	}

	redisClient := h.getRedis()
	ctx := context.Background()
	tokenKey := h.getTokenKey(token)

	ttl, err := redisClient.TTL(ctx, tokenKey).Result()
	if err != nil || ttl < 0 {
		return -1
	}
	return int(ttl.Seconds())
}

// GetSessionTimeout returns the remaining TTL (in seconds) of the current session.
// Returns -1 if there is no active session or if Redis returns no TTL.
func (h *HeiClientAuthTool) GetSessionTimeout(c *gin.Context) int {
	loginID := h.GetLoginIDDefaultNull(c)
	if loginID == "" {
		return -1
	}

	redisClient := h.getRedis()
	ctx := context.Background()
	sessionKey := h.getSessionKey(loginID)

	ttl, err := redisClient.TTL(ctx, sessionKey).Result()
	if err != nil || ttl < 0 {
		return -1
	}
	return int(ttl.Seconds())
}

// GetTokenValueByLoginID returns one token for the given login ID.
func (h *HeiClientAuthTool) GetTokenValueByLoginID(loginID string) string {
	redisClient := h.getRedis()
	ctx := context.Background()
	sessionKey := h.getSessionKey(loginID)

	tokens, err := redisClient.SMembers(ctx, sessionKey).Result()
	if err != nil || len(tokens) == 0 {
		return ""
	}
	return tokens[0]
}

// GetTokenValuesByLoginID returns all tokens for the given login ID.
func (h *HeiClientAuthTool) GetTokenValuesByLoginID(loginID string) []string {
	redisClient := h.getRedis()
	ctx := context.Background()
	sessionKey := h.getSessionKey(loginID)

	tokens, err := redisClient.SMembers(ctx, sessionKey).Result()
	if err != nil {
		return nil
	}
	return tokens
}

// Disable marks a login ID as disabled for the specified duration (in seconds).
func (h *HeiClientAuthTool) Disable(loginID string, timeSeconds int) {
	redisClient := h.getRedis()
	ctx := context.Background()
	disableKey := h.getDisableKey(loginID)
	_ = redisClient.SetEx(ctx, disableKey, "1", time.Duration(timeSeconds)*time.Second).Err()
}

// IsDisable checks whether a login ID is currently disabled.
func (h *HeiClientAuthTool) IsDisable(loginID string) bool {
	redisClient := h.getRedis()
	ctx := context.Background()
	disableKey := h.getDisableKey(loginID)

	exists, err := redisClient.Exists(ctx, disableKey).Result()
	if err != nil {
		return false
	}
	return exists > 0
}

// CheckDisable returns an error if the login ID is currently disabled.
func (h *HeiClientAuthTool) CheckDisable(loginID string) error {
	if h.IsDisable(loginID) {
		return errors.New("账号已被禁用")
	}
	return nil
}

// GetDisableTime returns the remaining disable time (in seconds) for the given login ID.
// Returns -1 if the login ID is not disabled.
func (h *HeiClientAuthTool) GetDisableTime(loginID string) int {
	redisClient := h.getRedis()
	ctx := context.Background()
	disableKey := h.getDisableKey(loginID)

	ttl, err := redisClient.TTL(ctx, disableKey).Result()
	if err != nil || ttl < 0 {
		return -1
	}
	return int(ttl.Seconds())
}

// UntieDisable removes the disabled status from a login ID.
func (h *HeiClientAuthTool) UntieDisable(loginID string) {
	redisClient := h.getRedis()
	ctx := context.Background()
	disableKey := h.getDisableKey(loginID)
	_ = redisClient.Del(ctx, disableKey).Err()
}

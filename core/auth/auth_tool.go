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

var (
	_expire    int
	_tokenName string
	_secret    string
	_algorithm string
)

const loginTypeBusiness = "BUSINESS"

// ensureConfig initializes default values from the global config if not already set.
func ensureConfig() {
	if _secret != "" {
		return
	}
	if config.C == nil {
		return
	}
	_expire = config.C.JWT.ExpireSeconds
	_tokenName = config.C.JWT.TokenName
	_secret = config.C.JWT.SecretKey
	_algorithm = config.C.JWT.Algorithm
}

// tokenURLSafe generates n random bytes and returns them as a URL-safe base64 string without padding.
func tokenURLSafe(n int) string {
	b := make([]byte, n)
	_, _ = rand.Read(b)
	return base64.RawURLEncoding.EncodeToString(b)
}

// getRedis returns the shared Redis client.
func getRedis() *redis.Client {
	return db.Redis
}

// getTokenKey returns the Redis key for storing token data.
func getTokenKey(token string) string {
	return constants.TOKEN_PREFIX_BUSINESS + token
}

// getSessionKey returns the Redis key for storing a user's session tokens.
func getSessionKey(userID string) string {
	return constants.SESSION_PREFIX_BUSINESS + userID
}

// getDisableKey returns the Redis key for storing the disable status of a login ID.
func getDisableKey(loginID string) string {
	return constants.DISABLE_KEY_BUSINESS + loginID
}

// Init initializes the auth tool with custom expire and token name.
// If expire <= 0 or tokenName is empty, values from the global config are used.
func Init(expire int, tokenName string) {
	ensureConfig()
	if expire > 0 {
		_expire = expire
	}
	if tokenName != "" {
		_tokenName = tokenName
	}
}

// GetLoginType returns the login type identifier for business back-end users.
func GetLoginType() string {
	ensureConfig()
	return loginTypeBusiness
}

// GetTokenName returns the HTTP header name used to carry the token.
func GetTokenName() string {
	ensureConfig()
	return _tokenName
}

// GetTokenValue extracts the token from the request header.
func GetTokenValue(c *gin.Context) string {
	ensureConfig()
	if c == nil {
		return ""
	}
	return c.GetHeader(_tokenName)
}

// Login authenticates a user by user ID, stores token data in Redis, and returns the signed JWT token.
func Login(c *gin.Context, id string, extra map[string]any) (string, error) {
	ensureConfig()

	now := time.Now()
	jti := tokenURLSafe(32)

	// Build and sign JWT with jti and iat claims
	claims := jwt.MapClaims{
		"jti": jti,
		"iat": jwt.NewNumericDate(now),
	}
	token := jwt.NewWithClaims(jwt.GetSigningMethod(_algorithm), claims)
	signedToken, err := token.SignedString([]byte(_secret))
	if err != nil {
		return "", err
	}

	// Build token data to store in Redis
	tokenData := map[string]any{
		"user_id":    id,
		"type":       loginTypeBusiness,
		"created_at": now.Format("2006-01-02 15:04:05"),
		"extra":      extra,
	}
	if extra == nil {
		tokenData["extra"] = map[string]any{}
	}

	tokenDataJSON, err := json.Marshal(tokenData)
	if err != nil {
		return "", err
	}

	redisClient := getRedis()
	ctx := context.Background()

	// Store token data in Redis with expiration
	err = redisClient.SetEx(ctx, getTokenKey(signedToken), tokenDataJSON, time.Duration(_expire)*time.Second).Err()
	if err != nil {
		return "", err
	}

	// Add token to user's session set and refresh its expiration
	sessionKey := getSessionKey(id)
	err = redisClient.SAdd(ctx, sessionKey, signedToken).Err()
	if err != nil {
		return "", err
	}
	err = redisClient.Expire(ctx, sessionKey, time.Duration(_expire)*time.Second).Err()
	if err != nil {
		return "", err
	}

	return signedToken, nil
}

// Logout invalidates the current session. If loginID is provided, it kicks out all sessions for that user.
func Logout(c *gin.Context, loginID ...string) {
	ensureConfig()

	if len(loginID) > 0 {
		Kickout(loginID[0])
		return
	}

	token := GetTokenValue(c)
	if token == "" {
		return
	}

	data := getTokenData(token)
	if data != nil {
		userID, _ := data["user_id"].(string)
		if userID != "" {
			redisClient := getRedis()
			ctx := context.Background()
			sessionKey := getSessionKey(userID)
			_ = redisClient.SRem(ctx, sessionKey, token).Err()
		}
	}

	redisClient := getRedis()
	ctx := context.Background()
	tokenKey := getTokenKey(token)
	_ = redisClient.Del(ctx, tokenKey).Err()
}

// Kickout deletes all tokens and session data for the given login ID.
func Kickout(loginID string) {
	ensureConfig()

	redisClient := getRedis()
	ctx := context.Background()
	sessionKey := getSessionKey(loginID)

	tokens, err := redisClient.SMembers(ctx, sessionKey).Result()
	if err != nil {
		return
	}

	for _, token := range tokens {
		tokenKey := getTokenKey(token)
		_ = redisClient.Del(ctx, tokenKey).Err()
	}

	_ = redisClient.Del(ctx, sessionKey).Err()
}

// KickoutToken removes a specific token from the user's session set and deletes its data.
func KickoutToken(loginID, token string) {
	ensureConfig()

	redisClient := getRedis()
	ctx := context.Background()
	sessionKey := getSessionKey(loginID)
	tokenKey := getTokenKey(token)

	_ = redisClient.SRem(ctx, sessionKey, token).Err()
	_ = redisClient.Del(ctx, tokenKey).Err()
}

// IsLogin checks whether the current request carries a valid token.
func IsLogin(c *gin.Context) bool {
	loginID := GetLoginIDDefaultNull(c)
	return loginID != ""
}

// CheckLogin returns an error if the current request is not authenticated.
func CheckLogin(c *gin.Context) error {
	if !IsLogin(c) {
		return errors.New("未授权/未登录")
	}
	return nil
}

// GetLoginID returns the login ID extracted from the current request's token.
func GetLoginID(c *gin.Context) string {
	return GetLoginIDDefaultNull(c)
}

// GetLoginIDDefaultNull returns the login ID from the current request's token, or an empty string if not logged in.
func GetLoginIDDefaultNull(c *gin.Context) string {
	token := GetTokenValue(c)
	if token == "" {
		return ""
	}
	data := decodeToken(token)
	if data == nil {
		return ""
	}
	userID, _ := data["user_id"].(string)
	return userID
}

// GetLoginIDByToken extracts the login ID from the given token value.
func GetLoginIDByToken(token string) string {
	if token == "" {
		return ""
	}
	data := decodeToken(token)
	if data == nil {
		return ""
	}
	userID, _ := data["user_id"].(string)
	return userID
}

// decodeToken retrieves token data from Redis and verifies the JWT signature.
func decodeToken(token string) map[string]any {
	if token == "" {
		return nil
	}

	data := getTokenData(token)
	if data == nil {
		return nil
	}

	// Verify JWT signature
	_, err := jwt.Parse(token, func(t *jwt.Token) (interface{}, error) {
		if _, ok := t.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return []byte(_secret), nil
	})
	if err != nil {
		return nil
	}

	return data
}

// getTokenData retrieves the token payload from Redis.
func getTokenData(token string) map[string]any {
	if token == "" {
		return nil
	}

	redisClient := getRedis()
	ctx := context.Background()
	tokenKey := getTokenKey(token)

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
func GetTokenInfo(c *gin.Context) map[string]any {
	token := GetTokenValue(c)
	if token == "" {
		return nil
	}
	return getTokenData(token)
}

// GetExtra returns a specific extra field from the token data.
func GetExtra(c *gin.Context, key string) any {
	data := GetTokenInfo(c)
	if data != nil {
		extra, ok := data["extra"].(map[string]any)
		if ok {
			return extra[key]
		}
	}
	return nil
}

// GetSession returns the full token payload (stored session) for the current request.
func GetSession(c *gin.Context) map[string]any {
	token := GetTokenValue(c)
	if token == "" {
		return nil
	}
	return getTokenData(token)
}

// RenewTimeout extends the token and session timeouts.
func RenewTimeout(c *gin.Context, timeout ...int) {
	ensureConfig()

	token := GetTokenValue(c)
	if token == "" {
		return
	}

	newTimeout := _expire
	if len(timeout) > 0 && timeout[0] > 0 {
		newTimeout = timeout[0]
	}

	redisClient := getRedis()
	ctx := context.Background()
	tokenKey := getTokenKey(token)
	_ = redisClient.Expire(ctx, tokenKey, time.Duration(newTimeout)*time.Second).Err()

	loginID := GetLoginIDByToken(token)
	if loginID != "" {
		sessionKey := getSessionKey(loginID)
		_ = redisClient.Expire(ctx, sessionKey, time.Duration(newTimeout)*time.Second).Err()
	}
}

// GetTokenTimeout returns the remaining TTL (in seconds) of the current token.
// Returns -1 if there is no token or if Redis returns no TTL.
func GetTokenTimeout(c *gin.Context) int {
	token := GetTokenValue(c)
	if token == "" {
		return -1
	}

	redisClient := getRedis()
	ctx := context.Background()
	tokenKey := getTokenKey(token)

	ttl, err := redisClient.TTL(ctx, tokenKey).Result()
	if err != nil || ttl < 0 {
		return -1
	}
	return int(ttl.Seconds())
}

// GetSessionTimeout returns the remaining TTL (in seconds) of the current session.
// Returns -1 if there is no active session or if Redis returns no TTL.
func GetSessionTimeout(c *gin.Context) int {
	loginID := GetLoginIDDefaultNull(c)
	if loginID == "" {
		return -1
	}

	redisClient := getRedis()
	ctx := context.Background()
	sessionKey := getSessionKey(loginID)

	ttl, err := redisClient.TTL(ctx, sessionKey).Result()
	if err != nil || ttl < 0 {
		return -1
	}
	return int(ttl.Seconds())
}

// GetTokenValueByLoginID returns one token for the given login ID.
func GetTokenValueByLoginID(loginID string) string {
	redisClient := getRedis()
	ctx := context.Background()
	sessionKey := getSessionKey(loginID)

	tokens, err := redisClient.SMembers(ctx, sessionKey).Result()
	if err != nil || len(tokens) == 0 {
		return ""
	}
	return tokens[0]
}

// GetTokenValuesByLoginID returns all tokens for the given login ID.
func GetTokenValuesByLoginID(loginID string) []string {
	redisClient := getRedis()
	ctx := context.Background()
	sessionKey := getSessionKey(loginID)

	tokens, err := redisClient.SMembers(ctx, sessionKey).Result()
	if err != nil {
		return nil
	}
	return tokens
}

// Disable marks a login ID as disabled for the specified duration (in seconds).
func Disable(loginID string, timeSeconds int) {
	redisClient := getRedis()
	ctx := context.Background()
	disableKey := getDisableKey(loginID)
	_ = redisClient.SetEx(ctx, disableKey, "1", time.Duration(timeSeconds)*time.Second).Err()
}

// IsDisable checks whether a login ID is currently disabled.
func IsDisable(loginID string) bool {
	redisClient := getRedis()
	ctx := context.Background()
	disableKey := getDisableKey(loginID)

	exists, err := redisClient.Exists(ctx, disableKey).Result()
	if err != nil {
		return false
	}
	return exists > 0
}

// CheckDisable returns an error if the login ID is currently disabled.
func CheckDisable(loginID string) error {
	if IsDisable(loginID) {
		return errors.New("账号已被禁用")
	}
	return nil
}

// GetDisableTime returns the remaining disable time (in seconds) for the given login ID.
// Returns -1 if the login ID is not disabled.
func GetDisableTime(loginID string) int {
	redisClient := getRedis()
	ctx := context.Background()
	disableKey := getDisableKey(loginID)

	ttl, err := redisClient.TTL(ctx, disableKey).Result()
	if err != nil || ttl < 0 {
		return -1
	}
	return int(ttl.Seconds())
}

// UntieDisable removes the disabled status from a login ID.
func UntieDisable(loginID string) {
	redisClient := getRedis()
	ctx := context.Background()
	disableKey := getDisableKey(loginID)
	_ = redisClient.Del(ctx, disableKey).Err()
}

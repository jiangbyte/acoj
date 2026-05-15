package auth

import (
	"context"
	"encoding/json"
	"time"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"
	"github.com/golang-jwt/jwt/v5"

	"hei-goframe/internal/consts"
)

type AuthTool struct {
	secret        string
	algorithm     string
	expire        int64
	tokenName     string
	tokenPrefix   string
	sessionPrefix string
	disableKey    string
	loginType     string
}

var (
	BusinessAuth *AuthTool
	ConsumerAuth *AuthTool
)

func init() {
	ctx := context.Background()
	BusinessAuth = NewAuthTool(ctx, consts.LoginTypeBusiness,
		consts.TokenPrefixBusiness, consts.SessionPrefixBusiness, consts.DisableKeyBusiness)
	ConsumerAuth = NewAuthTool(ctx, consts.LoginTypeConsumer,
		consts.TokenPrefixConsumer, consts.SessionPrefixConsumer, consts.DisableKeyConsumer)
}

func NewAuthTool(ctx context.Context, loginType, tokenPrefix, sessionPrefix, disableKey string) *AuthTool {
	return &AuthTool{
		secret:        g.Cfg().MustGet(ctx, "hei.jwt.secretKey").String(),
		algorithm:     g.Cfg().MustGet(ctx, "hei.jwt.algorithm", "HS256").String(),
		expire:        g.Cfg().MustGet(ctx, "hei.jwt.expireSeconds", 2592000).Int64(),
		tokenName:     g.Cfg().MustGet(ctx, "hei.jwt.tokenName", "Authorization").String(),
		tokenPrefix:   tokenPrefix,
		sessionPrefix: sessionPrefix,
		disableKey:    disableKey,
		loginType:     loginType,
	}
}

func (a *AuthTool) GetTokenName() string {
	return a.tokenName
}

func (a *AuthTool) GetLoginType() string {
	return a.loginType
}

// Login 创建登录 token 并写入 Redis
func (a *AuthTool) Login(ctx context.Context, id string, extra map[string]interface{}) (string, error) {
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

	tokenData := map[string]interface{}{
		"user_id":    id,
		"type":       a.loginType,
		"created_at": now.Format(time.RFC3339),
		"extra":      extra,
	}
	tokenJson, _ := json.Marshal(tokenData)

	g.Redis().SetEX(ctx, a.tokenPrefix+tokenStr, tokenJson, a.expire)

	g.Redis().SAdd(ctx, a.sessionPrefix+id, tokenStr)
	g.Redis().Expire(ctx, a.sessionPrefix+id, a.expire)

	return tokenStr, nil
}

// Logout 登出，删除 token
func (a *AuthTool) Logout(ctx context.Context, tokenStr string) error {
	if tokenStr == "" {
		return nil
	}
	tokenKey := a.tokenPrefix + tokenStr

	data, err := g.Redis().Get(ctx, tokenKey)
	if err == nil && !data.IsNil() {
		var td map[string]interface{}
		if json.Unmarshal(data.Bytes(), &td) == nil {
			if uid, ok := td["user_id"].(string); ok {
				g.Redis().SRem(ctx, a.sessionPrefix+uid, tokenStr)
			}
		}
	}
	_, err = g.Redis().Del(ctx, tokenKey)
	return err
}

// Kickout 踢下线用户，删除所有 token
func (a *AuthTool) Kickout(ctx context.Context, loginId string) error {
	sessionKey := a.sessionPrefix + loginId
	tokens, err := g.Redis().SMembers(ctx, sessionKey)
	if err != nil {
		return err
	}
	for _, t := range tokens {
		g.Redis().Del(ctx, a.tokenPrefix+t.String())
	}
	_, err = g.Redis().Del(ctx, sessionKey)
	return err
}

// KickoutToken 踢下线用户的指定 token
func (a *AuthTool) KickoutToken(ctx context.Context, loginId, tokenStr string) error {
	g.Redis().SRem(ctx, a.sessionPrefix+loginId, tokenStr)
	_, err := g.Redis().Del(ctx, a.tokenPrefix+tokenStr)
	return err
}

// IsLogin 检查用户是否已登录
func (a *AuthTool) IsLogin(ctx context.Context, tokenStr string) bool {
	id, _ := a.GetLoginId(ctx, tokenStr)
	return id != ""
}

// GetLoginId 从 token 获取用户 ID
func (a *AuthTool) GetLoginId(ctx context.Context, tokenStr string) (string, error) {
	if tokenStr == "" {
		return "", nil
	}
	payload, err := a.decodeToken(ctx, tokenStr)
	if err != nil || payload == nil {
		return "", err
	}
	if sub, ok := payload["sub"].(string); ok {
		return sub, nil
	}
	return gconv.String(payload["sub"]), nil
}

// GetSession 获取 token 中的会话数据
func (a *AuthTool) GetSession(ctx context.Context, tokenStr string) (map[string]interface{}, error) {
	if tokenStr == "" {
		return nil, nil
	}
	tokenKey := a.tokenPrefix + tokenStr
	data, err := g.Redis().Get(ctx, tokenKey)
	if err != nil || data.IsNil() {
		return nil, err
	}
	var td map[string]interface{}
	if err := json.Unmarshal(data.Bytes(), &td); err != nil {
		return nil, err
	}
	return td, nil
}

// GetTokenTimeout 获取 token 剩余秒数
func (a *AuthTool) GetTokenTimeout(ctx context.Context, tokenStr string) int {
	if tokenStr == "" {
		return -1
	}
	ttl, err := g.Redis().TTL(ctx, a.tokenPrefix+tokenStr)
	if err != nil {
		return -1
	}
	return int(ttl)
}

// RenewTimeout 续期 token 和 session
func (a *AuthTool) RenewTimeout(ctx context.Context, tokenStr string, timeout ...int64) error {
	if tokenStr == "" {
		return nil
	}
	newTimeout := a.expire
	if len(timeout) > 0 && timeout[0] > 0 {
		newTimeout = timeout[0]
	}
	tokenKey := a.tokenPrefix + tokenStr
	g.Redis().Expire(ctx, tokenKey, newTimeout)

	loginId, _ := a.GetLoginId(ctx, tokenStr)
	if loginId != "" {
		g.Redis().Expire(ctx, a.sessionPrefix+loginId, newTimeout)
	}
	return nil
}

// Disable 禁用用户 login_id 一段时间（秒）
func (a *AuthTool) Disable(ctx context.Context, loginId string, seconds int64) error {
	g.Redis().SetEX(ctx, a.disableKey+loginId, "1", seconds)
	return nil
}

// IsDisable 检查用户是否被禁用
func (a *AuthTool) IsDisable(ctx context.Context, loginId string) bool {
	exists, _ := g.Redis().Exists(ctx, a.disableKey+loginId)
	return exists > 0
}

// GetDisableTime 获取禁用剩余秒数
func (a *AuthTool) GetDisableTime(ctx context.Context, loginId string) int {
	ttl, err := g.Redis().TTL(ctx, a.disableKey+loginId)
	if err != nil || ttl <= 0 {
		return -1
	}
	return int(ttl)
}

// UntieDisable 解除禁用
func (a *AuthTool) UntieDisable(ctx context.Context, loginId string) error {
	_, err := g.Redis().Del(ctx, a.disableKey+loginId)
	return err
}

// GetTokenValueByLoginId 获取用户的一个活跃 token
func (a *AuthTool) GetTokenValueByLoginId(ctx context.Context, loginId string) (string, error) {
	tokens, err := g.Redis().SMembers(ctx, a.sessionPrefix+loginId)
	if err != nil || len(tokens) == 0 {
		return "", err
	}
	return tokens[0].String(), nil
}

// GetTokenValuesByLoginId 获取用户的所有活跃 token
func (a *AuthTool) GetTokenValuesByLoginId(ctx context.Context, loginId string) ([]string, error) {
	tokens, err := g.Redis().SMembers(ctx, a.sessionPrefix+loginId)
	if err != nil {
		return nil, err
	}
	result := make([]string, len(tokens))
	for i, t := range tokens {
		result[i] = t.String()
	}
	return result, nil
}

func (a *AuthTool) decodeToken(ctx context.Context, tokenStr string) (jwt.MapClaims, error) {
	tokenKey := a.tokenPrefix + tokenStr
	exists, err := g.Redis().Exists(ctx, tokenKey)
	if err != nil || exists == 0 {
		return nil, err
	}

	token, err := jwt.Parse(tokenStr, func(token *jwt.Token) (interface{}, error) {
		return []byte(a.secret), nil
	})
	if err != nil {
		g.Redis().Del(ctx, tokenKey)
		return nil, err
	}
	if claims, ok := token.Claims.(jwt.MapClaims); ok && token.Valid {
		return claims, nil
	}
	return nil, nil
}

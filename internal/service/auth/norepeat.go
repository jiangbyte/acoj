package auth

import (
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/gogf/gf/v2/errors/gcode"
	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/consts"
)

// NoRepeatConfig configures the no-repeat submission guard.
type NoRepeatConfig struct {
	Interval int // interval in milliseconds
}

// NoRepeat returns a middleware that prevents duplicate submissions.
// If the same user submits identical params to the same URL within
// the configured interval (ms), a 400 error is returned.
func NoRepeat(interval int) ghttp.HandlerFunc {
	return func(r *ghttp.Request) {
		if interval <= 0 {
			interval = 5000
		}

		loginId := GetLoginIdFromCtx(r.Context())
		ip := r.GetClientIp()
		path := r.URL.Path
		paramsHash := paramsHash(r)

		cacheKey := fmt.Sprintf("%s%s:%s:%s", consts.NoRepeatPrefix, ip, loginId, path)

		// Check Redis for existing request hash
		exists, err := g.Redis().Get(r.Context(), cacheKey)
		if err == nil && !exists.IsNil() {
			var cached struct {
				Hash string `json:"hash"`
				Time int64  `json:"time"`
			}
			if err := json.Unmarshal(exists.Bytes(), &cached); err == nil {
				if cached.Hash == paramsHash {
					elapsed := time.Now().UnixMilli() - cached.Time
					if elapsed < int64(interval) {
						remaining := max(1, int64(interval)-elapsed) / 1000
						r.Response.WriteJsonExit(g.Map{
							"code":    400,
							"message": fmt.Sprintf("请求过于频繁，请%d秒后再试", remaining),
							"success": false,
							"data":    nil,
						})
						return
					}
				}
			}
		}

		// Store current request hash
		data, _ := json.Marshal(map[string]interface{}{
			"hash": paramsHash,
			"time": time.Now().UnixMilli(),
		})
		g.Redis().SetEX(r.Context(), cacheKey, data, 3600)

		r.Middleware.Next()
	}
}

// GetLoginIdFromCtx extracts login ID from context.
func GetLoginIdFromCtx(ctx context.Context) string {
	if v := ctx.Value(ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}

var excludeParams = map[string]bool{
	"request": true,
	"db":      true,
	"file":    true,
}

func paramsHash(r *ghttp.Request) string {
	params := make(map[string]interface{})

	// Collect query params
	for k, v := range r.GetQueryMap() {
		if !excludeParams[k] {
			params[k] = v
		}
	}

	// Collect body params (only for POST/PUT/PATCH)
	if r.Method == "POST" || r.Method == "PUT" || r.Method == "PATCH" {
		body := r.GetBody()
		if len(body) > 0 {
			var bodyMap map[string]interface{}
			if json.Unmarshal(body, &bodyMap) == nil {
				for k, v := range bodyMap {
					if !excludeParams[k] {
						params[k] = v
					}
				}
			} else {
				params["_body"] = string(body)
			}
		}

		// Also check multipart/form-data
		for k, v := range r.GetMap() {
			if !excludeParams[k] {
				params[k] = v
			}
		}
	}

	data, _ := json.Marshal(params)
	hash := sha256.Sum256(data)
	return fmt.Sprintf("%x", hash[:8])
}

func max(a, b int64) int64 {
	if a > b {
		return a
	}
	return b
}

// CheckPermission is a middleware that checks if the current user has a specific permission.
// It parses the token from the request header and uses the PermissionTool to verify.
func CheckPermission(permission string) ghttp.HandlerFunc {
	return func(r *ghttp.Request) {
		loginType := GetLoginTypeFromCtx(r.Context())
		if loginType == "" {
			r.Middleware.Next()
			return
		}

		tokenStr := r.Header.Get(getTokenName(loginType))
		tokenStr = strings.TrimPrefix(tokenStr, "Bearer ")
		tokenStr = strings.TrimPrefix(tokenStr, "bearer ")

		has, err := PermTool.HasPermission(r.Context(), permission, tokenStr, loginType)
		if err != nil || !has {
			r.Response.WriteJsonExit(g.Map{
				"code":    403,
				"message": "无权限: " + permission,
				"success": false,
				"data":    nil,
			})
			return
		}
		r.Middleware.Next()
	}
}

// GetLoginTypeFromCtx extracts login type from context.
func GetLoginTypeFromCtx(ctx context.Context) string {
	if v := ctx.Value(ContextKeyLoginType); v != nil {
		return v.(string)
	}
	return ""
}

// CheckNoRepeatInline checks for duplicate submissions inline (used when middleware is not feasible).
// Call at the beginning of a controller method. If the same request was made within the interval (ms),
// it returns a "请求过于频繁" error.
func CheckNoRepeatInline(ctx context.Context, interval int) error {
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return nil
	}
	if interval <= 0 {
		interval = 5000
	}

	loginId := GetLoginIdFromCtx(ctx)
	ip := r.GetClientIp()
	path := r.URL.Path
	hash := paramsHash(r)

	cacheKey := fmt.Sprintf("%s%s:%s:%s", consts.NoRepeatPrefix, ip, loginId, path)

	exists, err := g.Redis().Get(ctx, cacheKey)
	if err == nil && !exists.IsNil() {
		var cached struct {
			Hash string `json:"hash"`
			Time int64  `json:"time"`
		}
		if err := json.Unmarshal(exists.Bytes(), &cached); err == nil {
			if cached.Hash == hash {
				elapsed := time.Now().UnixMilli() - cached.Time
				if elapsed < int64(interval) {
					remaining := max(1, int64(interval)-elapsed) / 1000
					return gerror.NewCode(gcode.New(400, "", nil),
						fmt.Sprintf("请求过于频繁，请%d秒后再试", remaining))
				}
			}
		}
	}

	data, _ := json.Marshal(map[string]interface{}{
		"hash": hash,
		"time": time.Now().UnixMilli(),
	})
	g.Redis().SetEX(ctx, cacheKey, data, 3600)
	return nil
}

func getTokenName(loginType string) string {
	if loginType == consts.LoginTypeConsumer {
		return ConsumerAuth.GetTokenName()
	}
	return BusinessAuth.GetTokenName()
}

// MustPerm checks if the current user has the specified permission.
// Returns a 403 error if not authorized. Inline equivalent of FastAPI's @HeiCheckPermission.
func MustPerm(ctx context.Context, permission string) error {
	loginId := GetLoginIdFromCtx(ctx)
	if loginId == "" {
		return nil
	}
	loginType := GetLoginTypeFromCtx(ctx)
	if loginType == "" {
		return nil
	}
	perms, err := PermTool.GetPermissionListByLoginId(ctx, loginId, loginType)
	if err != nil {
		return err
	}
	matcher := &PermissionMatcher{}
	if !matcher.HasPermission(permission, perms) {
		return gerror.NewCode(gcode.New(403, "", nil), "无权限: "+permission)
	}
	return nil
}

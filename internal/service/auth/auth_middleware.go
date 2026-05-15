package auth

import (
	"context"
	"strings"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/consts"
)

// Context keys for auth info stored in request context.
type ContextKey string

const (
	ContextKeyLoginId   ContextKey = "loginId"
	ContextKeyLoginType ContextKey = "loginType"
)

// MiddlewareAuth is the path-based authentication middleware.
func MiddlewareAuth(r *ghttp.Request) {
	path := r.URL.Path

	if isPublicPath(path) {
		r.Middleware.Next()
		return
	}

	loginType := getLoginTypeFromPath(path)
	if loginType == "" {
		r.Middleware.Next()
		return
	}

	var authTool *AuthTool
	if loginType == consts.LoginTypeBusiness {
		authTool = BusinessAuth
	} else {
		authTool = ConsumerAuth
	}

	tokenStr := r.Header.Get(authTool.GetTokenName())
	tokenStr = strings.TrimPrefix(tokenStr, "Bearer ")
	tokenStr = strings.TrimPrefix(tokenStr, "bearer ")

	loginId, err := authTool.GetLoginId(r.Context(), tokenStr)
	if err != nil || loginId == "" {
		r.Response.WriteJsonExit(g.Map{
			"code":    401,
			"message": "未登录或登录已过期",
			"success": false,
			"data":    nil,
		})
		return
	}

	ctx := context.WithValue(r.Context(), ContextKeyLoginId, loginId)
	ctx = context.WithValue(ctx, ContextKeyLoginType, loginType)
	r.SetCtx(ctx)

	r.Middleware.Next()
}

// MiddlewareCORS adds CORS headers from config.
func MiddlewareCORS(r *ghttp.Request) {
	r.Response.CORS(ghttp.CORSOptions{
		AllowOrigin:      strings.Join(g.Cfg().MustGet(r.Context(), "hei.cors.allowOrigins").Strings(), ","),
		AllowMethods:     strings.Join(g.Cfg().MustGet(r.Context(), "hei.cors.allowMethods").Strings(), ","),
		AllowHeaders:     strings.Join(g.Cfg().MustGet(r.Context(), "hei.cors.allowHeaders").Strings(), ","),
		AllowCredentials: g.Cfg().MustGet(r.Context(), "hei.cors.allowCredentials").String(),
	})
	r.Middleware.Next()
}

func isPublicPath(path string) bool {
	publicPrefixes := []string{
		"/api/v1/public/",
		"/api.json",
		"/swagger",
	}
	for _, p := range publicPrefixes {
		if strings.HasPrefix(path, p) {
			return true
		}
	}
	return false
}

func getLoginTypeFromPath(path string) string {
	if strings.HasPrefix(path, "/api/v1/b/") || strings.HasPrefix(path, "/api/v1/public/b/") || strings.HasPrefix(path, "/api/v1/sys/") {
		return consts.LoginTypeBusiness
	}
	if strings.HasPrefix(path, "/api/v1/c/") || strings.HasPrefix(path, "/api/v1/public/c/") || strings.HasPrefix(path, "/api/v1/client/") {
		return consts.LoginTypeConsumer
	}
	return ""
}

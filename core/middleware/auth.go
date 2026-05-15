package middleware

import (
	"regexp"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/result"
)

// Path pattern constants (mirror Python AuthMiddleware)
var (
	publicBPattern  = regexp.MustCompile(`^/api/v\d+/public/b/`)
	publicCPattern  = regexp.MustCompile(`^/api/v\d+/public/c/`)
	privateCPattern = regexp.MustCompile(`^/api/v\d+/c/`)
	defaultBPattern = regexp.MustCompile(`^/api/v\d+/`)
	staticPaths     = []string{"/favicon.ico", "/docs", "/swagger", "/openapi.json"}
)

func Auth() gin.HandlerFunc {
	return func(c *gin.Context) {
		path := c.Request.URL.Path

		// Skip static paths
		for _, p := range staticPaths {
			if path == p || (len(path) >= len(p) && path[:len(p)] == p) {
				c.Next()
				return
			}
		}

		// Skip OPTIONS
		if c.Request.Method == "OPTIONS" {
			c.Next()
			return
		}

		// Public paths → skip auth
		if publicBPattern.MatchString(path) || publicCPattern.MatchString(path) {
			c.Next()
			return
		}

		// C端 paths → consumer auth
		if privateCPattern.MatchString(path) {
			if !auth.ClientAuthTool.IsLogin(c) {
				result.Failure(c, "Unauthorized", 401)
				c.Abort()
				return
			}
			c.Set("login_id", auth.ClientAuthTool.GetLoginID(c))
			c.Next()
			return
		}

		// DEFAULT: everything else → business auth (B端)
		if defaultBPattern.MatchString(path) {
			if !auth.AuthTool.IsLogin(c) {
				result.Failure(c, "Unauthorized", 401)
				c.Abort()
				return
			}
			c.Set("login_id", auth.AuthTool.GetLoginID(c))
			c.Next()
			return
		}

		c.Next()
	}
}

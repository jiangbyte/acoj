package middleware

import (
	"regexp"
	"strings"

	"hei-gin/core/auth"

	"github.com/gin-gonic/gin"
)

var (
	// staticPaths lists paths that require no authentication.
	staticPaths = []string{
		"/favicon.ico",
		"/docs",
		"/redoc",
		"/openapi.json",
		"/v3/api-docs",
	}

	// apiSegmentPattern extracts the segment after /api/v<digits>/ from a path.
	// Example: /api/v1/sys/user -> matches with capture "sys".
	apiSegmentPattern = regexp.MustCompile(`^/api/v\d+/([^/]+)/`)
)

// AuthCheck returns a Gin middleware that enforces authentication based on path patterns.
//
//	Static paths (favicon.ico, docs, etc.)        -> pass
//	OPTIONS method                                 -> pass
//	/api/v{n}/public/*                             -> pass (no auth)
//	/api/v{n}/c/*                                  -> client auth required
//	/api/v{n}/b/* or /api/v{n}/<other>/*           -> regular auth required
func AuthCheck() gin.HandlerFunc {
	return func(c *gin.Context) {
		path := c.Request.URL.Path
		method := c.Request.Method

		// 1. Static paths – no auth
		for _, sp := range staticPaths {
			if path == sp || strings.HasPrefix(path, sp) {
				c.Next()
				return
			}
		}

		// 2. OPTIONS method – no auth
		if method == "OPTIONS" {
			c.Next()
			return
		}

		// 3. Check versioned API paths
		matches := apiSegmentPattern.FindStringSubmatch(path)
		if len(matches) < 2 {
			c.Next()
			return
		}

		segment := matches[1]

		// 4. Public paths – no auth
		if segment == "public" {
			c.Next()
			return
		}

		// 5. Client auth path: /api/v{n}/c/...
		if segment == "c" {
			clientAuth := &auth.HeiClientAuthTool{}
			if !clientAuth.IsLogin(c) {
				c.Abort()
				c.JSON(200, gin.H{"code": 401, "message": "未授权/未登录", "success": false})
				return
			}
			c.Next()
			return
		}

		// 6. B path (/api/v{n}/b/...) or DEFAULT (/api/v{n}/<other>/...)
		if !auth.IsLogin(c) {
			c.Abort()
			c.JSON(200, gin.H{"code": 401, "message": "未授权/未登录", "success": false})
			return
		}

		c.Next()
	}
}

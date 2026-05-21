package middleware

import (
	"hei-gin/core/auth"
	"hei-gin/core/result"

	"github.com/gin-gonic/gin"
)

// HeiCheckLogin returns a middleware that checks if the user is logged in.
// loginType defaults to "BUSINESS". Pass "CONSUMER" for client-side users.
// Sets "loginUser" in the Gin context for downstream audit logging.
func HeiCheckLogin(loginType ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		lt := "BUSINESS"
		if len(loginType) > 0 {
			lt = loginType[0]
		}
		var isLogin bool
		if lt == "CONSUMER" {
			tool := &auth.HeiClientAuthTool{}
			isLogin = tool.IsLogin(c)
		} else {
			isLogin = auth.IsLogin(c)
		}
		if !isLogin {
			c.Abort()
			c.JSON(200, result.Failure(c, "未授权/未登录", 401, nil))
			return
		}

		// Set loginUser for downstream audit logging
		if username := auth.GetExtra(c, "username"); username != nil {
			if u, ok := username.(string); ok && u != "" {
				c.Set("loginUser", u)
			}
		}

		c.Next()
	}
}

// HeiClientCheckLogin returns a middleware that checks if the CONSUMER user is logged in.
func HeiClientCheckLogin() gin.HandlerFunc {
	return HeiCheckLogin("CONSUMER")
}

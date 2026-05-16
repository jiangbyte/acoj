package middleware

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/result"
)

// CheckClientLogin returns middleware that verifies a CONSUMER is logged in.
func CheckClientLogin() gin.HandlerFunc {
	return func(c *gin.Context) {
		loginID := auth.ClientAuthTool.GetLoginID(c)
		if loginID == "" {
			result.Failure(c, "未授权/未登录", 401)
			c.Abort()
			return
		}
		c.Set("login_id", loginID)
		c.Next()
	}
}

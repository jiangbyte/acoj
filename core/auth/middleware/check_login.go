package middleware

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/result"
)

// CheckLogin returns middleware that verifies the user is logged in.
// Determines B-end or C-end auth based on URL path.
func CheckLogin() gin.HandlerFunc {
	return func(c *gin.Context) {
		loginType := auth.DetectLoginType(c)
		var loginID string
		if loginType == "CONSUMER" {
			loginID = auth.ClientAuthTool.GetLoginID(c)
		} else {
			loginID = auth.AuthTool.GetLoginID(c)
		}
		if loginID == "" {
			result.Failure(c, "未授权/未登录", 401)
			c.Abort()
			return
		}
		c.Next()
	}
}

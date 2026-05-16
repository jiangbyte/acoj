package middleware

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/enums"
	"hei-gin/core/result"
)

// CheckClientPermission returns middleware that verifies a CONSUMER has the required permission.
func CheckClientPermission(code string) gin.HandlerFunc {
	return func(c *gin.Context) {
		permissions := auth.PermissionInterface.GetPermissionList(
			auth.ClientAuthTool.GetLoginID(c),
			string(enums.LoginTypeConsumer),
		)
		if !auth.Matcher.HasPermission(code, permissions) {
			result.Failure(c, "缺少权限: "+code, 403)
			c.Abort()
			return
		}
		c.Next()
	}
}

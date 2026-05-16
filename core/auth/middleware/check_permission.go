package middleware

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/constants"
	"hei-gin/core/result"
)

// CheckPermission returns middleware that verifies the user has the required permission.
// It also registers the route for auto-discovery permission scanning.
func CheckPermission(code string) gin.HandlerFunc {
	return func(c *gin.Context) {
		auth.RegisterPermission(c.Request.Method, c.FullPath(), code)

		loginType := auth.DetectLoginType(c)
		permissions := auth.PermissionTool.GetPermissionList(c, loginType)
		if !auth.Matcher.HasPermission(code, permissions) {
			roles := auth.PermissionTool.GetRoleList(c, loginType)
			for _, role := range roles {
				if role == constants.SuperAdminCode {
					c.Next()
					return
				}
			}
			result.Failure(c, "缺少权限: "+code, 403)
			c.Abort()
			return
		}
		c.Next()
	}
}

// CheckPermissionAnd returns middleware that verifies the user has ALL specified permissions (AND mode).
func CheckPermissionAnd(codes ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		for _, code := range codes {
			auth.RegisterPermission(c.Request.Method, c.FullPath(), code)
		}

		loginType := auth.DetectLoginType(c)
		permissions := auth.PermissionTool.GetPermissionList(c, loginType)
		roles := auth.PermissionTool.GetRoleList(c, loginType)
		for _, role := range roles {
			if role == constants.SuperAdminCode {
				c.Next()
				return
			}
		}
		if !auth.Matcher.HasPermissionAnd(codes, permissions) {
			result.Failure(c, "缺少权限", 403)
			c.Abort()
			return
		}
		c.Next()
	}
}

// CheckPermissionOr returns middleware that verifies the user has ANY of the specified permissions (OR mode).
func CheckPermissionOr(codes ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		for _, code := range codes {
			auth.RegisterPermission(c.Request.Method, c.FullPath(), code)
		}

		loginType := auth.DetectLoginType(c)
		permissions := auth.PermissionTool.GetPermissionList(c, loginType)
		if auth.Matcher.HasPermissionOr(codes, permissions) {
			c.Next()
			return
		}
		roles := auth.PermissionTool.GetRoleList(c, loginType)
		for _, role := range roles {
			if role == constants.SuperAdminCode {
				c.Next()
				return
			}
		}
		result.Failure(c, "缺少权限", 403)
		c.Abort()
	}
}

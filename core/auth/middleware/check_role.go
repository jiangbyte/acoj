package middleware

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/constants"
	"hei-gin/core/result"
)

// CheckRole returns middleware that verifies the user has the specified role.
// Supports AND mode (user must have all roles).
func CheckRole(roleCode string) gin.HandlerFunc {
	return func(c *gin.Context) {
		loginType := auth.DetectLoginType(c)
		roles := auth.PermissionTool.GetRoleList(c, loginType)
		for _, role := range roles {
			if role == roleCode || role == constants.SuperAdminCode {
				c.Next()
				return
			}
		}
		result.Failure(c, "无角色权限: "+roleCode, 403)
		c.Abort()
	}
}

// CheckRoleAnd returns middleware that verifies the user has ALL specified roles.
func CheckRoleAnd(roleCodes ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		loginType := auth.DetectLoginType(c)
		roles := auth.PermissionTool.GetRoleList(c, loginType)
		for _, rc := range roleCodes {
			hasRole := false
			for _, r := range roles {
				if r == rc || r == constants.SuperAdminCode {
					hasRole = true
					break
				}
			}
			if !hasRole {
				result.Failure(c, "无角色权限: "+rc, 403)
				c.Abort()
				return
			}
		}
		c.Next()
	}
}

// CheckRoleOr returns middleware that verifies the user has ANY of the specified roles.
func CheckRoleOr(roleCodes ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		loginType := auth.DetectLoginType(c)
		roles := auth.PermissionTool.GetRoleList(c, loginType)
		for _, rc := range roleCodes {
			for _, r := range roles {
				if r == rc || r == constants.SuperAdminCode {
					c.Next()
					return
				}
			}
		}
		result.Failure(c, "无角色权限", 403)
		c.Abort()
	}
}

package middleware

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/constants"
	"hei-gin/core/result"
)

// CheckClientRole returns middleware that verifies a CONSUMER has the specified role.
func CheckClientRole(roleCode string) gin.HandlerFunc {
	return func(c *gin.Context) {
		loginID := auth.ClientAuthTool.GetLoginID(c)
		roles := auth.PermissionInterface.GetRoleList(loginID, "CONSUMER")
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

// CheckClientRoleAnd returns middleware that verifies a CONSUMER has ALL specified roles.
func CheckClientRoleAnd(roleCodes ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		loginID := auth.ClientAuthTool.GetLoginID(c)
		roles := auth.PermissionInterface.GetRoleList(loginID, "CONSUMER")
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

// CheckClientRoleOr returns middleware that verifies a CONSUMER has ANY of the specified roles.
func CheckClientRoleOr(roleCodes ...string) gin.HandlerFunc {
	return func(c *gin.Context) {
		loginID := auth.ClientAuthTool.GetLoginID(c)
		roles := auth.PermissionInterface.GetRoleList(loginID, "CONSUMER")
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

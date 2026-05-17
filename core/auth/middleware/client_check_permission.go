package middleware

import (
	"github.com/gin-gonic/gin"
)

// HeiClientCheckPermission returns a middleware that checks the CONSUMER user has the required permissions.
// mode defaults to "AND" (all permissions required). Pass "OR" for any permission.
func HeiClientCheckPermission(permissions []string, mode ...string) gin.HandlerFunc {
	m := "AND"
	if len(mode) > 0 {
		m = mode[0]
	}
	return heiCheckPermissionInner("CONSUMER", permissions, m)
}

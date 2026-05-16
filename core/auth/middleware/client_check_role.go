package middleware

import (
	"github.com/gin-gonic/gin"
)

// HeiClientCheckRole returns a middleware that checks the CONSUMER user has the required roles.
// mode defaults to "AND" (all roles required). Pass "OR" for any role.
func HeiClientCheckRole(roles []string, mode ...string) gin.HandlerFunc {
	m := "AND"
	if len(mode) > 0 {
		m = mode[0]
	}
	return heiCheckRoleInner("CONSUMER", roles, m)
}

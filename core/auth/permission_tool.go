package auth

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"

	"hei-gin/core/enums"
	"hei-gin/core/result"
)

// getLoginID extracts the login ID from the request using the appropriate auth tool.
func getLoginID(c *gin.Context, loginType string) string {
	if loginType == string(enums.LoginTypeConsumer) {
		clientAuth := NewHeiClientAuthTool()
		return clientAuth.GetLoginID(c)
	}
	return GetLoginID(c)
}

// GetPermissionList gets the current user's permission list from the request context.
// Results are cached per-request in the gin context to avoid redundant DB queries.
func GetPermissionList(c *gin.Context, loginType string) ([]string, error) {
	// Check per-request cache
	cacheKey := "_perm_cache_" + loginType
	if cached, exists := c.Get(cacheKey); exists {
		if perms, ok := cached.([]string); ok {
			return perms, nil
		}
	}

	iface := GetInterface()
	if iface == nil {
		return []string{}, nil
	}

	loginID := getLoginID(c, loginType)
	if loginID == "" {
		return []string{}, nil
	}

	perms, err := iface.GetPermissionList(loginID, loginType)
	if err != nil {
		return perms, err
	}
	// Cache per-request
	c.Set(cacheKey, perms)
	return perms, nil
}

// GetRoleList gets the current user's role list from the request context.
// Results are cached per-request in the gin context to avoid redundant DB queries.
func GetRoleList(c *gin.Context, loginType string) ([]string, error) {
	// Check per-request cache
	cacheKey := "_role_cache_" + loginType
	if cached, exists := c.Get(cacheKey); exists {
		if roles, ok := cached.([]string); ok {
			return roles, nil
		}
	}

	iface := GetInterface()
	if iface == nil {
		return []string{}, nil
	}

	loginID := getLoginID(c, loginType)
	if loginID == "" {
		return []string{}, nil
	}

	roles, err := iface.GetRoleList(loginID, loginType)
	if err != nil {
		return roles, err
	}
	// Cache per-request
	c.Set(cacheKey, roles)
	return roles, nil
}

// GetPermissionListByLoginID gets the permission list for a specific login ID.
func GetPermissionListByLoginID(loginID, loginType string) ([]string, error) {
	iface := GetInterface()
	if iface == nil {
		return []string{}, nil
	}
	return iface.GetPermissionList(loginID, loginType)
}

// GetRoleListByLoginID gets the role list for a specific login ID.
func GetRoleListByLoginID(loginID, loginType string) ([]string, error) {
	iface := GetInterface()
	if iface == nil {
		return []string{}, nil
	}
	return iface.GetRoleList(loginID, loginType)
}

// HasPermission checks if the current user has the specified permission.
func HasPermission(c *gin.Context, permission, loginType string) bool {
	permissions, err := GetPermissionList(c, loginType)
	if err != nil {
		return false
	}
	return MatchPermission(permission, permissions)
}

// CheckPermission checks if the current user has the specified permission, aborts with 403 if not.
func CheckPermission(c *gin.Context, permission, loginType string) {
	if !HasPermission(c, permission, loginType) {
		c.AbortWithStatusJSON(http.StatusForbidden, result.Failure(c, fmt.Sprintf("缺少权限: %s", permission), 403, nil))
		return
	}
}

// HasPermissionAnd checks if the current user has all specified permissions.
func HasPermissionAnd(c *gin.Context, loginType string, permissions ...string) bool {
	perms, err := GetPermissionList(c, loginType)
	if err != nil {
		return false
	}
	return MatchPermissionsAnd(permissions, perms)
}

// CheckPermissionAnd checks if the current user has all specified permissions, aborts with 403 if not.
func CheckPermissionAnd(c *gin.Context, loginType string, permissions ...string) {
	for _, permission := range permissions {
		if !HasPermission(c, permission, loginType) {
			c.AbortWithStatusJSON(http.StatusForbidden, result.Failure(c, fmt.Sprintf("缺少权限: %s", permission), 403, nil))
			return
		}
	}
}

// HasPermissionOr checks if the current user has any of the specified permissions.
func HasPermissionOr(c *gin.Context, loginType string, permissions ...string) bool {
	perms, err := GetPermissionList(c, loginType)
	if err != nil {
		return false
	}
	return MatchPermissionsOr(permissions, perms)
}

// CheckPermissionOr checks if the current user has any of the specified permissions, aborts with 403 if not.
func CheckPermissionOr(c *gin.Context, loginType string, permissions ...string) {
	if !HasPermissionOr(c, loginType, permissions...) {
		c.AbortWithStatusJSON(http.StatusForbidden, result.Failure(c, fmt.Sprintf("缺少权限: %v", permissions), 403, nil))
		return
	}
}

// HasRole checks if the current user has the specified role.
func HasRole(c *gin.Context, role, loginType string) bool {
	roles, err := GetRoleList(c, loginType)
	if err != nil {
		return false
	}
	for _, r := range roles {
		if r == role {
			return true
		}
	}
	return false
}

// CheckRole checks if the current user has the specified role, aborts with 403 if not.
func CheckRole(c *gin.Context, role, loginType string) {
	if !HasRole(c, role, loginType) {
		c.AbortWithStatusJSON(http.StatusForbidden, result.Failure(c, fmt.Sprintf("缺少角色: %s", role), 403, nil))
		return
	}
}

// HasRoleAnd checks if the current user has all specified roles.
func HasRoleAnd(c *gin.Context, loginType string, roles ...string) bool {
	userRoles, err := GetRoleList(c, loginType)
	if err != nil {
		return false
	}
	roleSet := make(map[string]struct{}, len(userRoles))
	for _, r := range userRoles {
		roleSet[r] = struct{}{}
	}
	for _, r := range roles {
		if _, ok := roleSet[r]; !ok {
			return false
		}
	}
	return true
}

// CheckRoleAnd checks if the current user has all specified roles, aborts with 403 if not.
func CheckRoleAnd(c *gin.Context, loginType string, roles ...string) {
	for _, role := range roles {
		if !HasRole(c, role, loginType) {
			c.AbortWithStatusJSON(http.StatusForbidden, result.Failure(c, fmt.Sprintf("缺少角色: %s", role), 403, nil))
			return
		}
	}
}

// HasRoleOr checks if the current user has any of the specified roles.
func HasRoleOr(c *gin.Context, loginType string, roles ...string) bool {
	userRoles, err := GetRoleList(c, loginType)
	if err != nil {
		return false
	}
	roleSet := make(map[string]struct{}, len(userRoles))
	for _, r := range userRoles {
		roleSet[r] = struct{}{}
	}
	for _, r := range roles {
		if _, ok := roleSet[r]; ok {
			return true
		}
	}
	return false
}

// CheckRoleOr checks if the current user has any of the specified roles, aborts with 403 if not.
func CheckRoleOr(c *gin.Context, loginType string, roles ...string) {
	if !HasRoleOr(c, loginType, roles...) {
		c.AbortWithStatusJSON(http.StatusForbidden, result.Failure(c, fmt.Sprintf("缺少角色: %v", roles), 403, nil))
		return
	}
}

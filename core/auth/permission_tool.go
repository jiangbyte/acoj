package auth

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/constants"
)

// PermissionTool provides permission checking for the current user.
type PermissionToolImpl struct{}

var PermissionTool = &PermissionToolImpl{}

// GetPermissionList returns the current user's permission codes.
func (p *PermissionToolImpl) GetPermissionList(c *gin.Context, loginType string) []string {
	loginID := p.getLoginID(c, loginType)
	if loginID == "" {
		return nil
	}
	return PermissionInterface.GetPermissionList(loginID, loginType)
}

// GetRoleList returns the current user's role codes.
func (p *PermissionToolImpl) GetRoleList(c *gin.Context, loginType string) []string {
	loginID := p.getLoginID(c, loginType)
	if loginID == "" {
		return nil
	}
	return PermissionInterface.GetRoleList(loginID, loginType)
}

// HasPermission checks if the current user has a specific permission.
func (p *PermissionToolImpl) HasPermission(c *gin.Context, code, loginType string) bool {
	permissions := p.GetPermissionList(c, loginType)
	return Matcher.HasPermission(code, permissions)
}

// HasPermissionAnd checks if the user has ALL specified permissions.
func (p *PermissionToolImpl) HasPermissionAnd(c *gin.Context, codes []string, loginType string) bool {
	permissions := p.GetPermissionList(c, loginType)
	return Matcher.HasPermissionAnd(codes, permissions)
}

// HasPermissionOr checks if the user has ANY of the specified permissions.
func (p *PermissionToolImpl) HasPermissionOr(c *gin.Context, codes []string, loginType string) bool {
	permissions := p.GetPermissionList(c, loginType)
	return Matcher.HasPermissionOr(codes, permissions)
}

func (p *PermissionToolImpl) getLoginID(c *gin.Context, loginType string) string {
	if loginType == "CONSUMER" {
		return ClientAuthTool.GetLoginID(c)
	}
	return AuthTool.GetLoginID(c)
}

// DetectLoginType determines auth type from request path.
func DetectLoginType(c *gin.Context) string {
	path := c.Request.URL.Path
	if len(path) >= 8 && path[:8] == "/api/v1/c" {
		return "CONSUMER"
	}
	return "BUSINESS"
}

// PermissionRouteRegistry stores permission codes for auto-discovery.
var PermissionRouteRegistry = map[string]string{}

// RegisterPermission records a route's permission code for the scanner.
func RegisterPermission(method, path, code string) {
	key := method + ":" + path
	PermissionRouteRegistry[key] = code
}

// CheckLogin returns a Gin middleware that verifies the user is logged in.
func CheckLogin() gin.HandlerFunc {
	return func(c *gin.Context) {
		loginType := DetectLoginType(c)
		var loginID string
		if loginType == "CONSUMER" {
			loginID = ClientAuthTool.GetLoginID(c)
		} else {
			loginID = AuthTool.GetLoginID(c)
		}
		if loginID == "" {
			c.JSON(200, map[string]interface{}{
				"code":    401,
				"message": "未授权/未登录",
				"data":    nil,
				"success": false,
				"trace_id": func() string {
					if id, ok := c.Get("trace_id"); ok {
						return id.(string)
					}
					return ""
				}(),
			})
			c.Abort()
			return
		}
		c.Next()
	}
}

// CheckPermission returns a Gin middleware that verifies the user has the required permission.
func CheckPermission(code string) gin.HandlerFunc {
	return func(c *gin.Context) {
		// Register for auto-discovery
		RegisterPermission(c.Request.Method, c.FullPath(), code)

		loginType := DetectLoginType(c)
		if !PermissionTool.HasPermission(c, code, loginType) {
			// Check SUPER_ADMIN
			roles := PermissionTool.GetRoleList(c, loginType)
			for _, role := range roles {
				if role == constants.SuperAdminCode {
					c.Next()
					return
				}
			}
			c.JSON(200, map[string]interface{}{
				"code":    403,
				"message": "缺少权限: " + code,
				"data":    nil,
				"success": false,
				"trace_id": func() string {
					if id, ok := c.Get("trace_id"); ok {
						return id.(string)
					}
					return ""
				}(),
			})
			c.Abort()
			return
		}
		c.Next()
	}
}

package v1

import (
	authmw "hei-gin/core/auth/middleware"
	"hei-gin/core/result"
	"hei-gin/modules/sys/permission"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all permission routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/permission/modules
	r.GET("/api/v1/sys/permission/modules", authmw.HeiCheckPermission([]string{"sys:permission:modules"}), permListModules)
	// GET /api/v1/sys/permission/by-module
	r.GET("/api/v1/sys/permission/by-module", authmw.HeiCheckPermission([]string{"sys:permission:by-module"}), permByModule)
}

// permListModules handles GET /api/v1/sys/permission/modules
func permListModules(c *gin.Context) {
	modules := permission.ListModules(c)
	c.JSON(200, result.Success(c, modules))
}

// permByModule handles GET /api/v1/sys/permission/by-module
func permByModule(c *gin.Context) {
	module := c.Query("module")
	perms := permission.ListByModule(c, module)
	c.JSON(200, result.Success(c, perms))
}

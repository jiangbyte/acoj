package app

import (
	"github.com/gin-gonic/gin"

	"hei-gin/modules/sys/auth"
	bannerApi "hei-gin/modules/sys/banner/api/v1"
	configApi "hei-gin/modules/sys/config/api/v1"
	dictApi "hei-gin/modules/sys/dict/api/v1"
	groupApi "hei-gin/modules/sys/group/api/v1"
	homeApi "hei-gin/modules/sys/home/api/v1"
	permissionApi "hei-gin/modules/sys/permission/api/v1"
	resourceApi "hei-gin/modules/sys/resource/api/v1"
	roleApi "hei-gin/modules/sys/role/api/v1"
	userApi "hei-gin/modules/sys/user/api/v1"
)

// SetupRouters registers all application routes.
// Each module registers its own full paths directly with the engine.
func SetupRouters(r *gin.Engine) {
	r.GET("/", HealthHandler)

	// Auth module (captcha, SM2, username login/register/logout)
	auth.RegisterRoutes(r)

	// Sys modules
	bannerApi.RegisterRoutes(r)     // Banner CRUD + pagination
	resourceApi.RegisterRoutes(r)   // Module CRUD + Resource CRUD + tree
	roleApi.RegisterRoutes(r)       // Role CRUD + permission/resource grant
	userApi.RegisterRoutes(r)       // User CRUD + roles/permissions/current/menus
	groupApi.RegisterRoutes(r)      // Group CRUD + tree + union-tree
	dictApi.RegisterRoutes(r)       // Dict CRUD + tree + label lookup + cache
	configApi.RegisterRoutes(r)     // Config CRUD + batch edit + cache
	homeApi.RegisterRoutes(r)       // Home dashboard + quick actions
	permissionApi.RegisterRoutes(r) // Permission module list (Redis-only)
}

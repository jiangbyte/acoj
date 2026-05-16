package app

import (
	"github.com/gin-gonic/gin"

	clientAuth "hei-gin/modules/client/auth"
	clientSessionApi "hei-gin/modules/client/session/api/v1"
	clientUserApi "hei-gin/modules/client/user/api/v1"
	analyzeApi "hei-gin/modules/sys/analyze/api/v1"
	"hei-gin/modules/sys/auth"
	bannerApi "hei-gin/modules/sys/banner/api/v1"
	configApi "hei-gin/modules/sys/config/api/v1"
	dictApi "hei-gin/modules/sys/dict/api/v1"
	fileApi "hei-gin/modules/sys/file/api/v1"
	groupApi "hei-gin/modules/sys/group/api/v1"
	homeApi "hei-gin/modules/sys/home/api/v1"
	logApi "hei-gin/modules/sys/log/api/v1"
	noticeApi "hei-gin/modules/sys/notice/api/v1"
	orgApi "hei-gin/modules/sys/org/api/v1"
	permissionApi "hei-gin/modules/sys/permission/api/v1"
	positionApi "hei-gin/modules/sys/position/api/v1"
	resourceApi "hei-gin/modules/sys/resource/api/v1"
	roleApi "hei-gin/modules/sys/role/api/v1"
	sessionApi "hei-gin/modules/sys/session/api/v1"
	userApi "hei-gin/modules/sys/user/api/v1"
)

// SetupRouters registers all application routes.
// Each module registers its own full paths directly with the engine.
func SetupRouters(r *gin.Engine) {
	r.GET("/", HealthHandler)

	// Auth module (captcha, SM2, username login/register/logout)
	auth.RegisterRoutes(r)

	// Client modules
	clientAuth.RegisterRoutes(r)       // C端 captcha/SM2/登录/注册/登出
	clientUserApi.RegisterRoutes(r)    // C端用户 CRUD + 个人设置
	clientSessionApi.RegisterRoutes(r) // C端会话管理 (Redis CONSUMER)

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

	// Phase 2 modules
	noticeApi.RegisterRoutes(r)   // Notice CRUD
	positionApi.RegisterRoutes(r) // Position CRUD + name path enrichment
	orgApi.RegisterRoutes(r)      // Org CRUD + tree + cascading delete
	fileApi.RegisterRoutes(r)     // File upload/download/CRUD
	logApi.RegisterRoutes(r)      // Log CRUD + charts + delete-by-category
	sessionApi.RegisterRoutes(r)  // Session management (Redis)
	analyzeApi.RegisterRoutes(r)  // Dashboard stats (no permission)
}

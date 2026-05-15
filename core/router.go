package core

import (
	"github.com/gin-gonic/gin"

	clientauth "hei-gin/modules/client/auth"
	clientsession "hei-gin/modules/client/session"
	clientuser "hei-gin/modules/client/user"

	"hei-gin/modules/sys/analyze"
	sysauth "hei-gin/modules/sys/auth"
	"hei-gin/modules/sys/banner"
	"hei-gin/modules/sys/config"
	dict "hei-gin/modules/sys/dict"
	"hei-gin/modules/sys/file"
	"hei-gin/modules/sys/group"
	"hei-gin/modules/sys/home"
	"hei-gin/modules/sys/log"
	"hei-gin/modules/sys/notice"
	"hei-gin/modules/sys/org"
	"hei-gin/modules/sys/permission"
	"hei-gin/modules/sys/position"
	"hei-gin/modules/sys/resource"
	"hei-gin/modules/sys/role"
	syssessions "hei-gin/modules/sys/session"
	sysuser "hei-gin/modules/sys/user"
)

// RegisterAllRoutes imports and registers all module routes.
// Each module's RegisterRoutes function is called with a root-level router group.
func RegisterAllRoutes(r *gin.RouterGroup) {
	// Health check
	r.GET("/", HealthCheck)

	// Phase 3: Sys Auth
	sysauth.RegisterRoutes(r)

	// Phase 4: Simple CRUD modules
	banner.RegisterRoutes(r)
	config.RegisterRoutes(r)
	notice.RegisterRoutes(r)
	group.RegisterRoutes(r)
	position.RegisterRoutes(r)
	dict.RegisterRoutes(r)
	home.RegisterRoutes(r)

	// Phase 5: Complex modules
	org.RegisterRoutes(r)
	role.RegisterRoutes(r)
	resource.RegisterRoutes(r)
	resource.RegisterModuleRoutes(r)
	permission.RegisterRoutes(r)
	sysuser.RegisterRoutes(r)

	// Phase 6: Remaining modules
	log.RegisterRoutes(r)
	syssessions.RegisterRoutes(r)
	file.RegisterRoutes(r)
	analyze.RegisterRoutes(r)

	// Phase 7: Client modules
	clientauth.RegisterRoutes(r)
	clientsession.RegisterRoutes(r)
	clientuser.RegisterRoutes(r)
}

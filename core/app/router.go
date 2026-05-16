package app

import (
	"github.com/gin-gonic/gin"

	"hei-gin/modules/sys/auth"
	bannerApi "hei-gin/modules/sys/banner/api/v1"
	resourceApi "hei-gin/modules/sys/resource/api/v1"
)

// SetupRouters registers all application routes.
// Each module registers its own full paths directly with the engine.
func SetupRouters(r *gin.Engine) {
	r.GET("/", HealthHandler)

	// Auth module (captcha, SM2, username login/register/logout)
	auth.RegisterRoutes(r)

	// Banner module (CRUD + pagination)
	bannerApi.RegisterRoutes(r)

	// Resource module (Module CRUD + Resource CRUD + tree)
	resourceApi.RegisterRoutes(r)
}

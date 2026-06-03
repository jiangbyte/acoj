package app

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/registry"
)

// SetupRouters registers application routes.
// Module routes are auto-collected via init() self-registration through registry.
func SetupRouters(r *gin.Engine) {
	r.GET("/", HealthHandler)
	registry.ExecuteRoutes(r)
}

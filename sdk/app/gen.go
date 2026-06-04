package app

import (
	"github.com/gin-gonic/gin"

	"hei-gin/sdk/registry"
)

func SetupRouters(r *gin.Engine) {
	r.GET("/", HealthHandler)
	registry.ExecuteRoutes(r)
}

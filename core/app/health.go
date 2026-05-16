package app

import (
	"github.com/gin-gonic/gin"

	"hei-gin/config"
	"hei-gin/core/result"
)

// HealthCheck returns service health status.
// Matches fastapi's health_router in core/app/health.py.
func HealthCheck(c *gin.Context) {
	result.Success(c, map[string]string{
		"message": config.C.App.Name + " is running",
		"version": config.C.App.Version,
	})
}

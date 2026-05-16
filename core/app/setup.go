package app

import (
	"github.com/gin-gonic/gin"

	"hei-gin/config"
	"hei-gin/core/middleware"
)

// CreateApp creates and configures the Gin engine with global middleware and routes.
// Matches fastapi's create_app() in core/app/setup.py.
func CreateApp() *gin.Engine {
	cfg := config.C

	if cfg.App.Debug {
		gin.SetMode(gin.DebugMode)
	} else {
		gin.SetMode(gin.ReleaseMode)
	}

	r := gin.New()

	r.Use(middleware.Trace())
	r.Use(middleware.SetupCORS())
	r.Use(middleware.ExceptionHandler())
	r.Use(middleware.Auth())

	RegisterAllRoutes(&r.RouterGroup)

	return r
}

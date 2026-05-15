package core

import (
	"log"

	"github.com/gin-gonic/gin"

	"hei-gin/config"
	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/middleware"
	"hei-gin/core/utils"
	"hei-gin/modules/sys/permission"
)

func CreateApp() *gin.Engine {
	cfg := config.C

	// Gin mode
	if cfg.App.Debug {
		gin.SetMode(gin.DebugMode)
	} else {
		gin.SetMode(gin.ReleaseMode)
	}

	r := gin.New()

	// Global middleware (order matters: top = outer)
	r.Use(middleware.Trace())
	r.Use(middleware.SetupCORS())
	r.Use(middleware.Recovery())
	r.Use(middleware.Auth())

	// Register all routes
	RegisterAllRoutes(&r.RouterGroup)

	// Set GinEngine for permission cache refresh
	permission.GinEngine = r

	return r
}

// InitCore initializes all core services in the correct order.
func InitCore() error {
	// 1. Snowflake ID generator
	if err := utils.InitSnowflake(); err != nil {
		return err
	}

	// 2. Database
	if err := db.InitEnt(); err != nil {
		return err
	}

	// 3. Redis
	if err := db.InitRedis(); err != nil {
		return err
	}

	// 4. Auth tools
	auth.InitAuthTool(config.C.JWT)
	auth.InitClientAuthTool(config.C.JWT)

	return nil
}

// CloseCore cleanly shuts down all core services.
func CloseCore() {
	db.Close()
	db.CloseRedis()
}

// ScanPermissions is called after all routes are registered to cache permissions.
func ScanPermissions(r *gin.Engine) {
	auth.ScanAndCachePermissions(r)
	log.Println("[PermissionScan] completed")
}

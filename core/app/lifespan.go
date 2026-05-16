package app

import (
	"log"

	"github.com/gin-gonic/gin"

	"hei-gin/config"
	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/utils"
)

// InitCore initializes all core services in the correct order.
// Matches fastapi's lifespan startup in core/app/lifespan.py.
func InitCore() error {
	if err := utils.InitSnowflake(); err != nil {
		return err
	}
	if err := db.InitEnt(); err != nil {
		return err
	}
	if err := db.InitRedis(); err != nil {
		return err
	}
	auth.InitAuthTool(config.C.JWT)
	auth.InitClientAuthTool(config.C.JWT)
	return nil
}

// CloseCore cleanly shuts down all core services.
// Matches fastapi's lifespan shutdown in core/app/lifespan.py.
func CloseCore() {
	db.Close()
	db.CloseRedis()
}

// ScanPermissions caches all permissions after routes are registered.
func ScanPermissions(r *gin.Engine) {
	auth.ScanAndCachePermissions(r)
	log.Println("[PermissionScan] completed")
}

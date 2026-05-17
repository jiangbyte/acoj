package app

import (
	"fmt"
	"log"

	"github.com/gin-gonic/gin"

	"hei-gin/config"
	"hei-gin/core/auth"
	"hei-gin/core/captcha"
	"hei-gin/core/db"
	"hei-gin/core/middleware"
	"hei-gin/core/utils"
)

func Run() {
	// 1. Load config
	if err := config.Load("config.yaml"); err != nil {
		log.Fatalf("[APP] Failed to load config: %v", err)
	}

	// 2. Init DB
	if err := db.InitEnt(); err != nil {
		log.Fatalf("[APP] Failed to init database: %v", err)
	}

	// 3. Init Redis
	if err := db.InitRedis(); err != nil {
		log.Fatalf("[APP] Failed to init Redis: %v", err)
	}

	// 4. SM2 Init
	utils.Init(config.C.SM2.PrivateKey, config.C.SM2.PublicKey)

	// 5. Auth tool init
	auth.Init(config.C.JWT.ExpireSeconds, config.C.JWT.TokenName)
	auth.NewHeiClientAuthTool().Init(config.C.JWT.ExpireSeconds, config.C.JWT.TokenName)

	// 6. Register permission interface
	auth.RegisterInterface(&auth.HeiPermissionInterfaceImpl{})

	// 7. Init captcha
	captcha.BCaptcha.Init(db.Redis)
	captcha.CCaptcha.Init(db.Redis)

	// 8. Init auth login user provider

	// 9. Create Gin engine
	r := gin.Default()

	// 10. Global middleware
	r.Use(middleware.Trace())
	r.Use(middleware.AuthCheck())
	r.Use(middleware.Recovery())

	// 11. CORS
	r.Use(middleware.CORS())

	// 12. Setup routes
	SetupRouters(r)

	// 13. Run permission scan
	auth.RunPermissionScan()

	// 14. Start HTTP server
	addr := fmt.Sprintf("%s:%d", config.C.App.Host, config.C.App.Port)
	if err := r.Run(addr); err != nil {
		log.Fatalf("[APP] Failed to start server: %v", err)
	}
}

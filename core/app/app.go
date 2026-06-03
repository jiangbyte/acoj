package app

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/config"
	"hei-gin/core/auth"
	"hei-gin/core/captcha"
	"hei-gin/core/cron"
	"hei-gin/core/db"
	"hei-gin/core/middleware"
	"hei-gin/core/utils"
	"hei-gin/judge/registry"
	"hei-gin/judge/worker"
)

func Run() {
	// Load config
	if err := config.Load("config.yaml"); err != nil {
		log.Fatalf("[APP] Failed to load config: %v", err)
	}

	// Init DB
	if err := db.InitDB(); err != nil {
		log.Fatalf("[APP] Failed to init database: %v", err)
	}

	// Init Redis
	if err := db.InitRedis(); err != nil {
		log.Fatalf("[APP] Failed to init Redis: %v", err)
	}

	// SM2 Init
	utils.Init(config.C.SM2.PrivateKey, config.C.SM2.PublicKey)

	// Auth tool init
	auth.Init(config.C.Token.ExpireSeconds, config.C.Token.TokenName)
	auth.NewHeiClientAuthTool().Init(config.C.Token.ExpireSeconds, config.C.Token.TokenName)

	// Register permission interface
	auth.RegisterInterface(&auth.HeiPermissionInterfaceImpl{})

	// Init captcha
	captcha.BCaptcha.Init(db.Redis)
	captcha.CCaptcha.Init(db.Redis)

	// Start Judge Registry
	judgeRegistry := registry.Global()
	judgeRegistry.Start()

	// Start Judge Worker
	workerInst := worker.NewWorker()
	if err := workerInst.Start(); err != nil {
		log.Printf("[APP] Failed to start judge worker: %v", err)
	}

	// Create Gin engine
	r := gin.Default()

	// Global middleware
	// Recovery must be first so it can catch panics from all downstream middleware
	r.Use(middleware.Recovery())
	r.Use(middleware.Trace())
	// CORS
	r.Use(middleware.CORS())
	r.Use(middleware.AuthCheck())

	// Setup routes
	SetupRouters(r)

	// Run permission scan
	auth.RunPermissionScan()

	// Start background cron jobs
	cron.Start()

	// Start HTTP server with graceful shutdown
	addr := fmt.Sprintf("%s:%d", config.C.App.Host, config.C.App.Port)
	srv := &http.Server{
		Addr:    addr,
		Handler: r,
	}

	go func() {
		log.Printf("[APP] Server started on %s", addr)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("[APP] Failed to start server: %v", err)
		}
	}()

	// Wait for interrupt signal
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("[APP] Shutting down server...")

	ctx, cancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("[APP] Server forced to shutdown: %v", err)
	}

	judgeRegistry.Stop()
	workerInst.Stop()
	db.Close()
	db.CloseRedis()
	log.Println("[APP] Server exited")
}

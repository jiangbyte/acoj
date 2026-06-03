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
	"hei-gin/core/db"
	"hei-gin/core/middleware"
	"hei-gin/core/module"
	"hei-gin/core/registry"

	// Blank-import core modules to trigger init() self-registration.
	_ "hei-gin/core/auth"
	_ "hei-gin/core/captcha"
	_ "hei-gin/core/scheduler"
	_ "hei-gin/core/utils"
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

	// Init all registered modules (auth, captcha, utils, etc.)
	if err := module.InitAll(); err != nil {
		log.Fatalf("[APP] Module init failed: %v", err)
	}

	// Create Gin engine
	r := gin.Default()

	// Global middleware
	r.Use(middleware.Recovery())
	r.Use(middleware.Trace())
	r.Use(middleware.CORS())
	r.Use(middleware.AuthCheck())

	// Apply module-registered global middlewares
	registry.ApplyMiddlewares(r)

	// Setup routes
	SetupRouters(r)

	// Start background modules (cron jobs, etc.)
	module.StartAll()

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

	// Stop all modules in reverse order
	module.StopAll()

	db.Close()
	db.CloseRedis()
	log.Println("[APP] Server exited")
}

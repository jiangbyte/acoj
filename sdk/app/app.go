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

	"hei-gin/sdk/config"
	"hei-gin/sdk/db"
	"hei-gin/sdk/middleware"
	"hei-gin/sdk/module"
	"hei-gin/sdk/registry"
	"hei-gin/sdk/ws"

	_ "hei-gin/sdk/auth"
	_ "hei-gin/sdk/captcha"
	_ "hei-gin/sdk/scheduler"
	_ "hei-gin/sdk/utils"
)

func Run() {
	if err := config.FindAndLoad(); err != nil {
		log.Fatalf("[APP] Failed to load config: %v", err)
	}

	if err := db.InitDB(); err != nil {
		log.Fatalf("[APP] Failed to init database: %v", err)
	}

	if err := db.InitRedis(); err != nil {
		log.Fatalf("[APP] Failed to init Redis: %v", err)
	}

	ws.GlobalCrossHub = ws.NewCrossHub(ws.GlobalHub, db.Redis)

	if err := module.InitAll(); err != nil {
		log.Fatalf("[APP] Module init failed: %v", err)
	}

	r := gin.Default()

	r.Use(middleware.Recovery())
	r.Use(middleware.Trace())
	r.Use(middleware.CORS())
	r.Use(middleware.AuthCheck())

	registry.ApplyMiddlewares(r)

	SetupRouters(r)

	module.StartAll()

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

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("[APP] Shutting down server...")

	ctx, cancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("[APP] Server forced to shutdown: %v", err)
	}

	module.StopAll()

	db.Close()
	ws.GlobalCrossHub.Close()
	db.CloseRedis()
	log.Println("[APP] Server exited")
}

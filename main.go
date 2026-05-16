package main

import (
	"fmt"
	"log"

	"hei-gin/config"
	"hei-gin/core"
)

func main() {
	// Load config
	if err := config.Load("config.yaml"); err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	// Init core services
	if err := core.InitCore(); err != nil {
		log.Fatalf("Failed to init core: %v", err)
	}
	defer core.CloseCore()

	// Create app
	app := core.CreateApp()

	// Cache permissions in Redis
	core.ScanPermissions(app)

	log.Printf("[Main] %s v%s starting...", config.C.App.Name, config.C.App.Version)

	addr := fmt.Sprintf("%s:%d", config.C.App.Host, config.C.App.Port)
	if err := app.Run(addr); err != nil {
		log.Fatalf("Server failed: %v", err)
	}
}

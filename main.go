package main

import (
	"fmt"
	"log"

	"hei-gin/config"
	appapi "hei-gin/core/app"
)

func main() {
	// Load config
	if err := config.Load("config.yaml"); err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	// Init core services
	if err := appapi.InitCore(); err != nil {
		log.Fatalf("Failed to init core: %v", err)
	}
	defer appapi.CloseCore()

	// Create engine
	engine := appapi.CreateApp()

	// Cache permissions in Redis
	appapi.ScanPermissions(engine)

	log.Printf("[Main] %s v%s starting...", config.C.App.Name, config.C.App.Version)

	addr := fmt.Sprintf("%s:%d", config.C.App.Host, config.C.App.Port)
	if err := engine.Run(addr); err != nil {
		log.Fatalf("Server failed: %v", err)
	}
}

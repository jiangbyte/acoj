package main

import (
	"flag"
	"fmt"
	"log"

	"hei-gin/config"
	"hei-gin/core/db"

	// Blank-import modules to trigger model + seed self-registration via init()
	_ "hei-gin/modules/client/user"
	_ "hei-gin/modules/sys/banner"
	_ "hei-gin/modules/sys/config"
	_ "hei-gin/modules/sys/dict"
	_ "hei-gin/modules/sys/file"
	_ "hei-gin/modules/sys/group"
	_ "hei-gin/modules/sys/home"
	_ "hei-gin/modules/sys/log"
	_ "hei-gin/modules/sys/notice"
	_ "hei-gin/modules/sys/message"
	_ "hei-gin/modules/sys/org"
	_ "hei-gin/modules/sys/position"
	_ "hei-gin/modules/sys/resource"
	_ "hei-gin/modules/sys/role"
	_ "hei-gin/modules/sys/user"
	_ "hei-gin/modules/client/message"
)

func main() {
	skipSeed := flag.Bool("skip-seed", false, "skip seeding initial data")
	flag.Parse()

	// 1. Load config
	if err := config.Load("config.yaml"); err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	// 2. Connect to database
	if err := db.InitDB(); err != nil {
		log.Fatalf("failed to init database: %v", err)
	}
	defer db.Close()

	// 3. AutoMigrate all registered models
	models := db.GetModels()
	if len(models) == 0 {
		fmt.Println("No models registered, skipping migration")
		return
	}
	if err := db.DB.AutoMigrate(models...); err != nil {
		log.Fatalf("failed to apply migration: %v", err)
	}
	fmt.Println("✓ Migration applied successfully")

	// 4. Run seeds
	if !*skipSeed {
		if err := db.RunSeeds(); err != nil {
			log.Fatalf("failed to run seeds: %v", err)
		}
		fmt.Println("✓ Seeds applied successfully")
	}
}

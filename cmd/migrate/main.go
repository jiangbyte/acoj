package main

import (
	"flag"
	"fmt"
	"log"

	"hei-gin/sdk/config"
	"hei-gin/sdk/db"

	// Blank-import plugins to trigger model + seed self-registration via init()
	_ "hei-gin/plugins/plugin-sys/user"
	_ "hei-gin/plugins/plugin-sys/banner"
	_ "hei-gin/plugins/plugin-sys/config"
	_ "hei-gin/plugins/plugin-sys/dict"
	_ "hei-gin/plugins/plugin-sys/file"
	_ "hei-gin/plugins/plugin-sys/group"
	_ "hei-gin/plugins/plugin-sys/home"
	_ "hei-gin/plugins/plugin-sys/log"
	_ "hei-gin/plugins/plugin-sys/notice"
	_ "hei-gin/plugins/plugin-im/sys_message"
	_ "hei-gin/plugins/plugin-sys/org"
	_ "hei-gin/plugins/plugin-sys/position"
	_ "hei-gin/plugins/plugin-sys/resource"
	_ "hei-gin/plugins/plugin-sys/role"
	_ "hei-gin/plugins/plugin-client/user"
	_ "hei-gin/plugins/plugin-im/client_message"
	_ "hei-gin/plugins/plugin-im/group"
)

func main() {
	skipSeed := flag.Bool("skip-seed", false, "skip seeding initial data")
	flag.Parse()

	if err := config.FindAndLoad(); err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	if err := db.InitDB(); err != nil {
		log.Fatalf("failed to init database: %v", err)
	}
	defer db.Close()

	models := db.GetModels()
	if len(models) == 0 {
		fmt.Println("No models registered, skipping migration")
		return
	}
	if err := db.DB.AutoMigrate(models...); err != nil {
		log.Fatalf("failed to apply migration: %v", err)
	}
	fmt.Println("✓ Migration applied successfully")

	if !*skipSeed {
		if err := db.RunSeeds(); err != nil {
			log.Fatalf("failed to run seeds: %v", err)
		}
		fmt.Println("✓ Seeds applied successfully")
	}
}

package main

/**
  core/db/ent.go

  - 移除了启动时自动执行 Schema.Create 的逻辑
  - 项目启动只连接数据库，不再执行任何迁移

  cmd/migrate/main.go — 新迁移工具

  四种用法：

  # 1. 预览 SQL（不执行）
  go run ./cmd/migrate -dry-run

  # 2. 导出 SQL 到文件，审查后手动执行
  go run ./cmd/migrate -out migrate.sql

  # 3. 直接应用到数据库并创建 admin 账户
  go run ./cmd/migrate

  # 4. 如需删除已废弃的列/索引
  go run ./cmd/migrate -drop-column -drop-index

  # 跳过种子数据（仅执行 Schema 迁移）
  go run ./cmd/migrate -skip-seed

*/

import (
	"context"
	"flag"
	"fmt"
	"log"
	"os"
	"time"

	"entgo.io/ent/dialect/sql/schema"
	"golang.org/x/crypto/bcrypt"

	"hei-gin/config"
	"hei-gin/core/db"
	"hei-gin/ent/gen/sysuser"
)

func main() {
	dryRun := flag.Bool("dry-run", false, "print SQL to stdout without executing")
	output := flag.String("out", "", "write SQL to file (implies -dry-run)")
	dropCol := flag.Bool("drop-column", false, "drop columns that no longer exist in schema")
	dropIdx := flag.Bool("drop-index", false, "drop indexes that no longer exist in schema")
	skipSeed := flag.Bool("skip-seed", false, "skip seeding initial data")
	flag.Parse()

	// 1. Load config
	if err := config.Load("config.yaml"); err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	// 2. Connect to database
	if err := db.InitEnt(); err != nil {
		log.Fatalf("failed to init database: %v", err)
	}
	defer db.Close()

	// 3. Build migration options
	opts := []schema.MigrateOption{
		schema.WithForeignKeys(false),
	}
	if *dropCol {
		opts = append(opts, schema.WithDropColumn(true))
	}
	if *dropIdx {
		opts = append(opts, schema.WithDropIndex(true))
	}

	// 4. Execute or dry-run
	switch {
	case *output != "":
		f, err := os.Create(*output)
		if err != nil {
			log.Fatalf("failed to create output file: %v", err)
		}
		defer f.Close()
		ctx := context.Background()
		if err := db.Client.Schema.WriteTo(ctx, f, opts...); err != nil {
			log.Fatalf("failed to generate migration SQL: %v", err)
		}
		fmt.Printf("Migration SQL written to %s\n", *output)

	case *dryRun:
		ctx := context.Background()
		if err := db.Client.Schema.WriteTo(ctx, os.Stdout, opts...); err != nil {
			log.Fatalf("failed to generate migration SQL: %v", err)
		}

	default:
		ctx := context.Background()
		if err := db.Client.Schema.Create(ctx, opts...); err != nil {
			log.Fatalf("failed to apply migration: %v", err)
		}
		fmt.Println("Migration applied successfully")

		if !*skipSeed {
			seedAdminUser(ctx)
		}
	}
}

// seedAdminUser creates the initial admin user if not exists.
func seedAdminUser(ctx context.Context) {
	exists, err := db.Client.SysUser.Query().Where(sysuser.UsernameEQ("admin")).Exist(ctx)
	if err != nil {
		log.Fatalf("failed to check admin user: %v", err)
	}

	if !exists {
		now := time.Now()
		hashed, err := bcrypt.GenerateFromPassword([]byte("123456"), bcrypt.DefaultCost)
		if err != nil {
			log.Fatalf("failed to hash password: %v", err)
		}

		// ID is auto-generated via UUIDMixin (UUID v7)
		_, err = db.Client.SysUser.Create().
			SetUsername("admin").
			SetPassword(string(hashed)).
			SetNickname("超级管理员").
			SetStatus("ACTIVE").
			SetLoginCount(0).
			SetCreatedAt(now).
			SetUpdatedAt(now).
			Save(ctx)
		if err != nil {
			log.Fatalf("failed to create admin user: %v", err)
		}
		fmt.Println("[Seed] Admin user created (password: 123456)")
	} else {
		fmt.Println("[Seed] Admin user already exists, skipped")
	}
}

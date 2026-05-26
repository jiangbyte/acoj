package main

import (
	"context"
	"flag"
	"fmt"
	"log"
	"time"

	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"

	"hei-gin/config"
	"hei-gin/core/db"
	userModel "hei-gin/modules/sys/user"
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

	// 3. AutoMigrate all tables
	if err := autoMigrate(db.DB); err != nil {
		log.Fatalf("failed to apply migration: %v", err)
	}
	fmt.Println("Migration applied successfully")

	if !*skipSeed {
		seedAdminUser(context.Background())
	}
}

func autoMigrate(db *gorm.DB) error {
	return db.AutoMigrate(
		&userModel.SysUser{},
		&userModel.RelUserRole{},
		&userModel.RelUserPermission{},
		&userModel.RelRolePermission{},
		&userModel.RelRoleResource{},
	)
}

func seedAdminUser(ctx context.Context) {
	var count int64
	db.DB.Model(&userModel.SysUser{}).Where("username = ?", "admin").Count(&count)
	if count > 0 {
		fmt.Println("[Seed] Admin user already exists, skipped")
		return
	}

	now := time.Now()
	hashed, err := bcrypt.GenerateFromPassword([]byte("123456"), bcrypt.DefaultCost)
	if err != nil {
		log.Fatalf("failed to hash password: %v", err)
	}

	user := userModel.SysUser{
		Username:   strPtr("admin"),
		Password:   strPtr(string(hashed)),
		Nickname:   strPtr("超管"),
		Status:     "ACTIVE",
		LoginCount: 0,
		CreatedAt:  &now,
		UpdatedAt:  &now,
	}
	if err := db.DB.Create(&user).Error; err != nil {
		log.Fatalf("failed to create admin user: %v", err)
	}
	fmt.Println("[Seed] Admin user created (password: 123456)")
}

func strPtr(s string) *string { return &s }

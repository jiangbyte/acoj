package db

import (
	"fmt"
	"log"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"

	"hei-gin/config"
)

var DB *gorm.DB

func InitDB() error {
	cfg := config.C.DB
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=utf8mb4&parseTime=True&loc=Local",
		cfg.User, cfg.Password, cfg.Host, cfg.Port, cfg.Database)

	var err error
	DB, err = gorm.Open(mysql.Open(dsn), &gorm.Config{
		SkipDefaultTransaction: true,
		PrepareStmt:            true,
	})
	if err != nil {
		return fmt.Errorf("failed to open database: %w", err)
	}

	sqlDB, err := DB.DB()
	if err != nil {
		return fmt.Errorf("failed to get sql.DB: %w", err)
	}

	sqlDB.SetMaxOpenConns(cfg.PoolSize + cfg.MaxOverflow)
	sqlDB.SetMaxIdleConns(cfg.PoolSize)
	sqlDB.SetConnMaxLifetime(time.Duration(cfg.PoolRecycle) * time.Second)
	sqlDB.SetConnMaxIdleTime(time.Duration(cfg.PoolRecycle) * time.Second)

	if err := sqlDB.Ping(); err != nil {
		return fmt.Errorf("database ping failed: %w", err)
	}
	log.Println("[Database] MySQL connection verified")
	return nil
}

func Close() {
	if DB != nil {
		sqlDB, err := DB.DB()
		if err == nil {
			sqlDB.Close()
		}
		DB = nil
	}
}

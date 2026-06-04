package db

import (
	"fmt"
	"log"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"

	"hei-gin/sdk/config"
)

var DB *gorm.DB

func InitDB() error {
	cfg := config.C.DB
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=utf8mb4&parseTime=True&loc=Local",
		cfg.User, cfg.Password, cfg.Host, cfg.Port, cfg.Database)

	var err error
	DB, err = gorm.Open(mysql.Open(dsn), &gorm.Config{
		// Auto-transaction enabled: GORM wraps single Create/Update/Delete in a transaction.
		// Explicit transactions are used for multi-step operations.
		PrepareStmt: true,
	})
	if err != nil {
		return fmt.Errorf("failed to open database: %w", err)
	}

	sqlDB, err := DB.DB()
	if err != nil {
		return fmt.Errorf("failed to get sql.DB: %w", err)
	}

	// Connection pool configuration
	sqlDB.SetMaxOpenConns(cfg.PoolSize + cfg.MaxOverflow)
	sqlDB.SetMaxIdleConns(cfg.PoolSize)

	// Set connection max lifetime with a hard upper bound to prevent connections
	// living indefinitely. Use PoolRecycle if configured, otherwise default to 1 hour.
	maxLifetime := time.Duration(cfg.PoolRecycle) * time.Second
	if maxLifetime <= 0 || maxLifetime > 1*time.Hour {
		maxLifetime = 1 * time.Hour
	}
	sqlDB.SetConnMaxLifetime(maxLifetime)

	// Set idle timeout to half of max lifetime to cycle idle connections faster
	sqlDB.SetConnMaxIdleTime(maxLifetime / 2)

	if err := sqlDB.Ping(); err != nil {
		sqlDB.Close()
		return fmt.Errorf("database ping failed: %w", err)
	}

	log.Printf("[Database] MySQL connection verified, max_conns=%d, max_lifetime=%v",
		cfg.PoolSize+cfg.MaxOverflow, maxLifetime)

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

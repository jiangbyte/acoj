package database

import (
	"fmt"
	"log"
	"time"

	"similarity-service/internal/config"

	"github.com/zeromicro/go-zero/core/logx"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

type MySQLManager struct {
	DB *gorm.DB
}

func NewMySQLManager(c config.MySQLConfig) (*MySQLManager, error) {
	dsn := buildDSN(c)

	db, err := connectMySQL(dsn, c)
	if err != nil {
		return nil, err
	}

	sqlDB, err := db.DB()
	if err != nil {
		return nil, err
	}

	// 优化连接池配置以适应高并发
	sqlDB.SetMaxIdleConns(50)                 // 增加最大空闲连接数
	sqlDB.SetMaxOpenConns(200)                // 增加最大打开连接数
	sqlDB.SetConnMaxLifetime(5 * time.Minute) // 减少连接最大生存时间
	sqlDB.SetConnMaxIdleTime(2 * time.Minute) // 设置连接最大空闲时间

	// 测试连接
	if err := sqlDB.Ping(); err != nil {
		return nil, fmt.Errorf("数据库连接测试失败: %v", err)
	}

	logx.Info("MySQL 连接成功")
	return &MySQLManager{DB: db}, nil
}

func buildDSN(c config.MySQLConfig) string {
	return c.Username + ":" + c.Password + "@tcp(" + c.Host + ")/" + c.Database +
		"?charset=" + c.Charset + "&parseTime=" + boolToString(c.ParseTime) + "&loc=" + c.Loc
}

func connectMySQL(dsn string, c config.MySQLConfig) (*gorm.DB, error) {
	newLogger := logger.New(
		log.New(log.Writer(), "\r\n", log.LstdFlags),
		logger.Config{
			SlowThreshold:             time.Second,
			LogLevel:                  logger.Silent,
			IgnoreRecordNotFoundError: true,
			Colorful:                  false,
		},
	)

	db, err := gorm.Open(mysql.Open(dsn), &gorm.Config{
		Logger: newLogger,
	})
	if err != nil {
		return nil, err
	}

	sqlDB, err := db.DB()
	if err != nil {
		return nil, err
	}

	sqlDB.SetMaxIdleConns(c.MaxIdleConns)
	sqlDB.SetMaxOpenConns(c.MaxOpenConns)

	if maxLifetime, err := time.ParseDuration(c.ConnMaxLifetime); err == nil {
		sqlDB.SetConnMaxLifetime(maxLifetime)
	}

	return db, nil
}

func boolToString(b bool) string {
	if b {
		return "True"
	}
	return "False"
}

func (m *MySQLManager) IsReady() bool {
	return m.DB != nil
}

func (m *MySQLManager) SafeDB() (*gorm.DB, error) {
	if m.DB == nil {
		return nil, fmt.Errorf("数据库未初始化")
	}
	return m.DB, nil
}

func (m *MySQLManager) Close() error {
	if m.DB != nil {
		sqlDB, err := m.DB.DB()
		if err != nil {
			return err
		}
		return sqlDB.Close()
	}
	return nil
}

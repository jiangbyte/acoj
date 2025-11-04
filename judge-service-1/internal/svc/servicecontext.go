package svc

import (
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/database"
	repository2 "judge-service/internal/database/repository"
	"judge-service/internal/initializer"

	"github.com/streadway/amqp"
	"gorm.io/gorm"
)

type ServiceContext struct {
	Config      config.Config
	Initializer *initializer.InitializerManager
}

func NewServiceContext(c config.Config) *ServiceContext {
	initializer := initializer.NewInitializerManager(c)

	if err := initializer.Initialize(); err != nil {
		panic(err)
	}

	return &ServiceContext{
		Config:      c,
		Initializer: initializer,
	}
}

// 新增 Redis 相关方法
func (s *ServiceContext) Redis() *database.RedisManager {
	return s.Initializer.GetRedisManager()
}

func (s *ServiceContext) IsRedisReady() bool {
	return s.Initializer.IsRedisReady()
}

// 便捷方法
func (s *ServiceContext) DB() *gorm.DB {
	if s.Initializer.GetMySQLManager() != nil {
		return s.Initializer.GetMySQLManager().DB
	}
	return nil
}

func (s *ServiceContext) RabbitMQ() *amqp.Connection {
	if s.Initializer.GetRabbitMQManager() != nil {
		return s.Initializer.GetRabbitMQManager().Conn
	}
	return nil
}

func (s *ServiceContext) CommonChannel() *amqp.Channel {
	if s.Initializer.GetRabbitMQManager() != nil {
		return s.Initializer.GetRabbitMQManager().CommonChannel
	}
	return nil
}

func (s *ServiceContext) TestCaseRepo() repository2.TestCaseRepository {
	// return s.Initializer.GetTestCaseRepo()
	baseRepo := s.Initializer.GetTestCaseRepo()
	redisMgr := s.Initializer.GetRedisManager()

	// 如果 Redis 可用，返回带缓存的版本
	if redisMgr != nil && redisMgr.IsReady() {
		return repository2.NewTestCaseRepositoryWithCache(baseRepo, redisMgr)
	}

	// 否则返回基础版本
	return baseRepo
}
func (s *ServiceContext) JudgeCaseRepo() repository2.JudgeCaseRepository {
	return s.Initializer.GetJudgeCaseRepo()
}

func (s *ServiceContext) IsDBReady() bool {
	return s.Initializer.IsDBReady()
}

func (s *ServiceContext) SafeDB() (*gorm.DB, error) {
	if s.Initializer.GetMySQLManager() != nil {
		return s.Initializer.GetMySQLManager().SafeDB()
	}
	return nil, fmt.Errorf("数据库未初始化")
}

func (s *ServiceContext) Close() error {
	return s.Initializer.Close()
}

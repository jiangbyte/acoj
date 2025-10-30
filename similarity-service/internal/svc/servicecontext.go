package svc

import (
	"fmt"
	"github.com/streadway/amqp"
	"gorm.io/gorm"
	"judge-service/internal/config"
	repository2 "judge-service/internal/database/repository"
	"judge-service/internal/initializer"
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
	return s.Initializer.GetTestCaseRepo()
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

package initializer

import (
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/database"
	repository2 "judge-service/internal/database/repository"
	"judge-service/internal/mq"
	"judge-service/internal/nacos"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

type InitializerManager struct {
	config              config.Config
	mysqlManager        *database.MySQLManager
	redisManager        *database.RedisManager // 新增
	rabbitMQManager     *mq.RabbitMQManager
	serviceRegistry     *nacos.ServiceRegistryManager
	testCaseRepo        repository2.TestCaseRepository
	judgeCaseRepository repository2.JudgeCaseRepository
}

func NewInitializerManager(c config.Config) *InitializerManager {
	return &InitializerManager{
		config: c,
	}
}

func (im *InitializerManager) Initialize() error {
	// 初始化 RabbitMQ（同步，因为其他组件依赖它）
	if err := im.initRabbitMQ(); err != nil {
		return err
	}

	// 初始化服务注册（同步）
	im.initServiceRegistry()

	// 异步初始化 MySQL
	go im.initMySQLWithRetry()

	go im.initRedisWithRetry() // 新增

	return nil
}

func (im *InitializerManager) initRabbitMQ() error {
	rabbitMQManager, err := mq.NewRabbitMQManager(im.config.RabbitMQ)
	if err != nil {
		logx.Errorf("无法连接到 RabbitMQ: %v", err)
		return err
	}
	im.rabbitMQManager = rabbitMQManager
	return nil
}

func (im *InitializerManager) initServiceRegistry() {
	im.serviceRegistry = nacos.NewServiceRegistryManager(im.config.Nacos, im.config)
	if err := im.serviceRegistry.Register(); err != nil {
		logx.Errorf("服务注册初始化失败: %v", err)
	}
}

func (im *InitializerManager) initMySQLWithRetry() {
	maxRetries := 5
	retryInterval := time.Second * 5

	for i := 0; i < maxRetries; i++ {
		mysqlManager, err := database.NewMySQLManager(im.config.MySQL)
		if err != nil {
			logx.Errorf("初始化 MySQL 失败 (尝试 %d/%d): %v", i+1, maxRetries, err)

			if i == maxRetries-1 {
				logx.Error("已达到最大重试次数，MySQL 连接失败")
				return
			}

			logx.Infof("%v 后重试...", retryInterval)
			time.Sleep(retryInterval)
			continue
		}

		im.mysqlManager = mysqlManager
		im.testCaseRepo = repository2.NewTestCaseRepository(mysqlManager.DB)
		im.judgeCaseRepository = repository2.NewJudgeCaseRepository(mysqlManager.DB)
		logx.Info("MySQL 连接成功")
		return
	}
}

// 新增 Redis 初始化方法
func (im *InitializerManager) initRedisWithRetry() {
	maxRetries := 5
	retryInterval := time.Second * 5

	for i := 0; i < maxRetries; i++ {
		redisManager, err := database.NewRedisManager(im.config.Redis)
		if err != nil {
			logx.Errorf("初始化 Redis 失败 (尝试 %d/%d): %v", i+1, maxRetries, err)

			if i == maxRetries-1 {
				logx.Error("已达到最大重试次数，Redis 连接失败")
				return
			}

			logx.Infof("%v 后重试...", retryInterval)
			time.Sleep(retryInterval)
			continue
		}

		im.redisManager = redisManager
		logx.Info("Redis 连接成功")
		return
	}
}

func (im *InitializerManager) Close() error {
	var errs []error

	if im.serviceRegistry != nil {
		if err := im.serviceRegistry.Deregister(); err != nil {
			errs = append(errs, err)
		}
	}

	if im.rabbitMQManager != nil {
		if err := im.rabbitMQManager.Close(); err != nil {
			errs = append(errs, err)
		}
	}

	if im.mysqlManager != nil {
		if err := im.mysqlManager.Close(); err != nil {
			errs = append(errs, err)
		}
	}
	// 新增 Redis 关闭
	if im.redisManager != nil {
		if err := im.redisManager.Close(); err != nil {
			errs = append(errs, err)
		}
	}
	if len(errs) > 0 {
		return fmt.Errorf("关闭资源时发生错误: %v", errs)
	}
	return nil
}

// Getters
func (im *InitializerManager) GetMySQLManager() *database.MySQLManager {
	return im.mysqlManager
}

func (im *InitializerManager) GetRabbitMQManager() *mq.RabbitMQManager {
	return im.rabbitMQManager
}

func (im *InitializerManager) GetTestCaseRepo() repository2.TestCaseRepository {
	return im.testCaseRepo
}

func (im *InitializerManager) GetJudgeCaseRepo() repository2.JudgeCaseRepository {
	return im.judgeCaseRepository
}

func (im *InitializerManager) GetServiceReRegistry() *nacos.ServiceRegistryManager {
	return im.serviceRegistry
}

func (im *InitializerManager) IsDBReady() bool {
	return im.mysqlManager != nil && im.mysqlManager.IsReady()
}

// 新增 GetRedisManager 方法
func (im *InitializerManager) GetRedisManager() *database.RedisManager {
	return im.redisManager
}

func (im *InitializerManager) IsRedisReady() bool {
	return im.redisManager != nil && im.redisManager.IsReady()
}

package svc

import (
	"judge-service/internal/config"
	"judge-service/internal/nacos"

	"github.com/streadway/amqp"
	"github.com/zeromicro/go-zero/core/logx"
)

type ServiceContext struct {
	Config   config.Config
	RabbitMQ *amqp.Connection
	// 两个通道分别处理题目和题集
	ProblemChannel    *amqp.Channel
	ProblemSetChannel *amqp.Channel
	CommonChannel     *amqp.Channel
	ServiceRegistry   *nacos.ServiceRegistry // 服务注册器
}

func NewServiceContext(c config.Config) *ServiceContext {
	// 创建连接
	conn, err := amqp.DialConfig(c.RabbitMQ.Host, amqp.Config{
		Vhost: c.RabbitMQ.VirtualHost,
	})
	if err != nil {
		logx.Errorf("无法连接到 RabbitMQ: %v", err)
		panic(err)
	}

	// // 创建题目通道
	// problemCh, err := conn.Channel()
	// if err != nil {
	// 	logx.Errorf("无法打开题目通道: %v", err)
	// 	panic(err)
	// }

	// // 创建题集通道
	// problemSetCh, err := conn.Channel()
	// if err != nil {
	// 	logx.Errorf("无法打开题集通道: %v", err)
	// 	panic(err)
	// }

	// 常规通道
	commonCh, err := conn.Channel()
	if err != nil {
		logx.Errorf("无法打开通道: %v", err)
		panic(err)
	}

	// 创建服务注册器
	var serviceRegistry *nacos.ServiceRegistry
	if c.Nacos.ServerAddr != "" {
		registry, err := nacos.NewServiceRegistry(c.Nacos, c)
		if err != nil {
			logx.Errorf("Failed to create service registry: %v", err)
			// 不 panic，让服务继续运行
		} else {
			serviceRegistry = registry

			// 注册服务到 Nacos
			if err := serviceRegistry.Register(); err != nil {
				logx.Errorf("Failed to register service: %v", err)
			} else {
				logx.Info("Service registered to Nacos successfully")

				// 启动健康检查
				go serviceRegistry.HealthCheck()
			}
		}
	}

	return &ServiceContext{
		Config:   c,
		RabbitMQ: conn,

		// ProblemChannel:    problemCh,
		// ProblemSetChannel: problemSetCh,
		CommonChannel: commonCh,
		ServiceRegistry: serviceRegistry,
	}
}

package svc

import (
	"github.com/streadway/amqp"
	"github.com/zeromicro/go-zero/core/logx"
	"judge-service/internal/config"
)

type ServiceContext struct {
	Config   config.Config
	RabbitMQ *amqp.Connection
	// 两个通道分别处理题目和题集
	ProblemChannel    *amqp.Channel
	ProblemSetChannel *amqp.Channel
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

	// 创建题目通道
	problemCh, err := conn.Channel()
	if err != nil {
		logx.Errorf("无法打开题目通道: %v", err)
		panic(err)
	}

	// 创建题集通道
	problemSetCh, err := conn.Channel()
	if err != nil {
		logx.Errorf("无法打开题集通道: %v", err)
		panic(err)
	}

	return &ServiceContext{
		Config:   c,
		RabbitMQ: conn,

		ProblemChannel:    problemCh,
		ProblemSetChannel: problemSetCh,
	}
}

package mq

import (
	"fmt"
	"judge-service/internal/config"

	"github.com/streadway/amqp"
	"github.com/zeromicro/go-zero/core/logx"
)

type RabbitMQManager struct {
	Conn          *amqp.Connection
	CommonChannel *amqp.Channel
	config        config.RabbitMQConfig
}

func NewRabbitMQManager(c config.RabbitMQConfig) (*RabbitMQManager, error) {
	conn, err := amqp.DialConfig(c.Host, amqp.Config{
		Vhost: c.VirtualHost,
	})
	if err != nil {
		return nil, err
	}

	commonCh, err := conn.Channel()
	if err != nil {
		conn.Close()
		return nil, err
	}

	logx.Info("RabbitMQ 连接成功")
	return &RabbitMQManager{
		Conn:          conn,
		CommonChannel: commonCh,
		config:        c,
	}, nil
}

func (r *RabbitMQManager) IsReady() bool {
	return r.Conn != nil && !r.Conn.IsClosed()
}

func (r *RabbitMQManager) Close() error {
	if r.CommonChannel != nil {
		r.CommonChannel.Close()
	}
	if r.Conn != nil {
		return r.Conn.Close()
	}
	return nil
}

func (r *RabbitMQManager) GetChannel() (*amqp.Channel, error) {
	if !r.IsReady() {
		return nil, fmt.Errorf("RabbitMQ 连接不可用")
	}
	return r.Conn.Channel()
}

package logic

import (
	"context"
	"encoding/json"
	"judge-service/internal/core"
	"judge-service/internal/mq"
	"judge-service/internal/svc"
	"sync"
	"time"

	"github.com/streadway/amqp"
	"github.com/zeromicro/go-zero/core/logx"
)

type CommonLogic struct {
	ctx       context.Context
	svcCtx    *svc.ServiceContext
	semaphore chan struct{} // 控制并发数量的信号量
}

func NewCommonLogic(ctx context.Context, svcCtx *svc.ServiceContext) *CommonLogic {
	return &CommonLogic{
		ctx:       ctx,
		svcCtx:    svcCtx,
		semaphore: make(chan struct{}, 20), // 控制并发数量
	}
}

func (l *CommonLogic) StartConsumer() {
	CommonChannel := l.svcCtx.Initializer.GetRabbitMQManager().CommonChannel

	_, err := CommonChannel.QueueDeclare(
		l.svcCtx.Config.RabbitMQ.Common.Queue,
		true, false, false, false, nil,
	)
	if err != nil {
		logx.Errorf("无法声明问题队列: %v", err)
		return
	}

	// 增加预取计数，提高并发度
	err = CommonChannel.Qos(
		50, // 增加预取计数
		0,
		false,
	)
	if err != nil {
		logx.Errorf("设置Qos失败: %v", err)
		return
	}

	msgs, err := CommonChannel.Consume(
		l.svcCtx.Config.RabbitMQ.Common.Queue,
		"", false, false, false, false, nil,
	)
	if err != nil {
		logx.Errorf("注册题目消费者失败: %v", err)
		return
	}

	logx.Info("题目消费者已成功启动")

	var wg sync.WaitGroup

	for d := range msgs {
		wg.Add(1)
		l.semaphore <- struct{}{} // 获取信号量

		go func(delivery amqp.Delivery) {
			defer wg.Done()
			defer func() { <-l.semaphore }() // 释放信号量

			l.processMessage(delivery)
		}(d)
	}

	wg.Wait()
}

// 为每个消息创建独立的上下文，避免上下文取消影响其他处理
func (l *CommonLogic) processMessage(delivery amqp.Delivery) {
	// 为每个消息创建独立的上下文
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Minute)
	defer cancel()

	defer func() {
		if err := delivery.Ack(false); err != nil {
			logx.Errorf("未能确认题目消息: %v", err)
		}
	}()

	var judgeSubmit mq.JudgeRequest
	if err := json.Unmarshal(delivery.Body, &judgeSubmit); err != nil {
		logx.Errorf("解码 JSON 出错: %v", err)
		return
	}

	// 使用新的上下文创建工作空间
	workspace, result := core.NewWorkspace(ctx, l.svcCtx.Config, judgeSubmit, l.svcCtx)
	if result != nil {
		if err := l.sendResultToMQ(result); err != nil {
			logx.Errorf("发送结果到MQ失败: %v", err)
		}
		return
	}

	// 程序执行
	judgeResponse := workspace.Execute()

	// 发送结果
	if err := l.sendResultToMQ(judgeResponse); err != nil {
		logx.Errorf("发送结果到MQ失败: %v", err)
	}

	// 清理工作空间
	if err := workspace.Cleanup(); err != nil {
		logx.Errorf("删除工作空间失败: %v", err)
	}
}

func (l *CommonLogic) sendResultToMQ(result *mq.JudgeResponse) error {
	body, err := json.Marshal(result)
	if err != nil {
		return err
	}

	exchange := l.svcCtx.Config.RabbitMQ.Common.ResultExchange
	routingKey := l.svcCtx.Config.RabbitMQ.Common.ResultRoutingKey

	return l.svcCtx.Initializer.GetRabbitMQManager().CommonChannel.Publish(
		exchange,
		routingKey,
		false,
		false,
		amqp.Publishing{
			ContentType: "application/json",
			Body:        body,
			Timestamp:   time.Now(),
		},
	)
}

package logic

import (
	"context"
	"encoding/json"
	"judge-service/internal/dto"
	"judge-service/internal/judge"
	"judge-service/internal/svc"
	"sync"
	"time"

	"github.com/streadway/amqp"
	"github.com/zeromicro/go-zero/core/logx"
)

type CommonLogic struct {
	ctx    context.Context
	svcCtx *svc.ServiceContext
}

// 题目逻辑
func NewCommonLogic(ctx context.Context, svcCtx *svc.ServiceContext) *CommonLogic {
	return &CommonLogic{
		ctx:    ctx,
		svcCtx: svcCtx,
	}
}

// 开始消费者
func (l *CommonLogic) StartConsumer() {
	CommonChannel := l.svcCtx.Initializer.GetRabbitMQManager().CommonChannel
	// 声明队列（确保存在）
	_, err := CommonChannel.QueueDeclare(
		l.svcCtx.Config.RabbitMQ.Common.JudgeQueue,
		true, false, false, false, nil,
	)
	if err != nil {
		logx.Errorf("无法声明问题队列: %v", err)
		return
	}

	// 设置预取计数，控制并发度
	err = CommonChannel.Qos(
		10,    // 预取计数，控制并发处理的消息数量
		0,     // 预取大小，0表示无限制
		false, // 全局设置，false表示只对当前channel有效
	)
	if err != nil {
		logx.Errorf("设置Qos失败: %v", err)
		return
	}

	msgs, err := CommonChannel.Consume(
		l.svcCtx.Config.RabbitMQ.Common.JudgeQueue,
		"", false, false, false, false, nil,
	)
	if err != nil {
		logx.Errorf("注册题目消费者失败: %v", err)
		return
	}

	logx.Info("题目消费者已成功启动")

	var wg sync.WaitGroup
	semaphore := make(chan struct{}, 10) // 控制并发数量的信号量

	for d := range msgs {
		semaphore <- struct{}{} // 获取信号量
		wg.Add(1)

		go func(delivery amqp.Delivery) {
			defer wg.Done()
			defer func() { <-semaphore }() // 释放信号量

			l.processMessage(delivery)
		}(d)
	}

	wg.Wait() // 等待所有处理完成
}

func (l *CommonLogic) processMessage(delivery amqp.Delivery) {
	defer func() {
		if err := delivery.Ack(false); err != nil {
			logx.Errorf("未能确认题目消息: %v", err)
		}
	}()

	var judgeSubmit dto.JudgeRequest
	if err := json.Unmarshal(delivery.Body, &judgeSubmit); err != nil {
		logx.Errorf("解码 JSON 出错: %v", err)
		return
	}

	workspace, result := judge.NewWorkspace(l.ctx, l.svcCtx.Config, judgeSubmit, l.svcCtx)
	if result != nil {
		err := l.sendResultToMQ(result)
		if err != nil {
			logx.Errorf("发送结果到MQ失败: %v", err)
		}
		return
	}

	// 程序执行
	judgeResponse := workspace.Execute()

	// 如果正常会执行到这里
	err := l.sendResultToMQ(judgeResponse)
	if err != nil {
		logx.Errorf("发送结果到MQ失败: %v", err)
		err = workspace.Cleanup()
		if err != nil {
			logx.Errorf("删除工作空间失败: %v", err)
		}
		return
	}

	err = workspace.Cleanup()
	if err != nil {
		logx.Errorf("删除工作空间失败: %v", err)
		return
	}
}

func (l *CommonLogic) sendResultToMQ(result *dto.JudgeResponse) error {
	body, err := json.Marshal(result)

	if err != nil {
		return err
	}

	exchange := l.svcCtx.Config.RabbitMQ.Common.ResultExchange
	routingKey := l.svcCtx.Config.RabbitMQ.Common.ResultRoutingKey

	err = l.svcCtx.Initializer.GetRabbitMQManager().CommonChannel.Publish(
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
	return err
}

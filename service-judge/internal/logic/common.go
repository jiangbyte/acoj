package logic

import (
	"context"
	"encoding/json"
	"service-judge/internal/core"
	"service-judge/internal/mq"
	"service-judge/internal/svc"
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

/**
 * 创建判题消费者；并发上限来自 Nacos Consumer 配置。
 */
func NewCommonLogic(ctx context.Context, svcCtx *svc.ServiceContext) *CommonLogic {
	// 1. 读取并校验 MaxConcurrency，非法时回落到 4
	maxConcurrency := svcCtx.Config.Consumer.MaxConcurrency
	if maxConcurrency <= 0 {
		maxConcurrency = 4
	}
	return &CommonLogic{
		ctx:       ctx,
		svcCtx:    svcCtx,
		semaphore: make(chan struct{}, maxConcurrency),
	}
}

/**
 * 启动判题队列消费：声明队列 → 设置 QoS → 信号量限流处理。
 */
func (l *CommonLogic) StartConsumer() {
	CommonChannel := l.svcCtx.Initializer.GetRabbitMQManager().CommonChannel

	_, err := CommonChannel.QueueDeclare(
		l.svcCtx.Config.RabbitMQ.Common.JudgeQueue,
		true, false, false, false, nil,
	)
	if err != nil {
		logx.Errorf("无法声明问题队列: %v", err)
		return
	}

	// 1. Prefetch 与并发上限对齐，防止 broker 一次塞过多未 ack 消息
	prefetch := l.svcCtx.Config.Consumer.Prefetch
	if prefetch <= 0 {
		prefetch = len(l.semaphore)
		if prefetch <= 0 {
			prefetch = 4
		}
	}
	err = CommonChannel.Qos(
		prefetch,
		0,
		false,
	)
	if err != nil {
		logx.Errorf("设置Qos失败: %v", err)
		return
	}

	// 2. 注册消费者并进入限流处理循环
	msgs, err := CommonChannel.Consume(
		l.svcCtx.Config.RabbitMQ.Common.JudgeQueue,
		"", false, false, false, false, nil,
	)
	if err != nil {
		logx.Errorf("注册题目消费者失败: %v", err)
		return
	}

	logx.Infof("题目消费者已成功启动 (concurrency=%d prefetch=%d)", len(l.semaphore), prefetch)

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

	//// 提前确认消息，确保即使后续处理失败也不会重复消费
	//if err := delivery.Ack(false); err != nil {
	//	logx.Errorf("未能确认题目消息: %v", err)
	//}

	var judgeSubmit mq.JudgeRequest
	if err := json.Unmarshal(delivery.Body, &judgeSubmit); err != nil {
		logx.Errorf("解码 JSON 出错: %v", err)
		// JSON 解析失败，拒绝消息（不重新入队）
		if err := delivery.Nack(false, false); err != nil {
			logx.Errorf("拒绝消息失败: %v", err)
		}
		return
	}

	// 使用新的上下文创建工作空间
	workspace, result := core.NewWorkspace(ctx, l.svcCtx.Config, judgeSubmit, l.svcCtx)
	if result != nil {
		if err := l.sendResultToMQ(result); err != nil {
			logx.Errorf("发送结果到MQ失败: %v", err)
			// 发送失败，拒绝消息（重新入队）
			if err := delivery.Nack(false, true); err != nil {
				logx.Errorf("拒绝消息失败: %v", err)
			}
		}
		// 成功发送结果，确认消息
		if err := delivery.Ack(false); err != nil {
			logx.Errorf("确认消息失败: %v", err)
		}
		return
	}

	// 程序执行
	judgeResponse := workspace.Execute()

	// 发送结果
	if err := l.sendResultToMQ(judgeResponse); err != nil {
		logx.Errorf("发送结果到MQ失败: %v", err)
		// 发送失败，拒绝消息（重新入队）
		if err := delivery.Nack(false, true); err != nil {
			logx.Errorf("拒绝消息失败: %v", err)
		}
		workspace.Cleanup()
		return
	}

	// 整个处理流程成功完成，确认消息
	if err := delivery.Ack(false); err != nil {
		logx.Errorf("确认消息失败: %v", err)
	}

	// 异步清理工作空间
	go func() {
		if err := workspace.Cleanup(); err != nil {
			logx.Errorf("清理工作空间失败: %v", err)
		}
	}()

	logx.Infof("============【任务结束】============")
	// 异步处理消息
	//go l.processMessageAsync(ctx, judgeSubmit)
}

// 异步处理消息并收集结果
func (l *CommonLogic) processMessageAsync(ctx context.Context, judgeSubmit mq.JudgeRequest) {
	// 使用新的上下文创建工作空间
	workspace, result := core.NewWorkspace(ctx, l.svcCtx.Config, judgeSubmit, l.svcCtx)
	if result != nil {
		if err := l.sendResultToMQ(result); err != nil {
			logx.Errorf("发送结果到MQ失败: %v", err)
		}
		return
	}

	// 异步执行程序
	resultChan := make(chan *mq.JudgeResponse, 1)

	go func() {
		judgeResponse := workspace.Execute()
		resultChan <- judgeResponse
	}()

	// 等待执行结果或超时
	select {
	case judgeResponse := <-resultChan:
		// 发送结果
		if err := l.sendResultToMQ(judgeResponse); err != nil {
			logx.Errorf("发送结果到MQ失败: %v", err)
		}

		// 异步清理工作空间
		go func() {
			if err := workspace.Cleanup(); err != nil {
				logx.Errorf("清理工作空间失败: %v", err)
			}
		}()

	case <-ctx.Done():
		logx.Errorf("处理消息超时: %v", ctx.Err())
	}
}

func (l *CommonLogic) sendResultToMQ(result *mq.JudgeResponse) error {
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

	if err != nil {
		logx.Infof("============【消息已发送】============")
	}

	return err
}

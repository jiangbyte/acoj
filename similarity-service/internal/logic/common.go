package logic

import (
	"context"
	"encoding/json"
	"similarity-service/internal/core"
	"similarity-service/internal/mq"
	"similarity-service/internal/svc"
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
		semaphore: make(chan struct{}, 100), // 控制并发数量
	}
}

func (l *CommonLogic) StartConsumer() {
	CommonChannel := l.svcCtx.Initializer.GetRabbitMQManager().CommonChannel

	_, err := CommonChannel.QueueDeclare(
		l.svcCtx.Config.RabbitMQ.Common.SimilarityQueue,
		true, false, false, false, nil,
	)
	if err != nil {
		logx.Errorf("无法声明问题队列: %v", err)
		return
	}

	// 增加预取计数，提高并发度
	err = CommonChannel.Qos(
		100, // 增加预取计数
		0,
		false,
	)
	if err != nil {
		logx.Errorf("设置Qos失败: %v", err)
		return
	}

	msgs, err := CommonChannel.Consume(
		l.svcCtx.Config.RabbitMQ.Common.SimilarityQueue,
		"", false, false, false, false, nil,
	)
	if err != nil {
		logx.Errorf("注册相似消费者失败: %v", err)
		return
	}

	logx.Info("相似消费者已成功启动")

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

	//if err := delivery.Ack(false); err != nil {
	//	logx.Errorf("未能确认相似消息: %v", err)
	//}

	var similarityMessage mq.SimilarityMessage
	if err := json.Unmarshal(delivery.Body, &similarityMessage); err != nil {
		logx.Errorf("解码 JSON 出错: %v", err)
		// JSON 解析失败，拒绝消息（不重新入队）
		if err := delivery.Nack(false, false); err != nil {
			logx.Errorf("拒绝消息失败: %v", err)
		}
		return
	}

	// 使用新的上下文创建工作空间
	workspace, result := core.NewWorkspace(ctx, l.svcCtx.Config, similarityMessage, l.svcCtx)
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
	resultMessage := workspace.Execute()

	// 发送结果
	if err := l.sendResultToMQ(resultMessage); err != nil {
		//logx.Errorf("发送结果到MQ失败: %v", err)
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
	workspace.Cleanup()

	logx.Infof("========== OK ==========")
}

func (l *CommonLogic) sendResultToMQ(result *mq.SimilarityResultMessage) error {
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

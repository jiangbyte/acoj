package logic

import (
	"context"
	"encoding/json"
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/dto"
	"judge-service/internal/logic/judge"
	"judge-service/internal/svc"
	"time"

	"github.com/streadway/amqp"
	"github.com/zeromicro/go-zero/core/logx"
)

type ProblemLogic struct {
	logx.Logger
	ctx    context.Context
	svcCtx *svc.ServiceContext
}

func NewProblemLogic(ctx context.Context, svcCtx *svc.ServiceContext) *ProblemLogic {
	return &ProblemLogic{
		Logger: logx.WithContext(ctx),
		ctx:    ctx,
		svcCtx: svcCtx,
	}
}

func (l *ProblemLogic) StartConsumer() {
	// 声明队列（确保存在）
	_, err := l.svcCtx.ProblemChannel.QueueDeclare(
		l.svcCtx.Config.RabbitMQ.Problems.JudgeQueue,
		true, false, false, false, nil,
	)
	if err != nil {
		l.Errorf("无法声明问题队列: %v", err)
		return
	}

	msgs, err := l.svcCtx.ProblemChannel.Consume(
		l.svcCtx.Config.RabbitMQ.Problems.JudgeQueue,
		"", false, false, false, false, nil,
	)
	if err != nil {
		l.Errorf("注册题目消费者失败: %v", err)
		return
	}

	l.Info("题目消费者已成功启动")
	for d := range msgs {
		l.processMessage(d)
	}
}

func (l *ProblemLogic) processMessage(delivery amqp.Delivery) {
	defer func() {
		if err := delivery.Ack(false); err != nil {
			l.Errorf("未能确认题目消息: %v", err)
		}
	}()

	var request dto.JudgeSubmitDto
	if err := json.Unmarshal(delivery.Body, &request); err != nil {
		l.Errorf("解码 JSON 出错: %v", err)
		return
	}

	l.Infof("收到消息: %+v", request)

	// 查找语言配置
	var langConfig config.LanguageConfig
	found := false
	for _, lc := range l.svcCtx.Config.Languages {
		if lc.Name == request.Language {
			langConfig = lc
			found = true
			break
		}
	}

	// 如果语言不支持，创建默认错误结果并发送
	if !found {
		l.Errorf("不支持的语言: %s", request.Language)
		result := dto.ConvertSubmitToResult(request)
		result.Status = dto.StatusSystemError
		result.Message = fmt.Sprintf("不支持的语言: %s", request.Language)

		// 发送错误结果到MQ
		if err := l.sendResultToMQ(&result); err != nil {
			l.Errorf("发送不支持语言的结果到MQ失败: %v", err)
		}
		return
	}

	// 处理判题请求
	sandbox := judge.NewSandbox(l.ctx, langConfig)
	result := sandbox.ProcessSubmission(&request)

	// 发送结果到MQ
	err := l.sendResultToMQ(result)
	if err != nil {
		l.Errorf("发送结果到MQ失败: %v", err)
	}
}

func (l *ProblemLogic) sendResultToMQ(result *dto.JudgeResultDto) error {
	l.Infof("发送激活")
	body, err := json.Marshal(result)
	if err != nil {
		return err
	}

	exchange := l.svcCtx.Config.RabbitMQ.Problems.ResultExchange
	routingKey := l.svcCtx.Config.RabbitMQ.Problems.ResultRoutingKey

	err = l.svcCtx.ProblemChannel.Publish(
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

	l.Infof("结果发送OK")
	return err
}

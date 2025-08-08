package logic

import (
	"context"
	"encoding/json"
	"github.com/streadway/amqp"
	"github.com/zeromicro/go-zero/core/logx"
	"judge-service/internal/dto"
	"judge-service/internal/svc"
)

type ProblemSetLogic struct {
	logx.Logger
	ctx    context.Context
	svcCtx *svc.ServiceContext
}

func NewProblemSetLogic(ctx context.Context, svcCtx *svc.ServiceContext) *ProblemSetLogic {
	return &ProblemSetLogic{
		Logger: logx.WithContext(ctx),
		ctx:    ctx,
		svcCtx: svcCtx,
	}
}

func (l *ProblemSetLogic) StartConsumer() {
	// 声明队列（确保存在）
	_, err := l.svcCtx.ProblemSetChannel.QueueDeclare(
		l.svcCtx.Config.RabbitMQ.Sets.JudgeQueue,
		true, false, false, false, nil,
	)
	if err != nil {
		l.Errorf("无法声明问题队列: %v", err)
		return
	}

	msgs, err := l.svcCtx.ProblemSetChannel.Consume(
		l.svcCtx.Config.RabbitMQ.Sets.JudgeQueue,
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

func (l *ProblemSetLogic) processMessage(delivery amqp.Delivery) {
	defer func() {
		if err := delivery.Ack(false); err != nil {
			l.Errorf("未能确认题目消息: %v", err)
		}
	}()

	var request dto.JudgeSubmitDto
	if err := json.Unmarshal(delivery.Body, &request); err != nil {
		l.Errorf("解码题目 JSON 出错: %v", err)
		return
	}

	l.Infof("成功处理的题目提交 ID: %d", request.ID)
}

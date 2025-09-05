package logic

import (
	"context"
	"encoding/json"
	"github.com/streadway/amqp"
	"github.com/zeromicro/go-zero/core/logx"
	"judge-service/internal/dto"
	"judge-service/internal/logic/judge"
	"judge-service/internal/svc"
	"time"
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

	var judgeSubmit dto.JudgeSubmitDto
	if err := json.Unmarshal(delivery.Body, &judgeSubmit); err != nil {
		l.Errorf("解码 JSON 出错: %v", err)
		return
	}

	l.Infof("收到消息: %+v", judgeSubmit)
	// ==================================== 工作空间准备、保存源代码 ====================================
	workspace, workspaceResultDto := judge.NewWorkspace(l.ctx, l.svcCtx.Config, judgeSubmit)
	if workspaceResultDto != nil {
		err := l.sendResultToMQ(workspaceResultDto)
		if err != nil {
			l.Errorf("发送结果到MQ失败: %v", err)
		}
		return
	}
	SourceCodeResultDto := workspace.SaveSourceCode()
	if SourceCodeResultDto != nil {
		err := l.sendResultToMQ(SourceCodeResultDto)
		if err != nil {
			l.Errorf("发送结果到MQ失败: %v", err)
		}
		workspace.Cleanup()
		return
	}

	// ==================================== 执行源代码 ====================================
	RunResultDto, err := workspace.Execute()
	if err != nil {
		err := l.sendResultToMQ(RunResultDto)
		if err != nil {
			l.Errorf("发送结果到MQ失败: %v", err)
		}
		workspace.Cleanup()
		return
	}

	// ==================================== 结果汇总 ====================================
	EvaluateResultDto := workspace.Evaluate(RunResultDto)
	err = l.sendResultToMQ(EvaluateResultDto)
	if err != nil {
		l.Errorf("发送结果到MQ失败: %v", err)
	}

	// ==================================== 删除工作空间 ====================================
	err = workspace.Cleanup()
	if err != nil {
		l.Errorf("删除工作空间失败: %v", err)
	}
}

func (l *ProblemSetLogic) sendResultToMQ(result *dto.JudgeResultDto) error {
	body, err := json.Marshal(result)
	if err != nil {
		return err
	}

	exchange := l.svcCtx.Config.RabbitMQ.Sets.ResultExchange
	routingKey := l.svcCtx.Config.RabbitMQ.Sets.ResultRoutingKey

	err = l.svcCtx.ProblemSetChannel.Publish(
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

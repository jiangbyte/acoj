package logic

import (
	"context"
	"encoding/json"
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

	var judgeSubmit dto.JudgeSubmitDto
	if err := json.Unmarshal(delivery.Body, &judgeSubmit); err != nil {
		l.Errorf("解码 JSON 出错: %v", err)
		return
	}

	l.Infof("收到消息: %+v", judgeSubmit)

	// 工作空间准备
	workspace, workspaceResultDto := judge.NewWorkspace(l.ctx, l.svcCtx.Config, judgeSubmit)
	// 如果resultDto不为空，则返回结果
	if workspaceResultDto != nil {
		err := l.sendResultToMQ(workspaceResultDto)
		if err != nil {
			l.Errorf("发送结果到MQ失败: %v", err)
		}
		// 删除工作空间
		err1 := workspace.Cleanup()
		if err1 != nil {
			l.Errorf("删除工作空间失败: %v", err)
		}
		return
	}

	// 保存源代码
	SourceCodeResultDto := workspace.SaveSourceCode()
	if SourceCodeResultDto != nil {
		err := l.sendResultToMQ(SourceCodeResultDto)
		if err != nil {
			l.Errorf("发送结果到MQ失败: %v", err)
		}
		// 删除工作空间
		err1 := workspace.Cleanup()
		if err1 != nil {
			l.Errorf("删除工作空间失败: %v", err)
		}
		return
	}

	// 执行
	RunResultDto := workspace.Execute()
	if RunResultDto != nil {
		err := l.sendResultToMQ(RunResultDto)
		if err != nil {
			l.Errorf("发送结果到MQ失败: %v", err)
		}
		// 删除工作空间
		err1 := workspace.Cleanup()
		if err1 != nil {
			l.Errorf("删除工作空间失败: %v", err)
		}
		return
	}

	// 结果汇总
	EvaluateResultDto := workspace.Evaluate()
	if EvaluateResultDto != nil {
		err := l.sendResultToMQ(EvaluateResultDto)
		if err != nil {
			l.Errorf("发送结果到MQ失败: %v", err)
		}
		// 删除工作空间
		err1 := workspace.Cleanup()
		if err1 != nil {
			l.Errorf("删除工作空间失败: %v", err)
		}
	}

}

func (l *ProblemLogic) sendResultToMQ(result *dto.JudgeResultDto) error {
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
	return err
}

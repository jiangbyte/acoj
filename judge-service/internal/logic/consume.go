package logic

import (
	"context"
	"judge-service/internal/svc"

	"github.com/zeromicro/go-zero/core/logx"
)

type ConsumeLogic struct {
	logx.Logger
	ctx    context.Context
	svcCtx *svc.ServiceContext
}

func NewConsumeLogic(ctx context.Context, svcCtx *svc.ServiceContext) *ConsumeLogic {
	return &ConsumeLogic{
		Logger: logx.WithContext(ctx),
		ctx:    ctx,
		svcCtx: svcCtx,
	}
}

func (l *ConsumeLogic) StartConsuming() {
	// 启动题目消费者
	go NewProblemLogic(l.ctx, l.svcCtx).StartConsumer()
	
	// 启动题集消费者
	go NewProblemSetLogic(l.ctx, l.svcCtx).StartConsumer()

	// 阻塞主goroutine
	forever := make(chan bool)
	<-forever
}

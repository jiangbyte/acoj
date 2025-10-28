package logic

import (
	"context"
	"judge-service/internal/svc"
)

type ConsumeLogic struct {
	ctx    context.Context
	svcCtx *svc.ServiceContext
}

func NewConsumeLogic(ctx context.Context, svcCtx *svc.ServiceContext) *ConsumeLogic {
	return &ConsumeLogic{
		ctx:    ctx,
		svcCtx: svcCtx,
	}
}

func (l *ConsumeLogic) StartConsuming() {
	ctx, cancel := context.WithCancel(l.ctx)
	defer cancel()

	go NewCommonLogic(ctx, l.svcCtx).StartConsumer()

	<-ctx.Done()
}

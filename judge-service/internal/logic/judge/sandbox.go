package judge

import (
	"context"
	"github.com/zeromicro/go-zero/core/logx"
	"judge-service/internal/dto"
)

type Sandbox struct {
	ctx       context.Context
	logger    logx.Logger
	workspace Workspace
}

func NewSandbox(ctx context.Context, workspace Workspace) *Sandbox {
	return &Sandbox{
		ctx:       ctx,
		logger:    logx.WithContext(ctx),
		workspace: workspace,
	}
}

// 编译
func (s *Sandbox) Compile() *dto.JudgeResultDto {
	s.logger.Info("开始编译")
	return nil
}

// 执行
func (s *Sandbox) Run() *dto.JudgeResultDto {
	s.logger.Info("开始执行")
	return nil
}

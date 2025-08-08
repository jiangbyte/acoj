package judge

import (
	"context"
	"judge-service/internal/config"
	"judge-service/internal/dto"

	"github.com/zeromicro/go-zero/core/logx"
)

type Sandbox struct {
	ctx    context.Context
	logger logx.Logger
	config config.LanguageConfig
}

func NewSandbox(ctx context.Context, config config.LanguageConfig) *Sandbox {
	return &Sandbox{
		ctx:    ctx,
		logger: logx.WithContext(ctx),
		config: config,
	}
}

// ProcessSubmission 处理判题请求的完整流程
func (s *Sandbox) ProcessSubmission(submission *dto.JudgeSubmitDto) *dto.JudgeResultDto {
	result := dto.ConvertSubmitToResult(*submission)
	result.Status = dto.StatusWrongAnswer
	result.Message = "目前 OK"

	return &result
}

package judge

// import (
// 	"context"
// 	"judge-service/internal/dto"
// 	"judge-service/internal/svc"

// 	"github.com/zeromicro/go-zero/core/logx"
// )

// type Sandbox struct {
// 	ctx       context.Context
// 	Workspace Workspace
// 	svcCtx    *svc.ServiceContext
// }

// // 创建沙盒，传入上下文，工作空间
// func NewSandbox(ctx context.Context, workspace Workspace, svcCtx *svc.ServiceContext) *Sandbox {
// 	return &Sandbox{
// 		ctx:       ctx,
// 		Workspace: workspace,
// 		svcCtx:    svcCtx,
// 	}
// }

// // 编译，返回编译结果
// func (s *Sandbox) Compile() (*dto.JudgeResponse, error) {
// 	logx.Info("开始编译")
// 	c := NewCompiler(s.ctx, *s, s.svcCtx)
// 	return c.Execute()
// }

// // 执行，返回执行结果
// func (s *Sandbox) Run() (*dto.JudgeResponse, error) {
// 	logx.Info("开始执行")
// 	r := NewExecutor(s.ctx, *s, s.svcCtx)
// 	return r.Execute()
// }

// // 评估
// func (s *Sandbox) Evaluater(resultData *dto.JudgeResponse) (*dto.JudgeResponse, error) {
// 	logx.Info("开始评估")
// 	r := NewEvaluater(s.ctx, *s, resultData, s.svcCtx)
// 	return r.Execute()
// }

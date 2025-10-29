package judge

import (
	"context"
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/dto"
	"judge-service/internal/svc"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zeromicro/go-zero/core/logx"
)

type Workspace struct {
	ctx          context.Context
	config       config.Config
	startTime    time.Time
	langConfig   config.LanguageConfig
	judgeRequest dto.JudgeRequest
	RootPath     string // 工作空间根目录
	SourcePath   string // 源代码目录
	SourceFile   string // 源代码文件
	BuildPath    string // 编译目录
	BuildFile    string // 编译文件
	RunsPath     string // 运行目录
	svcCtx       *svc.ServiceContext
}

// NewWorkspace 创建工作空间,上下文/配置/提交信息,返回 工作空间实例 和 提交信息
func NewWorkspace(ctx context.Context, config config.Config, judgeRequest dto.JudgeRequest, svcCtx *svc.ServiceContext) (*Workspace, *dto.JudgeResponse) {
	submissionID := generateSubmissionID()
	workDir := config.Workspace
	root := filepath.Join(workDir, submissionID) // 本次工作空间根目录

	ws := &Workspace{
		ctx:          ctx,                           // 使用传入的ctx
		startTime:    time.Now(),                    // 记录开始时间，用来计算任务总耗时
		config:       config,                        // 系统配置
		judgeRequest: judgeRequest,                  // 提交信息
		RootPath:     root,                          // 根目录
		SourcePath:   filepath.Join(root, "source"), // 源代码目录
		BuildPath:    filepath.Join(root, "build"),  // 编译目录
		RunsPath:     filepath.Join(root, "runs"),   // 运行目录
		svcCtx:       svcCtx,
	}

	// 创建目录
	if err := ws.createDirs(); err != nil {
		return nil, ws.buildErrorResponse("创建工作空间失败: " + err.Error())
	}

	return ws, nil
}

// generateSubmissionID 生成提交ID
func generateSubmissionID() string {
	return strings.ReplaceAll(uuid.New().String(), "-", "")
}

// buildErrorResponse 构建错误响应
func (w *Workspace) buildErrorResponse(message string) *dto.JudgeResponse {
	return &dto.JudgeResponse{
		UserId:      w.judgeRequest.UserId,
		ProblemId:   w.judgeRequest.ProblemId,
		Language:    w.judgeRequest.Language,
		SetId:       w.judgeRequest.SetId,
		Code:        w.judgeRequest.Code,
		SubmitType:  w.judgeRequest.SubmitType,
		MaxTime:     w.judgeRequest.MaxTime,
		MaxMemory:   w.judgeRequest.MaxMemory,
		Message:     message,
		ID:          w.judgeRequest.ID,
		IsSet:       w.judgeRequest.IsSet,
		Status:      "SYSTEM_ERROR",
		ExitCode:    1,
		JudgeTaskId: w.judgeRequest.JudgeTaskId,
	}
}

// createDirs 创建目录
func (w *Workspace) createDirs() error {
	dirs := []string{w.SourcePath, w.BuildPath, w.RunsPath}
	for _, dir := range dirs {
		if err := os.MkdirAll(dir, 0755); err != nil {
			return fmt.Errorf("创建目录 %s 失败: %w", dir, err)
		}
	}
	return nil
}

// getLanguageConfig 获取语言配置
func (w *Workspace) getLanguageConfig() (*config.LanguageConfig, error) {
	for _, lang := range w.config.Languages {
		if lang.Name == w.judgeRequest.Language {
			return &lang, nil
		}
	}
	return nil, fmt.Errorf("不支持的语言: %s", w.judgeRequest.Language)
}

// writeSourceCode 写入源代码
func (w *Workspace) writeSourceCode() error {
	langConfig, err := w.getLanguageConfig()
	if err != nil {
		return err
	}
	w.langConfig = *langConfig

	fileName := langConfig.SourceFile + langConfig.Extension
	filePath := filepath.Join(w.SourcePath, fileName)

	if err := os.WriteFile(filePath, []byte(w.judgeRequest.Code), 0644); err != nil {
		return fmt.Errorf("写入源代码文件失败: %w", err)
	}

	w.SourceFile = filePath
	w.BuildFile = filepath.Join(w.RunsPath, langConfig.CompileFile)
	logx.Infof("源代码保存成功: %s", filePath)

	return nil
}

// 执行代码
func (w *Workspace) Execute() *dto.JudgeResponse {

	// 1. 写入源代码
	if err := w.writeSourceCode(); err != nil {
		return w.buildErrorResponse(err.Error())
	}

	// 2. 编译（如果需要）
	if w.langConfig.NeedCompile {
		// TODO 执行编译
		compileResult, err := w.compile()
		if err != nil {
			return w.buildErrorResponse("编译失败: " + err.Error())
		}
		if !compileResult.Success {
			return &dto.JudgeResponse{
				Status:  "COMPILE_ERROR",
				Message: compileResult.Message,
			}
		}
	}

	// 3. 执行测试用例
	executeResult, err := w.executeTestCases()
	if err != nil {
		return w.buildErrorResponse("执行失败: " + err.Error())
	}

	// 4. 评估结果
	evaluationResult := w.evaluate(executeResult)

	// 5. 返回最终结果
	return evaluationResult
}

// Cleanup 清理工作空间
func (w *Workspace) Cleanup() error {
	logx.Infof("开始清理工作空间 路径: %s", w.RootPath)

	if err := os.RemoveAll(w.RootPath); err != nil {
		logx.Errorf("清理工作空间失败 路径: %s, 错误: %v", w.RootPath, err)
		return err
	}

	logx.Infof("工作空间清理完毕: %s", w.RootPath)
	return nil
}

package judge

import (
	"context"
	"fmt"
	"github.com/google/uuid"
	"github.com/zeromicro/go-zero/core/logx"
	"judge-service/internal/config"
	"judge-service/internal/dto"
	"os"
	"path/filepath"
	"strings"
	"time"
)

type Workspace struct {
	ctx         context.Context
	logger      logx.Logger
	config      config.Config
	startTime   time.Time
	langConfig  config.LanguageConfig
	judgeSubmit dto.JudgeSubmitDto
	RootPath    string // 工作空间根目录
	SourcePath  string // 源代码目录
	SourceFile  string // 源代码文件
	BuildPath   string // 编译目录
	BuildFile   string // 编译文件
	RunsPath    string
}

// NewWorkspace 创建工作空间
func NewWorkspace(ctx context.Context, config config.Config, judgeSubmitDto dto.JudgeSubmitDto) (*Workspace, *dto.JudgeResultDto) {
	workUuid := uuid.New()
	submissionID := strings.ReplaceAll(workUuid.String(), "-", "")

	workDir := config.Workspace
	root := filepath.Join(workDir, submissionID)

	ws := &Workspace{
		ctx:         ctx,                           // 使用传入的ctx
		logger:      logx.WithContext(ctx),         // 使用传入的ctx创建logger
		startTime:   time.Now(),                    // 记录开始时间，用来计算任务总耗时
		config:      config,                        // 系统配置
		judgeSubmit: judgeSubmitDto,                // 提交信息
		RootPath:    root,                          // 根目录
		SourcePath:  filepath.Join(root, "source"), // 源代码目录
		BuildPath:   filepath.Join(root, "build"),  // 编译目录
		RunsPath:    filepath.Join(root, "runs"),   // 运行目录
	}
	if err := ws.createDirs(); err != nil {
		result := dto.ConvertSubmitToResult(judgeSubmitDto)
		result.Status = dto.StatusSystemError
		result.Message = fmt.Sprintf("创建工作空间失败: %v", err)
		return nil, &result
	}

	return ws, nil
}

// 独立的创建目录方法
func (w *Workspace) createDirs() error {
	dirs := []string{w.SourcePath, w.BuildPath, w.RunsPath}
	for _, dir := range dirs {
		if err := os.MkdirAll(dir, 0755); err != nil {
			return err
		}
	}
	return nil
}

// Cleanup 删除工作空间
func (w *Workspace) Cleanup() error {
	return os.RemoveAll(w.RootPath)
}

// getLanguageConfig 查找并返回语言配置
func (w *Workspace) getLanguageConfig() (*config.LanguageConfig, error) {
	for _, l := range w.config.Languages {
		if l.Name == w.judgeSubmit.Language {
			return &l, nil
		}
	}
	return nil, fmt.Errorf("不支持的语言: %s", w.judgeSubmit.Language)
}

// SaveSourceCode 保存源代码
func (w *Workspace) SaveSourceCode() *dto.JudgeResultDto {
	// 判断语言是否支持
	langConfig, err := w.getLanguageConfig()
	if err != nil {
		w.logger.Errorf(err.Error())
		result := dto.ConvertSubmitToResult(w.judgeSubmit)
		result.Status = dto.StatusSystemError
		result.Message = err.Error()
		return &result
	}

	w.langConfig = *langConfig
	fileName := w.langConfig.Name + w.langConfig.Extension
	filePath := filepath.Join(w.SourcePath, fileName)
	if err := os.WriteFile(filePath, []byte(w.judgeSubmit.Code), 0644); err != nil {
		w.logger.Errorf("写入源代码文件失败: %v", err)
		result := dto.ConvertSubmitToResult(w.judgeSubmit)
		result.Status = dto.StatusSystemError
		result.Message = fmt.Sprintf("写入源代码文件失败：%v", err)
		return &result
	}
	w.SourceFile = filePath
	w.BuildFile = filepath.Join(w.BuildPath, w.langConfig.CompileFile)
	return nil
}

func (w *Workspace) CreateTestCaseDir(testCaseID string) (string, error) {
	caseDir := filepath.Join(w.RunsPath, testCaseID)
	if err := os.MkdirAll(caseDir, 0755); err != nil {
		return "", fmt.Errorf("创建测试用例目录失败: %v", err)
	}
	return caseDir, nil
}

// 执行代码
func (w *Workspace) Execute() *dto.JudgeResultDto {
	sandbox := NewSandbox(w.ctx, *w)
	// 编译
	compileResult := sandbox.Compile()
	if compileResult != nil {
		return compileResult
	}

	// 运行
	runResult := sandbox.Run()
	if runResult != nil {
		return runResult
	}

	result := dto.ConvertSubmitToResult(w.judgeSubmit)
	result.Status = dto.StatusSystemError
	result.Message = fmt.Sprintf("执行错误")
	return runResult
}

// 结果汇总
func (w *Workspace) Evaluate() *dto.JudgeResultDto {
	result := dto.ConvertSubmitToResult(w.judgeSubmit)
	result.Status = dto.StatusSystemError
	result.Message = fmt.Sprintf("评估步骤")
	return &result
}

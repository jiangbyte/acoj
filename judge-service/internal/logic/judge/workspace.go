package judge

import (
	"context"
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/dto"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/zeromicro/go-zero/core/logx"
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
	RunsPath    string // 运行目录
}

// NewWorkspace 创建工作空间,上下文/配置/提交信息,返回 工作空间实例 和 提交信息
func NewWorkspace(ctx context.Context, config config.Config, judgeSubmitDto dto.JudgeSubmitDto) (*Workspace, *dto.JudgeResultDto) {
	// 生成工作空间ID
	workUuid := uuid.New()
	submissionID := strings.ReplaceAll(workUuid.String(), "-", "") // 替换-为空，生成没有连字符的UUID

	workDir := config.Workspace                  // 工作空间根目录
	root := filepath.Join(workDir, submissionID) // 本次工作空间根目录

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

	// 创建目录
	if err := ws.createDirs(); err != nil {
		// err 不为空，说明创建目录失败，返回错误信息
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
	// return os.RemoveAll(w.RootPath)
	return nil
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
		// err 不为空，说明不支持的语言
		w.logger.Errorf(err.Error())
		result := dto.ConvertSubmitToResult(w.judgeSubmit)
		result.Status = dto.StatusSystemError
		result.Message = err.Error()
		return &result
	}

	// 设置工作空间语言配置
	w.langConfig = *langConfig

	fileName := w.langConfig.Name + w.langConfig.Extension // 设置源代码文件名
	filePath := filepath.Join(w.SourcePath, fileName)      // 源文件路径
	// 写入文件
	if err := os.WriteFile(filePath, []byte(w.judgeSubmit.Code), 0644); err != nil {
		w.logger.Errorf("写入源代码文件失败: %v", err)
		result := dto.ConvertSubmitToResult(w.judgeSubmit)
		result.Status = dto.StatusSystemError
		result.Message = fmt.Sprintf("写入源代码文件失败：%v", err)
		return &result
	}
	// 设置工作空间源代码文件路径（编译用）和编译文件路径（执行用）
	w.SourceFile = filePath
	w.BuildFile = filepath.Join(w.BuildPath, w.langConfig.CompileFile)
	return nil
}

// func (w *Workspace) CreateTestCaseDir(testCaseID string) (string, error) {
// 	caseDir := filepath.Join(w.RunsPath, testCaseID)
// 	if err := os.MkdirAll(caseDir, 0755); err != nil {
// 		return "", fmt.Errorf("创建测试用例目录失败: %v", err)
// 	}
// 	return caseDir, nil
// }

// 执行代码
func (w *Workspace) Execute() (*dto.JudgeResultDto, error) {
	// 构建沙箱，传入工作空间上下文，工作空间实例
	sandbox := NewSandbox(w.ctx, *w)
	// 沙箱编译
	compileResult, err := sandbox.Compile()
	if err != nil {
		// compileResult 不为空，返回编译结果给上级
		return compileResult, err
	}

	// 沙箱运行
	runResult, err := sandbox.Run()

	// 运行成功，返回运行结果给上级
	return runResult, err
}

// 结果汇总
func (w *Workspace) Evaluate(submit *dto.JudgeResultDto) *dto.JudgeResultDto {
	// submit.Status = dto.StatusSystemError
	// submit.Message = "评估步骤执行可以"

	maxTime := 0
	maxMemory := 0

	// 使用range遍历（获取索引和值）
	for _, testCase := range submit.TestCase {
		if maxTime > testCase.MaxTime {
			maxTime = testCase.MaxTime
		}
		if maxMemory > testCase.MaxMemory {
			maxMemory = testCase.MaxMemory
		}
	}

	submit.MaxTime = maxTime
	submit.MaxMemory = maxMemory

	return submit
}

package core

import (
	"context"
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/mq"
	"judge-service/internal/svc"
	"judge-service/internal/utils"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/zeromicro/go-zero/core/logx"
)

// WorkspaceManager 工作空间管理器，负责工作空间的创建和清理
type WorkspaceManager struct {
	workspaces sync.Map // 存储活跃的工作空间
	basePath   string
	mu         sync.RWMutex
}

var (
	workspaceManager *WorkspaceManager
	once             sync.Once
)

func GetWorkspaceManager(basePath string) *WorkspaceManager {
	once.Do(func() {
		workspaceManager = &WorkspaceManager{
			basePath: basePath,
		}
	})
	return workspaceManager
}

type Workspace struct {
	ctx          context.Context
	config       config.Config
	startTime    time.Time
	langConfig   config.LanguageConfig
	judgeRequest mq.JudgeRequest
	RootPath     string // 工作空间根目录
	SourcePath   string // 源代码目录
	SourceFile   string // 源代码文件
	BuildPath    string // 编译目录
	BuildFile    string // 编译文件
	RunsPath     string // 运行目录
	svcCtx       *svc.ServiceContext
	mu           sync.RWMutex // 工作空间内部锁
	isCleaned    bool         // 标记是否已清理
}

// NewWorkspace 创建工作空间,上下文/配置/提交信息,返回 工作空间实例 和 提交信息
func NewWorkspace(ctx context.Context, config config.Config, judgeRequest mq.JudgeRequest, svcCtx *svc.ServiceContext) (*Workspace, *mq.JudgeResponse) {
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
		isCleaned:    false,
	}

	// 注册到工作空间管理器
	GetWorkspaceManager(config.Workspace).Register(ws)

	// 创建目录
	if err := ws.createDirs(); err != nil {
		return nil, ws.buildErrorResponse("创建工作空间失败: "+err.Error(), "SYSTEM_ERROR")
	}

	return ws, nil
}

// generateSubmissionID 生成提交ID
func generateSubmissionID() string {
	return strings.ReplaceAll(uuid.New().String(), "-", "")
}

// buildErrorResponse 构建错误响应
func (w *Workspace) buildErrorResponse(message string, status string) *mq.JudgeResponse {
	return &mq.JudgeResponse{
		UserId:      w.judgeRequest.UserId,
		ProblemId:   w.judgeRequest.ProblemId,
		Language:    w.judgeRequest.Language,
		ModuleId:    w.judgeRequest.ModuleId,
		Code:        w.judgeRequest.Code,
		SubmitType:  w.judgeRequest.SubmitType,
		MaxTime:     0,
		MaxMemory:   0,
		Message:     message,
		ID:          w.judgeRequest.ID,
		ModuleType:  w.judgeRequest.ModuleType,
		Status:      status,
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
func (w *Workspace) Execute() *mq.JudgeResponse {
	startTime := time.Now()
	w.mu.Lock()
	defer w.mu.Unlock()

	if w.isCleaned {
		return w.buildErrorResponse("工作空间已清理", "SYSTEM_ERROR")
	}

	// 1. 写入源代码
	if err := w.writeSourceCode(); err != nil {
		return w.buildErrorResponse(err.Error(), "SYSTEM_ERROR")
	}

	// 2. 编译（如果需要）
	if w.langConfig.NeedCompile {
		compileResult, err := w.compile()
		if err != nil {
			logx.Error("编译失败", err.Error())
			compileResult.Message = utils.AutoMaskAllFilePaths(compileResult.Message)
			return w.buildErrorResponse(compileResult.Message, "COMPILATION_ERROR")
		}
		if !compileResult.Success {
			compileResult.Message = utils.AutoMaskAllFilePaths(compileResult.Message)
			return w.buildErrorResponse(compileResult.Message, "COMPILATION_ERROR")
		}
	}

	// 3. 执行测试用例
	executeResult, err := w.executeTestCases()
	if err != nil {
		return w.buildErrorResponse("执行失败: "+err.Error(), "RUNTIME_ERROR")
	}

	// 4. 评估结果
	evaluationResult := w.evaluate(executeResult)

	totalTime := time.Since(startTime)
	logx.Infof("Execute函数总执行耗时: %v", totalTime)

	// 5. 返回最终结果
	return evaluationResult
}

// Register 注册工作空间
func (wm *WorkspaceManager) Register(ws *Workspace) {
	wm.workspaces.Store(ws.RootPath, ws)
}

// Unregister 注销工作空间
func (wm *WorkspaceManager) Unregister(ws *Workspace) {
	wm.workspaces.Delete(ws.RootPath)
}

// GetAllWorkspaces 获取所有工作空间（用于监控）
func (wm *WorkspaceManager) GetAllWorkspaces() []*Workspace {
	var workspaces []*Workspace
	wm.workspaces.Range(func(key, value interface{}) bool {
		if ws, ok := value.(*Workspace); ok {
			workspaces = append(workspaces, ws)
		}
		return true
	})
	return workspaces
}

// Cleanup 清理工作空间
func (w *Workspace) Cleanup() error {
	w.mu.Lock()
	defer w.mu.Unlock()

	if w.isCleaned {
		return nil
	}

	logx.Infof("开始清理工作空间 路径: %s", w.RootPath)

	// 从管理器注销
	GetWorkspaceManager(w.config.Workspace).Unregister(w)

	// 异步清理，不阻塞当前goroutine
	go func(path string) {
		if err := os.RemoveAll(path); err != nil {
			logx.Errorf("清理工作空间失败 路径: %s, 错误: %v", path, err)
		} else {
			logx.Infof("工作空间清理完毕: %s", path)
		}
	}(w.RootPath)

	w.isCleaned = true
	return nil
}

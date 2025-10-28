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
	ctx         context.Context
	config      config.Config
	startTime   time.Time
	langConfig  config.LanguageConfig
	judgeSubmit dto.JudgeRequest
	RootPath    string // 工作空间根目录
	SourcePath  string // 源代码目录
	SourceFile  string // 源代码文件
	BuildPath   string // 编译目录
	BuildFile   string // 编译文件
	RunsPath    string // 运行目录
	svcCtx      *svc.ServiceContext
}

// NewWorkspace 创建工作空间,上下文/配置/提交信息,返回 工作空间实例 和 提交信息
func NewWorkspace(ctx context.Context, config config.Config, judgeSubmitDto dto.JudgeRequest, svcCtx *svc.ServiceContext) (*Workspace, *dto.JudgeResponse) {
	// 生成工作空间ID
	workUuid := uuid.New()
	submissionID := strings.ReplaceAll(workUuid.String(), "-", "") // 替换-为空，生成没有连字符的UUID

	workDir := config.Workspace                  // 工作空间根目录
	root := filepath.Join(workDir, submissionID) // 本次工作空间根目录

	ws := &Workspace{
		ctx:         ctx,                           // 使用传入的ctx
		startTime:   time.Now(),                    // 记录开始时间，用来计算任务总耗时
		config:      config,                        // 系统配置
		judgeSubmit: judgeSubmitDto,                // 提交信息
		RootPath:    root,                          // 根目录
		SourcePath:  filepath.Join(root, "source"), // 源代码目录
		BuildPath:   filepath.Join(root, "build"),  // 编译目录
		RunsPath:    filepath.Join(root, "runs"),   // 运行目录
		svcCtx:      svcCtx,
	}

	// 创建目录
	if err := ws.createDirs(); err != nil {
		// err 不为空，说明创建目录失败，返回错误信息
		result := dto.JudgeResponse{
			UserId:      judgeSubmitDto.UserId,
			ProblemId:   judgeSubmitDto.ProblemId,
			Language:    judgeSubmitDto.Language,
			SetId:       judgeSubmitDto.SetId,
			Code:        judgeSubmitDto.Code,
			SubmitType:  judgeSubmitDto.SubmitType,
			MaxTime:     judgeSubmitDto.MaxTime,
			MaxMemory:   judgeSubmitDto.MaxMemory,
			Message:     "失败了",
			ID:          judgeSubmitDto.ID,
			IsSet:       judgeSubmitDto.IsSet,
			Status:      "ERROR",
			ExitCode:    0,
			JudgeTaskId: judgeSubmitDto.JudgeTaskId,
		}
		result.Status = "ERROR"
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
	logx.Infof("开始清理工作空间 路径: %s", w.RootPath)

	err := os.RemoveAll(w.RootPath)
	if err != nil {
		logx.Errorf("清理工作空间失败 路径: %s, 错误: %v", w.RootPath, err)
		return err
	}

	logx.Infof("工作空间清理完毕: %s", w.RootPath)
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

// 执行代码
func (w *Workspace) Execute() *dto.JudgeResponse {
	// 获取测试用例
	res, err := w.svcCtx.TestCaseRepo().GetTestCasesByProblemID(w.ctx, w.judgeSubmit.ProblemId)
	if err != nil {
		logx.Errorf("获取测试用例失败, ProblemID: %d, 错误: %v", w.judgeSubmit.ProblemId, err)
		return nil
	}

	// 检查测试用例是否存在
	if len(res) == 0 {
		logx.Error("问题ID %d 没有找到测试用例", w.judgeSubmit.ProblemId)
		return nil
	}

	logx.Infof("获取到 %d 个测试用例，问题ID: %d", len(res), w.judgeSubmit.ProblemId)

	// 判断语言是否支持
	langConfig, err := w.getLanguageConfig()
	if err != nil {
		// err 不为空，说明不支持的语言
		logx.Errorf(err.Error())
		result := dto.JudgeResponse{
			UserId:      w.judgeSubmit.UserId,
			ProblemId:   w.judgeSubmit.ProblemId,
			Language:    w.judgeSubmit.Language,
			SetId:       w.judgeSubmit.SetId,
			Code:        w.judgeSubmit.Code,
			SubmitType:  w.judgeSubmit.SubmitType,
			MaxTime:     w.judgeSubmit.MaxTime,
			MaxMemory:   w.judgeSubmit.MaxMemory,
			Message:     "失败了",
			ID:          w.judgeSubmit.ID,
			IsSet:       w.judgeSubmit.IsSet,
			Status:      "ERROR",
			ExitCode:    0,
			JudgeTaskId: w.judgeSubmit.JudgeTaskId,
		}
		result.Status = "ERROR"
		result.Message = err.Error()
		return &result
	}

	// 设置工作空间语言配置
	w.langConfig = *langConfig

	fileName := w.langConfig.SourceFile + w.langConfig.Extension // 设置源代码文件名
	filePath := filepath.Join(w.SourcePath, fileName)            // 源文件路径
	// 写入文件
	if err := os.WriteFile(filePath, []byte(w.judgeSubmit.Code), 0644); err != nil {
		logx.Errorf("写入源代码文件失败: %v", err)
		result := dto.JudgeResponse{
			UserId:      w.judgeSubmit.UserId,
			ProblemId:   w.judgeSubmit.ProblemId,
			Language:    w.judgeSubmit.Language,
			SetId:       w.judgeSubmit.SetId,
			Code:        w.judgeSubmit.Code,
			SubmitType:  w.judgeSubmit.SubmitType,
			MaxTime:     w.judgeSubmit.MaxTime,
			MaxMemory:   w.judgeSubmit.MaxMemory,
			Message:     "失败了",
			ID:          w.judgeSubmit.ID,
			IsSet:       w.judgeSubmit.IsSet,
			Status:      "ERROR",
			ExitCode:    0,
			JudgeTaskId: w.judgeSubmit.JudgeTaskId,
		}
		result.Status = "ERROR"
		result.Message = fmt.Sprintf("写入源代码文件失败：%v", err)
		return &result
	}
	// 设置工作空间源代码文件路径（编译用）和编译文件路径（执行用）
	w.SourceFile = filePath
	w.BuildFile = filepath.Join(w.RunsPath, w.langConfig.CompileFile)
	logx.Infof("源代码保存成功: %s", filePath)
	// sandbox := NewSandbox(w.ctx, *w, w.svcCtx)

	// langConfig, err := w.getLanguageConfig()
	// if err != nil {
	// 	return nil, fmt.Errorf("获取语言配置失败: %v", err)
	// }

	// // 调试日志
	// logx.Infof("语言: %s, 编译命令长度: %d", w.judgeSubmit.Language, len(langConfig.CompileCmd))

	// // 如果编译命令不为空，执行编译
	// if len(langConfig.CompileCmd) > 0 {
	// 	// 沙箱编译
	// 	compileResult, err := sandbox.Compile()
	// 	// 不为空说明出错了
	// 	if err != nil {
	// 		if compileResult.Message != "" {
	// 			compileResult.Message = grutil.FilterFilePath(compileResult.Message)
	// 		}
	// 		return compileResult, err // 编译结果到这里结束
	// 	}
	// }

	// // 沙箱运行
	// runResult, _ := sandbox.Run()

	// // 结果评估
	// evaluateResult, _ := sandbox.Evaluater(runResult)
	// if evaluateResult.Message != "" {
	// 	evaluateResult.Message = grutil.FilterFilePath(evaluateResult.Message)
	// }

	// 运行成功，返回运行结果给上级
	// res := dto.JudgeResponse{
	// 	UserId:      w.judgeSubmit.UserId,
	// 	ProblemId:   w.judgeSubmit.ProblemId,
	// 	Language:    w.judgeSubmit.Language,
	// 	SetId:       w.judgeSubmit.SetId,
	// 	Code:        w.judgeSubmit.Code,
	// 	SubmitType:  w.judgeSubmit.SubmitType,
	// 	MaxTime:     w.judgeSubmit.MaxTime,
	// 	MaxMemory:   w.judgeSubmit.MaxMemory,
	// 	Message:     "失败了",
	// 	ID:          w.judgeSubmit.ID,
	// 	IsSet:       w.judgeSubmit.IsSet,
	// 	Status:      "ERROR",
	// 	ExitCode:    0,
	// 	JudgeTaskId: w.judgeSubmit.JudgeTaskId,
	// }
	// return &res, nil
	return nil
}

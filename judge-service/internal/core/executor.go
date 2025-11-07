package core

import (
	"bytes"
	"context"
	"fmt"
	model2 "judge-service/internal/database/model"
	"judge-service/internal/utils"
	"os/exec"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

// ExecutorManager 执行器管理器
type ExecutorManager struct {
	executors sync.Pool
}

var executorManager = &ExecutorManager{
	executors: sync.Pool{
		New: func() interface{} {
			return &SandboxExecutor{}
		},
	},
}

func GetExecutor() *SandboxExecutor {
	return executorManager.executors.Get().(*SandboxExecutor)
}

func ReleaseExecutor(executor *SandboxExecutor) {
	executorManager.executors.Put(executor)
}

// Executor 执行器接口
type Executor interface {
	Execute(workspace *Workspace, testCase *model2.DataTestCase) (*model2.DataJudgeCase, error)
}

// SandboxExecutor 沙箱执行器
type SandboxExecutor struct {
	// mu sync.Mutex
}

// Execute 执行测试用例
func (e *SandboxExecutor) Execute(workspace *Workspace, testCase *model2.DataTestCase) (*model2.DataJudgeCase, error) {
	startTime := time.Now()

	// 初始化结果对象
	result := e.initResult(workspace, testCase)

	// 创建cgroup进行资源限制
	cgroupPath, err := utils.CreateCgroup(workspace.judgeRequest.MaxMemory)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("创建cgroup失败: %v", err), 1, err)
	}
	defer utils.CleanupCgroup(cgroupPath)

	// 执行命令并获取结果
	return e.executeCommand(workspace, testCase, result, cgroupPath, startTime)
}

// initResult 初始化结果对象
func (e *SandboxExecutor) initResult(workspace *Workspace, testCase *model2.DataTestCase) *model2.DataJudgeCase {
	now := time.Now()
	return &model2.DataJudgeCase{
		ID:             utils.GenerateID(),
		SubmitID:       workspace.judgeRequest.ID,
		CaseSign:       testCase.CaseSign,
		InputData:      testCase.InputData,
		ExpectedOutput: testCase.ExpectedOutput,
		IsSample:       testCase.IsSample,
		Score:          testCase.Score,
		Status:         "PENDING",
		// 文件相关字段
		InputFilePath:  "", // 初始化为空字符串
		InputFileSize:  0,  // 使用默认值
		OutputFilePath: "", // 初始化为空字符串
		OutputFileSize: 0,  // 使用默认值
		// 执行结果相关字段
		MaxTime:   0.00,
		MaxMemory: 0.00,
		Message:   "",
		ExitCode:  0,
		Deleted:   false,
		// 时间相关字段
		CreateTime: &now,
		CreateUser: "0",
		UpdateTime: &now,
		UpdateUser: "0",
	}
}

// executeCommand 执行命令并处理结果
func (e *SandboxExecutor) executeCommand(workspace *Workspace, testCase *model2.DataTestCase,
	result *model2.DataJudgeCase, cgroupPath string, startTime time.Time) (*model2.DataJudgeCase, error) {

	// 准备命令执行环境
	cmd, stdoutBuf, stderrBuf, ctx, cancel, err := e.prepareCommand(workspace, testCase)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("准备命令失败: %v", err), 1, err)
	}
	defer cancel()

	// 启动并管理进程
	pgid, err := e.startAndManageProcess(cmd, cgroupPath)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("进程管理失败: %v", err), 1, err)
	}

	// 等待命令完成并收集结果
	return e.waitForCompletion(cmd, ctx, workspace, testCase, result, cgroupPath, pgid, startTime, stdoutBuf, stderrBuf)
}

// prepareCommand 准备命令执行环境
func (e *SandboxExecutor) prepareCommand(workspace *Workspace, testCase *model2.DataTestCase) (
	*exec.Cmd, *bytes.Buffer, *bytes.Buffer, context.Context, context.CancelFunc, error) {

	runCmd := utils.GetRunCommand(workspace.langConfig, workspace.SourceFile, workspace.BuildFile)

	// 设置超时上下文（执行时间限制 + 安全余量）
	timeout := time.Duration(workspace.judgeRequest.MaxTime)*time.Millisecond + 30*time.Millisecond
	ctx, cancel := context.WithTimeout(context.Background(), timeout)

	cmd := exec.CommandContext(ctx, runCmd[0], runCmd[1:]...)
	e.setProcessAttributes(cmd)

	// 设置输入输出缓冲区
	cmd.Stdin = strings.NewReader(testCase.InputData)
	var stdoutBuf, stderrBuf bytes.Buffer
	cmd.Stdout = &stdoutBuf
	cmd.Stderr = &stderrBuf

	return cmd, &stdoutBuf, &stderrBuf, ctx, cancel, nil
}

// setProcessAttributes 设置进程属性
func (e *SandboxExecutor) setProcessAttributes(cmd *exec.Cmd) {
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Setpgid: true,
		Cloneflags: syscall.CLONE_NEWNS |
			syscall.CLONE_NEWUTS |
			syscall.CLONE_NEWPID |
			syscall.CLONE_NEWNET |
			syscall.CLONE_NEWIPC,
		Unshareflags: syscall.CLONE_NEWNS,
	}
}

// startAndManageProcess 启动并管理进程
func (e *SandboxExecutor) startAndManageProcess(cmd *exec.Cmd, cgroupPath string) (int, error) {
	// 启动命令
	if err := cmd.Start(); err != nil {
		return 0, fmt.Errorf("启动进程失败: %w", err)
	}

	pgid := cmd.Process.Pid

	// 暂停进程以便设置cgroup
	if err := syscall.Kill(pgid, syscall.SIGSTOP); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		return 0, fmt.Errorf("暂停进程失败: %w", err)
	}

	// 设置cgroup
	if err := utils.SetCgroupForProcess(cgroupPath, pgid); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		return 0, fmt.Errorf("设置cgroup失败: %w", err)
	}

	// 恢复进程执行
	if err := syscall.Kill(pgid, syscall.SIGCONT); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		return 0, fmt.Errorf("恢复进程失败: %w", err)
	}

	return pgid, nil
}

// waitForCompletion 等待命令完成并收集结果
func (e *SandboxExecutor) waitForCompletion(cmd *exec.Cmd, ctx context.Context, workspace *Workspace,
	testCase *model2.DataTestCase, result *model2.DataJudgeCase, cgroupPath string, pgid int,
	startTime time.Time, stdoutBuf, stderrBuf *bytes.Buffer) (*model2.DataJudgeCase, error) {

	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	select {
	case <-ctx.Done():
		return e.handleTimeout(workspace, result, cgroupPath, pgid, startTime, stdoutBuf, stderrBuf)
	case err := <-done:
		return e.handleCommandResult(workspace, testCase, result, cgroupPath, startTime, stdoutBuf, stderrBuf, err)
	}
}

// handleTimeout 处理超时情况
func (e *SandboxExecutor) handleTimeout(workspace *Workspace, result *model2.DataJudgeCase,
	cgroupPath string, pgid int, startTime time.Time, stdoutBuf, stderrBuf *bytes.Buffer) (*model2.DataJudgeCase, error) {

	// 杀死进程组
	syscall.Kill(-pgid, syscall.SIGKILL)

	// 收集执行结果
	elapsed := time.Since(startTime)
	e.collectExecutionMetrics(result, cgroupPath, elapsed)

	result.OutputData = stdoutBuf.String()
	result.Status = "TIME_LIMIT_EXCEEDED"

	// 检查错误输出
	if stderr := strings.TrimSpace(stderrBuf.String()); stderr != "" {
		result.Message = stderr
	}

	logx.Errorf("超时杀死进程组，运行时间: %v ms，时间限制: %v ms", elapsed.Milliseconds(), workspace.judgeRequest.MaxTime)
	return result, nil
}

// handleCommandResult 处理命令执行结果
func (e *SandboxExecutor) handleCommandResult(workspace *Workspace, testCase *model2.DataTestCase,
	result *model2.DataJudgeCase, cgroupPath string, startTime time.Time,
	stdoutBuf, stderrBuf *bytes.Buffer, cmdErr error) (*model2.DataJudgeCase, error) {

	elapsed := time.Since(startTime)
	e.collectExecutionMetrics(result, cgroupPath, elapsed)

	result.OutputData = stdoutBuf.String()

	// 判断执行状态
	result.Status = e.determineExecutionStatus(workspace, result, cgroupPath, elapsed, cmdErr, stderrBuf.String(), testCase)

	logx.Infof("运行完成 - 状态: %s, 时间: %.2f ms, 内存: %.2f KB",
		result.Status, result.MaxTime, result.MaxMemory)

	return result, nil
}

// collectExecutionMetrics 收集执行指标
func (e *SandboxExecutor) collectExecutionMetrics(result *model2.DataJudgeCase,
	cgroupPath string, elapsed time.Duration) {

	result.MaxTime = float64(elapsed.Milliseconds())

	if memoryUsed, err := utils.GetMemoryUsage(cgroupPath); err == nil {
		result.MaxMemory = utils.FormatBytesKB(memoryUsed)
	}
}

// determineExecutionStatus 判断执行状态
func (e *SandboxExecutor) determineExecutionStatus(workspace *Workspace, result *model2.DataJudgeCase,
	cgroupPath string, elapsed time.Duration, cmdErr error, stderr string, testCase *model2.DataTestCase) string {

	// 检查时间限制
	if elapsed > time.Duration(workspace.judgeRequest.MaxTime)*time.Millisecond {
		result.Score = 0
		return "TIME_LIMIT_EXCEEDED"
	}

	// 检查内存限制
	if utils.CheckOOMEvent(cgroupPath) {
		result.Score = 0
		return "MEMORY_LIMIT_EXCEEDED"
	}

	// 检查运行时错误
	if cmdErr != nil {
		result.Score = 0
		result.Message = cmdErr.Error()
		return "RUNTIME_ERROR"
	}

	// 检查错误输出
	if strings.TrimSpace(stderr) != "" {
		result.Score = 0
		result.Message = stderr
		return "RUNTIME_ERROR"
	}

	result.Score = testCase.Score
	return "RUN_SUCCESS"
}

// executeTestCases 并发执行测试用例
func (w *Workspace) executeTestCases() ([]*model2.DataJudgeCase, error) {
	testCases, err := w.svcCtx.TestCaseRepo().GetTestCasesByProblemIDWithSample(w.ctx, w.judgeRequest.ProblemId, w.judgeRequest.SubmitType)
	if err != nil {
		return nil, fmt.Errorf("获取测试用例失败: %w", err)
	}

	results := make([]*model2.DataJudgeCase, 0, len(testCases))
	var wg sync.WaitGroup
	resultChan := make(chan *model2.DataJudgeCase, len(testCases))
	errChan := make(chan error, len(testCases))

	// 控制并发执行的测试用例数量
	semaphore := make(chan struct{}, 5)

	for _, testCase := range testCases {
		wg.Add(1)
		semaphore <- struct{}{}

		go func(tc model2.DataTestCase) {
			defer wg.Done()
			defer func() { <-semaphore }()

			executor := GetExecutor()
			defer ReleaseExecutor(executor)

			result, err := executor.Execute(w, &tc)
			if err != nil {
				logx.Errorf("执行测试用例 %s 失败: %v", tc.CaseSign, err)
				errChan <- err
				return
			}

			resultChan <- result
		}(testCase)
	}

	wg.Wait()
	close(resultChan)
	close(errChan)

	// 收集结果
	for result := range resultChan {
		results = append(results, result)
	}

	// 检查错误
	if len(results) == 0 && len(testCases) > 0 {
		return nil, fmt.Errorf("所有测试用例执行失败")
	}

	return results, nil
}

// handleError 统一错误处理
func (e *SandboxExecutor) handleError(result *model2.DataJudgeCase, message string, exitCode int, err error) (*model2.DataJudgeCase, error) {
	result.Status = "SystemError"
	result.Message = message
	result.ExitCode = exitCode
	logx.Errorf("执行错误: %s, 原始错误: %v", message, err)
	return result, fmt.Errorf("%s: %w", message, err)
}

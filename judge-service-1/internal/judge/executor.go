// package judge

// import (
// 	"bytes"
// 	"context"
// 	"fmt"
// 	"judge-service/internal/grutil"
// 	"judge-service/internal/model"
// 	"os/exec"
// 	"strings"
// 	"sync"
// 	"syscall"
// 	"time"

// 	"github.com/zeromicro/go-zero/core/logx"
// )

// type Executor interface {
// 	Execute(workspace *Workspace, testCase *model.DataTestCase) (*model.DataJudgeCase, error)
// }

// type SandboxExecutor struct {
// 	mu sync.Mutex // 添加互斥锁防止竞争条件
// }

// func (e *SandboxExecutor) Execute(workspace *Workspace, testCase *model.DataTestCase) (*model.DataJudgeCase, error) {
// 	startTime := time.Now()

// 	result := &model.DataJudgeCase{
// 		SubmitID: workspace.judgeRequest.ID,
// 		CaseSign: testCase.CaseSign,
// 		IsSample: testCase.IsSample,
// 	}

// 	cgroupPath, err := grutil.CreateCgroup(workspace.judgeRequest.MaxMemory)
// 	if err != nil {
// 		return e.handleError(result, fmt.Sprintf("创建cgroup失败: %v", err), 1, err)
// 	}
// 	defer grutil.CleanupCgroup(cgroupPath) // 使用defer确保资源清理

// 	runCmd := grutil.GetRunCommand(workspace.langConfig, workspace.SourceFile, workspace.BuildFile)

// 	// 执行超时保护，默认执行时间限制 + 配置时间限制
// 	timeout := time.Duration(workspace.judgeRequest.MaxTime)*time.Millisecond + 30*time.Millisecond
// 	ctx, cancel := context.WithTimeout(context.Background(), timeout)
// 	defer cancel() // 设置超时时间，根据提交的最大时间限制

// 	cmd := exec.CommandContext(ctx, runCmd[0], runCmd[1:]...)
// 	// 设置进程属性，创建新的命名空间进行隔离
// 	cmd.SysProcAttr = &syscall.SysProcAttr{
// 		Setpgid: true, // 设置进程组ID，便于管理进程组
// 		Cloneflags: syscall.CLONE_NEWNS | // 新的挂载命名空间
// 			syscall.CLONE_NEWUTS | // 新的UTS命名空间（主机名和域名）
// 			syscall.CLONE_NEWPID | // 新的PID命名空间
// 			syscall.CLONE_NEWNET | // 新的网络命名空间
// 			syscall.CLONE_NEWIPC, // 新的IPC命名空间
// 		Unshareflags: syscall.CLONE_NEWNS, // 取消共享挂载命名空间
// 	}

// 	// 设置标准输入为测试用例的输入
// 	cmd.Stdin = strings.NewReader(testCase.InputData)
// 	var stdoutBuf, stderrBuf bytes.Buffer
// 	cmd.Stdout = &stdoutBuf
// 	cmd.Stderr = &stderrBuf

// 	// 启动命令
// 	err = cmd.Start()
// 	if err != nil {
// 		result.Status = "errore"
// 		result.Message = fmt.Sprintf("启动进程失败: %v", err)
// 		return e.handleError(result, fmt.Sprintf("启动进程失败: %v", err), 1, err)
// 	}
// 	// 加锁确保对进程操作的原子性
// 	e.mu.Lock()

// 	// 立即暂停进程，以便设置cgroup
// 	syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP)

// 	// 获取进程组ID
// 	pgid := cmd.Process.Pid

// 	// 将进程添加到cgroup进行资源限制
// 	if err := grutil.SetCgroupForProcess(cgroupPath, pgid); err != nil {
// 		syscall.Kill(-pgid, syscall.SIGKILL) // 将进程添加到cgroup进行资源限制
// 		result.Status = "RuntimeError"
// 		result.Message = fmt.Sprintf("设置cgroup失败: %v", err)
// 		return e.handleError(result, fmt.Sprintf("设置cgroup失败: %v", err), 1, err)
// 	}

// 	// 将进程添加到cgroup进行资源限制
// 	syscall.Kill(cmd.Process.Pid, syscall.SIGCONT)
// 	e.mu.Unlock() // 解锁，允许其他操作

// 	done := make(chan error, 1) // 创建通道等待命令执行完成
// 	go func() {
// 		done <- cmd.Wait()
// 	}()

// 	select {
// 	case <-ctx.Done():
// 		// 获取内存使用情况
// 		syscall.Kill(-pgid, syscall.SIGKILL)
// 		elapsed := time.Since(startTime)
// 		// 获取内存使用情况
// 		memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
// 		result.MaxMemory = grutil.FormatBytesKB(memoryUsed)
// 		result.MaxTime = float64(elapsed.Milliseconds())
// 		result.Status = "TimeLimitExceeded"
// 		result.OutputData = stdoutBuf.String()
// 		// 检查是否有错误输出
// 		stderr := stderrBuf.String()
// 		if strings.TrimSpace(stderr) != "" {
// 			result.Message = stderr
// 			result.Status = "RuntimeError"
// 		}
// 		// // 超时情况下退出码通常不可用，设置为特定值
// 		// testCase.ExitCode = -1 // 或者使用其他特殊值表示超时

// 		logx.Errorf("超时杀死整个进程组，运行时间: %v ms 时间限制 %v ms", elapsed, workspace.judgeRequest.MaxTime)
// 	case err := <-done:
// 		// 命令正常完成或出错
// 		elapsed := time.Since(startTime)
// 		memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
// 		result.MaxMemory = grutil.FormatBytesKB(memoryUsed)
// 		result.MaxTime = float64(elapsed.Milliseconds())
// 		// 时间判断
// 		if elapsed > time.Duration(workspace.judgeRequest.MaxTime)*time.Millisecond {
// 			result.Status = "TimeLimitExceeded"
// 		}

// 		// 检查是否发生内存溢出
// 		if grutil.CheckOOMEvent(cgroupPath) {
// 			result.Status = "MemoryLimitExceeded"
// 		}
// 		result.OutputData = stdoutBuf.String()
// 		if err != nil {
// 			result.Message = err.Error()
// 			result.Status = "RuntimeError"
// 		}
// 		// 检查是否有错误输出
// 		stderr := stderrBuf.String()
// 		if strings.TrimSpace(stderr) != "" {
// 			result.Message = stderr
// 			result.Status = "RuntimeError"
// 		}

// 		logx.Infof("运行时间: %f ms 时间限制 %f ms, 内存使用: %f KB", result.MaxTime, workspace.judgeRequest.MaxTime, result.MaxMemory)
// 	}

// 	return result, nil
// }

// func (w *Workspace) executeTestCases() ([]*model.DataJudgeCase, error) {
// 	testCases, err := w.svcCtx.TestCaseRepo().GetTestCasesByProblemID(w.ctx, w.judgeRequest.ProblemId)
// 	if err != nil {
// 		return nil, err
// 	}

// 	executor := &SandboxExecutor{}
// 	results := make([]*model.DataJudgeCase, 0, len(testCases))

// 	for _, testCase := range testCases {
// 		result, err := executor.Execute(w, &testCase)
// 		if err != nil {
// 			return nil, err
// 		}

// 		dataJudgeCase := &model.DataJudgeCase{
// 			SubmitID:       w.judgeRequest.ID,
// 			CaseSign:       testCase.CaseSign,
// 			InputData:      testCase.InputData,
// 			OutputData:     result.OutputData,
// 			ExpectedOutput: testCase.ExpectedOutput,
// 			MaxTime:        result.MaxTime,
// 			MaxMemory:      result.MaxMemory,
// 			IsSample:       result.IsSample,
// 			Message:        result.Message,
// 		}

// 		results = append(results, dataJudgeCase)
// 	}

// 	return results, nil
// }

// // 统一错误处理
// func (e *SandboxExecutor) handleError(result *model.DataJudgeCase, message string, exitCode int, err error) (*model.DataJudgeCase, error) {
// 	result.Message = message
// 	result.ExitCode = exitCode
// 	logx.Errorf(message)
// 	return result, err
// }

package judge

import (
	"bytes"
	"context"
	"fmt"
	"judge-service/internal/grutil"
	"judge-service/internal/model"
	"judge-service/internal/snowflake"
	"os/exec"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

// Executor 执行器接口
type Executor interface {
	Execute(workspace *Workspace, testCase *model.DataTestCase) (*model.DataJudgeCase, error)
}

// SandboxExecutor 沙箱执行器
type SandboxExecutor struct {
	mu sync.Mutex
}

// Execute 执行测试用例
func (e *SandboxExecutor) Execute(workspace *Workspace, testCase *model.DataTestCase) (*model.DataJudgeCase, error) {
	startTime := time.Now()

	// 初始化结果对象
	result := e.initResult(workspace, testCase)

	// 创建cgroup进行资源限制
	cgroupPath, err := grutil.CreateCgroup(workspace.judgeRequest.MaxMemory)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("创建cgroup失败: %v", err), 1, err)
	}
	defer grutil.CleanupCgroup(cgroupPath)

	// 执行命令并获取结果
	return e.executeCommand(workspace, testCase, result, cgroupPath, startTime)
}

// initResult 初始化结果对象
func (e *SandboxExecutor) initResult(workspace *Workspace, testCase *model.DataTestCase) *model.DataJudgeCase {
	now := time.Now()
	return &model.DataJudgeCase{
		ID:             snowflake.GenerateID(),
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
func (e *SandboxExecutor) executeCommand(workspace *Workspace, testCase *model.DataTestCase,
	result *model.DataJudgeCase, cgroupPath string, startTime time.Time) (*model.DataJudgeCase, error) {

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
func (e *SandboxExecutor) prepareCommand(workspace *Workspace, testCase *model.DataTestCase) (
	*exec.Cmd, *bytes.Buffer, *bytes.Buffer, context.Context, context.CancelFunc, error) {

	runCmd := grutil.GetRunCommand(workspace.langConfig, workspace.SourceFile, workspace.BuildFile)

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

	e.mu.Lock()
	defer e.mu.Unlock()

	pgid := cmd.Process.Pid

	// 暂停进程以便设置cgroup
	if err := syscall.Kill(pgid, syscall.SIGSTOP); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		return 0, fmt.Errorf("暂停进程失败: %w", err)
	}

	// 设置cgroup
	if err := grutil.SetCgroupForProcess(cgroupPath, pgid); err != nil {
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
	testCase *model.DataTestCase, result *model.DataJudgeCase, cgroupPath string, pgid int,
	startTime time.Time, stdoutBuf, stderrBuf *bytes.Buffer) (*model.DataJudgeCase, error) {

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
func (e *SandboxExecutor) handleTimeout(workspace *Workspace, result *model.DataJudgeCase,
	cgroupPath string, pgid int, startTime time.Time, stdoutBuf, stderrBuf *bytes.Buffer) (*model.DataJudgeCase, error) {

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
func (e *SandboxExecutor) handleCommandResult(workspace *Workspace, testCase *model.DataTestCase,
	result *model.DataJudgeCase, cgroupPath string, startTime time.Time,
	stdoutBuf, stderrBuf *bytes.Buffer, cmdErr error) (*model.DataJudgeCase, error) {

	elapsed := time.Since(startTime)
	e.collectExecutionMetrics(result, cgroupPath, elapsed)

	result.OutputData = stdoutBuf.String()

	// 判断执行状态
	result.Status = e.determineExecutionStatus(workspace, result, cgroupPath, elapsed, cmdErr, stderrBuf.String())

	logx.Infof("运行完成 - 状态: %s, 时间: %.2f ms, 内存: %.2f KB",
		result.Status, result.MaxTime, result.MaxMemory)

	return result, nil
}

// collectExecutionMetrics 收集执行指标
func (e *SandboxExecutor) collectExecutionMetrics(result *model.DataJudgeCase,
	cgroupPath string, elapsed time.Duration) {

	result.MaxTime = float64(elapsed.Milliseconds())

	if memoryUsed, err := grutil.GetMemoryUsage(cgroupPath); err == nil {
		result.MaxMemory = grutil.FormatBytesKB(memoryUsed)
	}
}

// determineExecutionStatus 判断执行状态
func (e *SandboxExecutor) determineExecutionStatus(workspace *Workspace, result *model.DataJudgeCase,
	cgroupPath string, elapsed time.Duration, cmdErr error, stderr string) string {

	// 检查时间限制
	if elapsed > time.Duration(workspace.judgeRequest.MaxTime)*time.Millisecond {
		return "TIME_LIMIT_EXCEEDED"
	}

	// 检查内存限制
	if grutil.CheckOOMEvent(cgroupPath) {
		return "MEMORY_LIMIT_EXCEEDED"
	}

	// 检查运行时错误
	if cmdErr != nil {
		result.Message = cmdErr.Error()
		return "RUNTIME_ERROR"
	}

	// 检查错误输出
	if strings.TrimSpace(stderr) != "" {
		result.Message = stderr
		return "RUNTIME_ERROR"
	}

	return "RUN_SUCCESS"
}

// executeTestCases 执行测试用例集
func (w *Workspace) executeTestCases() ([]*model.DataJudgeCase, error) {
	// testCases, err := w.svcCtx.TestCaseRepo().GetTestCasesByProblemID(w.ctx, w.judgeRequest.ProblemId)
	testCases, err := w.svcCtx.TestCaseRepo().GetTestCasesByProblemIDWithSample(w.ctx, w.judgeRequest.ProblemId, w.judgeRequest.SubmitType)
	if err != nil {
		return nil, fmt.Errorf("获取测试用例失败: %w", err)
	}

	executor := &SandboxExecutor{}
	results := make([]*model.DataJudgeCase, 0, len(testCases))

	for _, testCase := range testCases {
		result, err := executor.Execute(w, &testCase)
		if err != nil {
			// 记录错误但继续执行其他测试用例
			logx.Errorf("执行测试用例 %s 失败: %v", testCase.CaseSign, err)
			continue
		}
		results = append(results, result)
	}

	if len(results) == 0 && len(testCases) > 0 {
		return nil, fmt.Errorf("所有测试用例执行失败")
	}

	return results, nil
}

// handleError 统一错误处理
func (e *SandboxExecutor) handleError(result *model.DataJudgeCase, message string, exitCode int, err error) (*model.DataJudgeCase, error) {
	result.Status = "SystemError"
	result.Message = message
	result.ExitCode = exitCode
	logx.Errorf("执行错误: %s, 原始错误: %v", message, err)
	return result, fmt.Errorf("%s: %w", message, err)
}

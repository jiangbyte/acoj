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

// type ExecuteResult struct {
// 	Output string
// 	Error  string

// 	TimeLimitExceeded   bool
// 	MemoryLimitExceeded bool

// 	Success   bool
// 	Message   string
// 	ExitCode  int
// 	MaxTime   float64
// 	MaxMemory float64
// }

// type Executor interface {
// 	Execute(workspace *Workspace, testCase *model.DataTestCase) (*model.DataJudgeCase, error)
// }

// type SandboxExecutor struct {
// 	mu sync.Mutex // 添加互斥锁防止竞争条件
// }

// func (e *SandboxExecutor) Execute(workspace *Workspace, testCase *model.DataTestCase) (*model.DataJudgeCase, error) {
// 	startTime := time.Now()

// 	result := &model.DataJudgeCase{}

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
// 			SubmitID:   w.judgeRequest.ID,
// 			CaseSign:   testCase.CaseSign,
// 			InputData:  testCase.InputData,
// 			OutputData: result.OutputData,
// 			MaxTime:    result.MaxTime,
// 			MaxMemory:  result.MaxMemory,
// 			IsSample:   result.IsSample,
// 			Message:    result.Message,
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
	"os/exec"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

type ExecuteResult struct {
	Output string
	Error  string

	TimeLimitExceeded   bool
	MemoryLimitExceeded bool

	Success   bool
	Message   string
	ExitCode  int
	MaxTime   float64
	MaxMemory float64
}

type Executor interface {
	Execute(workspace *Workspace, testCase *model.DataTestCase) (*model.DataJudgeCase, error)
}

type SandboxExecutor struct {
	mu sync.Mutex
}

// 执行配置常量
const (
	extraTimeout    = 30 * time.Millisecond // 额外超时缓冲
	defaultExitCode = 1
	timeoutExitCode = -1
)

// Execute 执行测试用例
func (e *SandboxExecutor) Execute(workspace *Workspace, testCase *model.DataTestCase) (*model.DataJudgeCase, error) {
	startTime := time.Now()
	result := &model.DataJudgeCase{}

	cgroupPath, err := grutil.CreateCgroup(workspace.judgeRequest.MaxMemory)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("创建cgroup失败: %v", err), defaultExitCode, err)
	}
	defer grutil.CleanupCgroup(cgroupPath)

	runCmd := grutil.GetRunCommand(workspace.langConfig, workspace.SourceFile, workspace.BuildFile)
	timeout := e.calculateTimeout(workspace.judgeRequest.MaxTime)

	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	cmd, err := e.createCommand(ctx, runCmd, testCase.InputData)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("创建命令失败: %v", err), defaultExitCode, err)
	}

	if err := cmd.Start(); err != nil {
		return e.handleError(result, fmt.Sprintf("启动进程失败: %v", err), defaultExitCode, err)
	}

	pgid, err := e.controlProcess(cmd, cgroupPath)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("进程控制失败: %v", err), defaultExitCode, err)
	}

	return e.waitForCompletion(cmd, ctx, pgid, cgroupPath, workspace, testCase, startTime, result)
}

// calculateTimeout 计算执行超时时间
func (e *SandboxExecutor) calculateTimeout(maxTime float64) time.Duration {
	return time.Duration(maxTime)*time.Millisecond + extraTimeout
}

// createCommand 创建执行命令
func (e *SandboxExecutor) createCommand(ctx context.Context, runCmd []string, inputData string) (*exec.Cmd, error) {
	cmd := exec.CommandContext(ctx, runCmd[0], runCmd[1:]...)

	cmd.SysProcAttr = &syscall.SysProcAttr{
		Setpgid: true,
		Cloneflags: syscall.CLONE_NEWNS |
			syscall.CLONE_NEWUTS |
			syscall.CLONE_NEWPID |
			syscall.CLONE_NEWNET |
			syscall.CLONE_NEWIPC,
		Unshareflags: syscall.CLONE_NEWNS,
	}

	cmd.Stdin = strings.NewReader(inputData)
	return cmd, nil
}

// controlProcess 控制进程：暂停→设置cgroup→恢复
func (e *SandboxExecutor) controlProcess(cmd *exec.Cmd, cgroupPath string) (int, error) {
	e.mu.Lock()
	defer e.mu.Unlock()

	// 暂停进程
	if err := syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP); err != nil {
		return 0, fmt.Errorf("暂停进程失败: %v", err)
	}

	pgid := cmd.Process.Pid
	if err := grutil.SetCgroupForProcess(cgroupPath, pgid); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		return 0, fmt.Errorf("设置cgroup失败: %v", err)
	}

	// 恢复进程
	if err := syscall.Kill(cmd.Process.Pid, syscall.SIGCONT); err != nil {
		return 0, fmt.Errorf("恢复进程失败: %v", err)
	}

	return pgid, nil
}

// waitForCompletion 等待命令完成
func (e *SandboxExecutor) waitForCompletion(cmd *exec.Cmd, ctx context.Context, pgid int,
	cgroupPath string, workspace *Workspace, testCase *model.DataTestCase,
	startTime time.Time, result *model.DataJudgeCase) (*model.DataJudgeCase, error) {

	var stdoutBuf, stderrBuf bytes.Buffer
	cmd.Stdout = &stdoutBuf
	cmd.Stderr = &stderrBuf

	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	select {
	case <-ctx.Done():
		return e.handleTimeout(pgid, cgroupPath, &stdoutBuf, &stderrBuf, workspace, startTime, result)
	case err := <-done:
		return e.handleCompletion(err, cgroupPath, &stdoutBuf, &stderrBuf, workspace, testCase, startTime, result)
	}
}

// handleTimeout 处理超时情况
func (e *SandboxExecutor) handleTimeout(pgid int, cgroupPath string, stdoutBuf, stderrBuf *bytes.Buffer,
	workspace *Workspace, startTime time.Time, result *model.DataJudgeCase) (*model.DataJudgeCase, error) {

	syscall.Kill(-pgid, syscall.SIGKILL)

	elapsed := time.Since(startTime)
	memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)

	e.setResultMetrics(result, elapsed, memoryUsed)
	result.OutputData = stdoutBuf.String()
	result.Status = "TimeLimitExceeded"

	if stderr := strings.TrimSpace(stderrBuf.String()); stderr != "" {
		result.Message = stderr
		result.Status = "RuntimeError"
	}

	logx.Errorf("超时杀死进程组，运行时间: %v ms，时间限制: %v ms",
		elapsed.Milliseconds(), workspace.judgeRequest.MaxTime)

	return result, nil
}

// handleCompletion 处理命令完成情况
func (e *SandboxExecutor) handleCompletion(err error, cgroupPath string, stdoutBuf, stderrBuf *bytes.Buffer,
	workspace *Workspace, testCase *model.DataTestCase, startTime time.Time, result *model.DataJudgeCase) (*model.DataJudgeCase, error) {

	elapsed := time.Since(startTime)
	memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)

	e.setResultMetrics(result, elapsed, memoryUsed)
	result.OutputData = stdoutBuf.String()

	// 检查各种错误情况
	e.evaluateResult(result, err, cgroupPath, stderrBuf, workspace, elapsed)

	logx.Infof("运行完成，时间: %.1f ms，内存: %.1f KB，状态: %s",
		result.MaxTime, result.MaxMemory, result.Status)

	return result, nil
}

// setResultMetrics 设置结果指标
func (e *SandboxExecutor) setResultMetrics(result *model.DataJudgeCase, elapsed time.Duration, memoryUsed uint64) {
	result.MaxTime = float64(elapsed.Milliseconds())
	result.MaxMemory = grutil.FormatBytesKB(memoryUsed)
}

// evaluateResult 评估执行结果
func (e *SandboxExecutor) evaluateResult(result *model.DataJudgeCase, err error, cgroupPath string,
	stderrBuf *bytes.Buffer, workspace *Workspace, elapsed time.Duration) {

	// 检查时间限制
	if elapsed > time.Duration(workspace.judgeRequest.MaxTime)*time.Millisecond {
		result.Status = "TimeLimitExceeded"
		return
	}

	// 检查内存溢出
	if grutil.CheckOOMEvent(cgroupPath) {
		result.Status = "MemoryLimitExceeded"
		return
	}

	// 检查执行错误
	if err != nil {
		result.Message = err.Error()
		result.Status = "RuntimeError"
		return
	}

	// 检查错误输出
	if stderr := strings.TrimSpace(stderrBuf.String()); stderr != "" {
		result.Message = stderr
		result.Status = "RuntimeError"
		return
	}

	// 正常完成
	result.Status = "Completed"
}

// handleError 统一错误处理
func (e *SandboxExecutor) handleError(result *model.DataJudgeCase, message string, exitCode int, err error) (*model.DataJudgeCase, error) {
	result.Message = message
	result.ExitCode = exitCode
	result.Status = "RuntimeError"
	logx.Errorf("执行错误: %s", message)
	return result, err
}

// executeTestCases 执行所有测试用例
func (w *Workspace) executeTestCases() ([]*model.DataJudgeCase, error) {
	testCases, err := w.svcCtx.TestCaseRepo().GetTestCasesByProblemID(w.ctx, w.judgeRequest.ProblemId)
	if err != nil {
		return nil, fmt.Errorf("获取测试用例失败: %v", err)
	}

	executor := &SandboxExecutor{}
	results := make([]*model.DataJudgeCase, 0, len(testCases))

	for _, testCase := range testCases {
		result, err := executor.Execute(w, &testCase)
		if err != nil {
			return nil, fmt.Errorf("执行测试用例失败: %v", err)
		}

		judgeCase := &model.DataJudgeCase{
			SubmitID:   w.judgeRequest.ID,
			CaseSign:   testCase.CaseSign,
			InputData:  testCase.InputData,
			OutputData: result.OutputData,
			MaxTime:    result.MaxTime,
			MaxMemory:  result.MaxMemory,
			IsSample:   result.IsSample,
			Message:    result.Message,
			Status:     result.Status,
			ExitCode:   result.ExitCode,
		}

		results = append(results, judgeCase)
	}

	return results, nil
}

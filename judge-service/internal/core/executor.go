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
	result := e.initResult(workspace, testCase)

	soft := utils.IsSoftSandbox()
	var cgroupPath string
	if !soft {
		path, err := utils.CreateCgroup(workspace.judgeRequest.MaxMemory)
		if err != nil {
			return e.handleError(result, fmt.Sprintf("创建cgroup失败: %v", err), 1, err)
		}
		cgroupPath = path
		defer utils.CleanupCgroup(cgroupPath)
	}

	return e.executeCommand(workspace, testCase, result, cgroupPath, soft, startTime)
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
		InputFilePath:  "",
		InputFileSize:  0,
		OutputFilePath: "",
		OutputFileSize: 0,
		MaxTime:        0.00,
		MaxMemory:      0.00,
		Message:        "",
		ExitCode:       0,
		Deleted:        false,
		CreateTime:     &now,
		CreateUser:     "0",
		UpdateTime:     &now,
		UpdateUser:     "0",
	}
}

// executeCommand 执行命令并处理结果
func (e *SandboxExecutor) executeCommand(workspace *Workspace, testCase *model2.DataTestCase,
	result *model2.DataJudgeCase, cgroupPath string, soft bool, startTime time.Time) (*model2.DataJudgeCase, error) {

	cmd, stdoutBuf, stderrBuf, ctx, cancel, err := e.prepareCommand(workspace, testCase, soft)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("准备命令失败: %v", err), 1, err)
	}
	defer cancel()

	pgid, watcher, err := e.startAndManageProcess(cmd, cgroupPath, soft, workspace.judgeRequest.MaxMemory)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("进程管理失败: %v", err), 1, err)
	}
	if watcher != nil {
		defer watcher.Stop()
	}

	return e.waitForCompletion(cmd, ctx, workspace, testCase, result, cgroupPath, soft, watcher, pgid, startTime, stdoutBuf, stderrBuf)
}

// prepareCommand 准备命令执行环境
func (e *SandboxExecutor) prepareCommand(workspace *Workspace, testCase *model2.DataTestCase, soft bool) (
	*exec.Cmd, *bytes.Buffer, *bytes.Buffer, context.Context, context.CancelFunc, error) {

	runCmd := utils.GetRunCommand(workspace.langConfig, workspace.SourceFile, workspace.BuildFile)

	timeout := time.Duration(workspace.judgeRequest.MaxTime)*time.Millisecond + 30*time.Millisecond
	ctx, cancel := context.WithTimeout(context.Background(), timeout)

	cmd := exec.CommandContext(ctx, runCmd[0], runCmd[1:]...)
	cmd.SysProcAttr = buildSysProcAttr(soft)

	cmd.Stdin = strings.NewReader(testCase.InputData)
	var stdoutBuf, stderrBuf bytes.Buffer
	cmd.Stdout = &stdoutBuf
	cmd.Stderr = &stderrBuf

	return cmd, &stdoutBuf, &stderrBuf, ctx, cancel, nil
}

// startAndManageProcess 启动并管理进程；soft 模式启动 /proc 内存监视
func (e *SandboxExecutor) startAndManageProcess(cmd *exec.Cmd, cgroupPath string, soft bool, maxMemoryKB float64) (int, *utils.MemoryWatcher, error) {
	if err := cmd.Start(); err != nil {
		return 0, nil, fmt.Errorf("启动进程失败: %w", err)
	}

	pgid := cmd.Process.Pid

	if soft {
		limitBytes := utils.MemoryLimitBytesFromKB(maxMemoryKB)
		watcher := utils.StartMemoryWatcher(pgid, limitBytes, 0)
		return pgid, watcher, nil
	}

	if err := syscall.Kill(pgid, syscall.SIGSTOP); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		return 0, nil, fmt.Errorf("暂停进程失败: %w", err)
	}

	if err := utils.SetCgroupForProcess(cgroupPath, pgid); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		return 0, nil, fmt.Errorf("设置cgroup失败: %w", err)
	}

	if err := syscall.Kill(pgid, syscall.SIGCONT); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		return 0, nil, fmt.Errorf("恢复进程失败: %w", err)
	}

	return pgid, nil, nil
}

// waitForCompletion 等待命令完成并收集结果
func (e *SandboxExecutor) waitForCompletion(cmd *exec.Cmd, ctx context.Context, workspace *Workspace,
	testCase *model2.DataTestCase, result *model2.DataJudgeCase, cgroupPath string, soft bool,
	watcher *utils.MemoryWatcher, pgid int, startTime time.Time, stdoutBuf, stderrBuf *bytes.Buffer) (*model2.DataJudgeCase, error) {

	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	var exceedCh <-chan struct{}
	if watcher != nil {
		exceedCh = watcher.ExceededChan()
	} else {
		exceedCh = make(chan struct{})
	}

	select {
	case <-exceedCh:
		syscall.Kill(-pgid, syscall.SIGKILL)
		<-done
		return e.handleSoftMLE(workspace, result, watcher, startTime, stdoutBuf, stderrBuf)
	case <-ctx.Done():
		return e.handleTimeout(workspace, result, cgroupPath, soft, watcher, pgid, startTime, stdoutBuf, stderrBuf)
	case err := <-done:
		return e.handleCommandResult(workspace, testCase, result, cgroupPath, soft, watcher, startTime, stdoutBuf, stderrBuf, err)
	}
}

func (e *SandboxExecutor) handleSoftMLE(workspace *Workspace, result *model2.DataJudgeCase,
	watcher *utils.MemoryWatcher, startTime time.Time, stdoutBuf, stderrBuf *bytes.Buffer) (*model2.DataJudgeCase, error) {

	elapsed := time.Since(startTime)
	result.MaxTime = float64(elapsed.Milliseconds())
	if watcher != nil {
		result.MaxMemory = utils.FormatBytesKB(watcher.Peak())
	}
	result.OutputData = stdoutBuf.String()
	result.Score = 0
	result.Status = "MEMORY_LIMIT_EXCEEDED"
	if stderr := strings.TrimSpace(stderrBuf.String()); stderr != "" {
		result.Message = stderr
	}
	logx.Infof("soft MLE - 时间: %.2f ms, 内存: %.2f KB, 限制: %.2f KB",
		result.MaxTime, result.MaxMemory, workspace.judgeRequest.MaxMemory)
	return result, nil
}

// handleTimeout 处理超时情况
func (e *SandboxExecutor) handleTimeout(workspace *Workspace, result *model2.DataJudgeCase,
	cgroupPath string, soft bool, watcher *utils.MemoryWatcher, pgid int, startTime time.Time,
	stdoutBuf, stderrBuf *bytes.Buffer) (*model2.DataJudgeCase, error) {

	syscall.Kill(-pgid, syscall.SIGKILL)

	elapsed := time.Since(startTime)
	e.collectExecutionMetrics(result, cgroupPath, soft, watcher, elapsed)

	result.OutputData = stdoutBuf.String()
	result.Status = "TIME_LIMIT_EXCEEDED"

	if stderr := strings.TrimSpace(stderrBuf.String()); stderr != "" {
		result.Message = stderr
	}

	logx.Errorf("超时杀死进程组，运行时间: %v ms，时间限制: %v ms", elapsed.Milliseconds(), workspace.judgeRequest.MaxTime)
	return result, nil
}

// handleCommandResult 处理命令执行结果
func (e *SandboxExecutor) handleCommandResult(workspace *Workspace, testCase *model2.DataTestCase,
	result *model2.DataJudgeCase, cgroupPath string, soft bool, watcher *utils.MemoryWatcher, startTime time.Time,
	stdoutBuf, stderrBuf *bytes.Buffer, cmdErr error) (*model2.DataJudgeCase, error) {

	elapsed := time.Since(startTime)
	e.collectExecutionMetrics(result, cgroupPath, soft, watcher, elapsed)

	result.OutputData = stdoutBuf.String()
	result.Status = e.determineExecutionStatus(workspace, result, cgroupPath, soft, watcher, elapsed, cmdErr, stderrBuf.String(), testCase)

	logx.Infof("运行完成 - 状态: %s, 时间: %.2f ms, 内存: %.2f KB",
		result.Status, result.MaxTime, result.MaxMemory)

	return result, nil
}

// collectExecutionMetrics 收集执行指标
func (e *SandboxExecutor) collectExecutionMetrics(result *model2.DataJudgeCase,
	cgroupPath string, soft bool, watcher *utils.MemoryWatcher, elapsed time.Duration) {

	result.MaxTime = float64(elapsed.Milliseconds())

	if soft {
		if watcher != nil {
			result.MaxMemory = utils.FormatBytesKB(watcher.Peak())
		}
		return
	}

	if memoryUsed, err := utils.GetMemoryUsage(cgroupPath); err == nil {
		result.MaxMemory = utils.FormatBytesKB(memoryUsed)
	}
}

// determineExecutionStatus 判断执行状态
func (e *SandboxExecutor) determineExecutionStatus(workspace *Workspace, result *model2.DataJudgeCase,
	cgroupPath string, soft bool, watcher *utils.MemoryWatcher, elapsed time.Duration, cmdErr error, stderr string, testCase *model2.DataTestCase) string {

	if elapsed > time.Duration(workspace.judgeRequest.MaxTime)*time.Millisecond {
		result.Score = 0
		return "TIME_LIMIT_EXCEEDED"
	}

	limitBytes := utils.MemoryLimitBytesFromKB(workspace.judgeRequest.MaxMemory)
	if soft {
		peak := uint64(0)
		if watcher != nil {
			peak = watcher.Peak()
			if watcher.Exceeded() {
				result.Score = 0
				return "MEMORY_LIMIT_EXCEEDED"
			}
		}
		if limitBytes > 0 && peak >= limitBytes {
			result.Score = 0
			return "MEMORY_LIMIT_EXCEEDED"
		}
	} else if utils.CheckOOMEvent(cgroupPath) {
		result.Score = 0
		return "MEMORY_LIMIT_EXCEEDED"
	}

	if cmdErr != nil {
		result.Score = 0
		result.Message = cmdErr.Error()
		return "RUNTIME_ERROR"
	}

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

	for result := range resultChan {
		results = append(results, result)
	}

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

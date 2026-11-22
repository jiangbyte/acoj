package core

import (
	"fmt"
	"os/exec"
	model2 "service-judge/internal/database/model"
	"service-judge/internal/utils"
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
	result := e.initResult(workspace, testCase)

	// 建带内存上限的 cgroup。失败就是系统错误，不再退回 /proc 轮询
	group, err := utils.CreateCgroup(workspace.judgeRequest.MaxMemory)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("创建cgroup失败: %v", err), 1, err)
	}
	defer group.Close()

	return e.executeCommand(workspace, testCase, result, group)
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
	result *model2.DataJudgeCase, group *utils.Group) (*model2.DataJudgeCase, error) {

	cmd, stdoutBuf, stderrBuf, err := e.prepareCommand(workspace, testCase, group.FD())
	if err != nil {
		return e.handleError(result, fmt.Sprintf("准备命令失败: %v", err), 1, err)
	}

	pgid, procStart, err := e.startAndManageProcess(cmd, group)
	if err != nil {
		return e.handleError(result, fmt.Sprintf("进程管理失败: %v", err), 1, err)
	}

	return e.waitForCompletion(cmd, workspace, testCase, result, group, pgid, procStart, stdoutBuf, stderrBuf)
}

const defaultOutputLimitBytes = 2 << 20 // 2 MiB stdout/stderr 上限

// prepareCommand 准备命令执行环境：隔离网络/目录、chdir、限制输出字节。
// 时限从进程真正跑起来才开始计，不在这里用 context 提前扣掉准备时间。
func (e *SandboxExecutor) prepareCommand(workspace *Workspace, testCase *model2.DataTestCase, cgroupFD int) (
	*exec.Cmd, *utils.LimitedWriter, *utils.LimitedWriter, error) {

	runCmd := utils.GetRunCommand(workspace.langConfig, workspace.SourceFile, workspace.BuildFile)
	plan, err := utils.BuildIsolatePlan(runCmd, workspace.RootPath, workspace.RunsPath, workspace.config.Sandbox, cgroupFD)
	if err != nil {
		return nil, nil, nil, err
	}
	cmd := exec.Command(plan.Argv[0], plan.Argv[1:]...)
	cmd.SysProcAttr = plan.Attr
	if plan.Dir != "" {
		cmd.Dir = plan.Dir
	}
	logx.Infof("sandbox mode=%s dir=%s cgroup=v%d fd=%v", plan.Mode, workspace.RunsPath, utils.CgroupVersion(), cgroupFD > 0)

	cmd.Stdin = strings.NewReader(testCase.InputData)
	stdoutBuf := utils.NewLimitedWriter(defaultOutputLimitBytes)
	stderrBuf := utils.NewLimitedWriter(defaultOutputLimitBytes)
	cmd.Stdout = stdoutBuf
	cmd.Stderr = stderrBuf

	return cmd, stdoutBuf, stderrBuf, nil
}

// startAndManageProcess 启动进程。v2 用 cgroup fd 在创建时进组；v1 暂停后写入 procs 再恢复。
func (e *SandboxExecutor) startAndManageProcess(cmd *exec.Cmd, group *utils.Group) (int, time.Time, error) {
	if err := cmd.Start(); err != nil {
		return 0, time.Time{}, fmt.Errorf("启动进程失败: %w", err)
	}

	pgid := cmd.Process.Pid
	procStart := time.Now()
	if group.FD() > 0 {
		return pgid, procStart, nil
	}

	// v1 没有 CLONE_INTO_CGROUP，先停住再迁入，避免用户代码先跑出一段没被限制的内存
	if err := syscall.Kill(pgid, syscall.SIGSTOP); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		_ = cmd.Wait()
		return 0, time.Time{}, fmt.Errorf("暂停进程失败: %w", err)
	}
	if err := group.AddProc(pgid); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		_ = cmd.Wait()
		return 0, time.Time{}, fmt.Errorf("设置cgroup失败: %w", err)
	}
	if err := syscall.Kill(pgid, syscall.SIGCONT); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		_ = cmd.Wait()
		return 0, time.Time{}, fmt.Errorf("恢复进程失败: %w", err)
	}
	return pgid, time.Now(), nil
}

// waitForCompletion 等待命令完成并收集结果。时限从 procStart 起算，到点只杀进程组。
func (e *SandboxExecutor) waitForCompletion(cmd *exec.Cmd, workspace *Workspace,
	testCase *model2.DataTestCase, result *model2.DataJudgeCase, group *utils.Group, pgid int, procStart time.Time,
	stdoutBuf, stderrBuf *utils.LimitedWriter) (*model2.DataJudgeCase, error) {

	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	limit := time.Duration(workspace.judgeRequest.MaxTime * float64(time.Millisecond))
	stopCPU := group.WatchCPU(limit, pgid)
	defer stopCPU()

	var timeout <-chan time.Time
	if limit > 0 {
		timer := time.NewTimer(limit)
		defer timer.Stop()
		timeout = timer.C
	}

	var cmdErr error
	timedOut := false
	select {
	case <-timeout:
		timedOut = true
		syscall.Kill(-pgid, syscall.SIGKILL)
		cmdErr = <-done
	case cmdErr = <-done:
	}

	cpu, memBytes := e.collectExecutionMetrics(result, group, procStart, timedOut)
	result.OutputData = stdoutBuf.String()
	if timedOut {
		result.Status = "TIME_LIMIT_EXCEEDED"
		result.Score = 0
		if stderr := strings.TrimSpace(stderrBuf.String()); stderr != "" {
			result.Message = stderr
		}
		logx.Errorf("TLE - 运行时间: %.2f ms，时间限制: %.2f ms，内存: %.2f KB",
			result.MaxTime, workspace.judgeRequest.MaxTime, result.MaxMemory)
		return result, nil
	}

	result.Status = e.determineExecutionStatus(workspace, result, group, cpu, memBytes, cmdErr, stderrBuf, stdoutBuf, testCase)
	logx.Infof("运行完成 - 状态: %s, 时间: %.2f ms, 内存: %.2f KB, stdout截断=%v stderr截断=%v",
		result.Status, result.MaxTime, result.MaxMemory, stdoutBuf.Truncated(), stderrBuf.Truncated())
	return result, nil
}

// collectExecutionMetrics 用 cgroup 的 CPU 时间和内存峰值填结果。
// 墙钟只在 CPU 计数还没到、但进程被墙钟杀掉时用来解释 TLE。
func (e *SandboxExecutor) collectExecutionMetrics(result *model2.DataJudgeCase, group *utils.Group, procStart time.Time, wallKilled bool) (time.Duration, uint64) {
	wall := time.Since(procStart)
	cpu, err := group.CPUTime()
	if err != nil {
		logx.Errorf("读取 cgroup CPU 时间失败: %v", err)
	}
	memBytes, memErr := group.MemoryPeak()
	if memErr != nil {
		logx.Errorf("读取 cgroup 内存峰值失败: %v", memErr)
	}
	reported := cpu
	if wallKilled && wall > cpu {
		reported = wall
	}
	result.MaxTime = utils.DurationMS(reported)
	result.MaxMemory = utils.FormatBytesKB(memBytes)
	logx.Infof("cgroup v%d cpu=%.2f ms wall=%.2f ms memory=%.2f KB",
		group.Version, utils.DurationMS(cpu), utils.DurationMS(wall), result.MaxMemory)
	return cpu, memBytes
}

// determineExecutionStatus 判断执行状态。时间和内存都以 cgroup 计数为准。
func (e *SandboxExecutor) determineExecutionStatus(workspace *Workspace, result *model2.DataJudgeCase,
	group *utils.Group, cpu time.Duration, memBytes uint64, cmdErr error,
	stderrBuf, stdoutBuf *utils.LimitedWriter, testCase *model2.DataTestCase) string {

	limit := time.Duration(workspace.judgeRequest.MaxTime * float64(time.Millisecond))
	limitBytes := utils.MemoryLimitBytesFromKB(workspace.judgeRequest.MaxMemory)

	// 1. 内核报了 OOM，或峰值超过上限 → MLE
	if group.OOMKilled() || (limitBytes > 0 && memBytes > limitBytes) {
		result.Score = 0
		return "MEMORY_LIMIT_EXCEEDED"
	}
	// 2. cgroup CPU 时间超过题目时限 → TLE
	if limit > 0 && cpu > limit {
		result.Score = 0
		return "TIME_LIMIT_EXCEEDED"
	}

	// 3. 输出超限截断证据
	if stdoutBuf.Truncated() || stderrBuf.Truncated() {
		result.Score = 0
		result.Message = fmt.Sprintf("output truncated: stdoutWritten=%d stderrWritten=%d limit=%d",
			stdoutBuf.Written(), stderrBuf.Written(), defaultOutputLimitBytes)
		logx.Infof("OUTPUT_LIMIT_EXCEEDED: %s", result.Message)
		return "OUTPUT_LIMIT_EXCEEDED"
	}

	if cmdErr != nil {
		result.Score = 0
		result.Message = cmdErr.Error()
		return "RUNTIME_ERROR"
	}

	stderr := strings.TrimSpace(stderrBuf.String())
	if stderr != "" {
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

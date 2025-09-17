package judge

import (
	"bytes"
	"context"
	"fmt"
	"judge-service/internal/dto"
	"judge-service/internal/grutil"
	"os/exec"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

type Executor struct {
	ctx     context.Context
	logger  logx.Logger
	Sandbox Sandbox
	mu      sync.Mutex // 添加互斥锁防止竞争条件
}

// 创建执行器，传入上下文，沙箱
func NewExecutor(ctx context.Context, sandbox Sandbox) *Executor {
	return &Executor{
		ctx:     ctx,
		logger:  logx.WithContext(ctx),
		Sandbox: sandbox,
	}
}

// 实际执行
func (e *Executor) Execute() (*dto.JudgeResultDto, error) {
	result := dto.ConvertSubmitToResult(e.Sandbox.Workspace.judgeSubmit)
	runCmd := grutil.GetRunCommand(e.Sandbox.Workspace.langConfig, e.Sandbox.Workspace.BuildFile)

	for i := range result.TestCase {
		testCase := &result.TestCase[i]

		cgroupPath, err := grutil.CreateCgroup(e.Sandbox.Workspace.judgeSubmit.MaxMemory)
		if err != nil {
			logx.Errorf("创建cgroup失败: %v", err)
			result.Status = dto.StatusSystemError
			result.Message = fmt.Sprintf("创建cgroup失败: %v", err)
			return &result, err
		}

		err = e.executeTestCase(testCase, i, runCmd, cgroupPath)
		if err != nil {
			result.Status = dto.StatusSystemError
			result.Message = fmt.Sprintf("执行测试用例失败: %v", err)
			return &result, err
		}


		grutil.CleanupCgroup(cgroupPath)
	}

	return &result, nil
}

func (e *Executor) executeTestCase(testCase *dto.SubmitTestCase, index int, runCmd []string, cgroupPath string) error {
	timeout := time.Duration(e.Sandbox.Workspace.judgeSubmit.MaxTime) * time.Millisecond
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

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

	cmd.Stdin = strings.NewReader(testCase.Input)
	var stdoutBuf, stderrBuf bytes.Buffer
	cmd.Stdout = &stdoutBuf
	cmd.Stderr = &stderrBuf

	err := cmd.Start()
	if err != nil {
		testCase.Status = dto.StatusRuntimeError
		testCase.Message = fmt.Sprintf("启动进程失败: %v", err)
		logx.Errorf("启动进程失败: %v", testCase.Message)
		return err
	}
	e.mu.Lock()

	// 立即暂停进程
	syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP)

	pgid := cmd.Process.Pid
	if err := grutil.SetCgroupForProcess(cgroupPath, pgid); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		testCase.Status = dto.StatusRuntimeError
		testCase.Message = fmt.Sprintf("设置cgroup失败: %v", err)
		logx.Errorf("设置cgroup失败: %v", testCase.Message)
		return err
	}

	// 进程即将恢复执行
	startTime := time.Now()
	syscall.Kill(cmd.Process.Pid, syscall.SIGCONT)
	e.mu.Unlock()

	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	select {
	case <-ctx.Done():
		syscall.Kill(-pgid, syscall.SIGKILL)
		elapsed := time.Since(startTime)
		memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
		testCase.MaxMemory = int(grutil.FormatBytesKB(memoryUsed))
		testCase.MaxTime = int(elapsed.Microseconds())
		testCase.Status = dto.StatusTimeLimitExceeded
		testCase.Output = stdoutBuf.String()
		stderr := stderrBuf.String()
		if strings.TrimSpace(stderr) != "" {
			testCase.Message = stderr
			testCase.Status = dto.StatusRuntimeError
		}

		logx.Errorf("超时杀死整个进程组，运行时间: %v ms 时间限制 %v ms", elapsed, e.Sandbox.Workspace.judgeSubmit.MaxTime)
	case err := <-done:
		elapsed := time.Since(startTime)
		memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
		testCase.MaxMemory = int(grutil.FormatBytesKB(memoryUsed))
		testCase.MaxTime = int(elapsed.Microseconds())
		if grutil.CheckOOMEvent(cgroupPath) {
			testCase.Status = dto.StatusMemoryLimitExceeded
		}
		testCase.Output = stdoutBuf.String()
		stderr := stderrBuf.String()
		if strings.TrimSpace(stderr) != "" {
			testCase.Message = stderr
			testCase.Status = dto.StatusRuntimeError
		}

		logx.Infof("测试用例 %d 完成, 内存使用: %d bytes (峰值), 程序退出状态: %v", index, memoryUsed, err)
		logx.Infof("测试用例 %d 内存使用: %v KB", index, grutil.FormatBytesKB(memoryUsed))
	}

	return nil
}

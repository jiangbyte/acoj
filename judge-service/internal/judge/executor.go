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

// 执行器执行
func (e *Executor) Execute() (*dto.JudgeResultDto, error) {
	result := dto.ConvertSubmitToResult(e.Sandbox.Workspace.judgeSubmit)
	runCmd := grutil.GetRunCommand(e.Sandbox.Workspace.langConfig, e.Sandbox.Workspace.BuildFile)
	for i := range result.TestCase {
		testCase := &result.TestCase[i]
		cgroupPath, err := grutil.CreateCgroup(e.Sandbox.Workspace.judgeSubmit.MaxMemory)
		if err != nil {
			logx.Errorf("创建 CGroup 失败: %v", err)
			result.Status = dto.StatusSystemError
			result.Message = fmt.Sprintf("创建 CGroup 失败: %v", err)
			return &result, err
		}
		err = e.executeTestCase(testCase, i, runCmd, cgroupPath)
		if err != nil {
			result.Status = dto.StatusSystemError
			result.Message = fmt.Sprintf("执行用例 %d 失败: %v", i, err)
			return &result, err
		}
		grutil.CleanupCgroup(cgroupPath)
	}
	return &result, nil
}

func (e *Executor) executeTestCase(testCase *dto.SubmitTestCase, index int, runCmd []string, cgroupPath string) error {
	timeout := time.Duration(e.Sandbox.Workspace.judgeSubmit.MaxTime) * time.Millisecond
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel() // 设置超时时间，根据提交的最大时间限制

	cmd := exec.CommandContext(ctx, runCmd[0], runCmd[1:]...)
	// 设置进程属性，创建新的命名空间进行隔离
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Setpgid: true, // 设置进程组ID，便于管理进程组
		Cloneflags: syscall.CLONE_NEWNS | // 新的挂载命名空间
			syscall.CLONE_NEWUTS | // 新的UTS命名空间（主机名和域名）
			syscall.CLONE_NEWPID | // 新的PID命名空间
			syscall.CLONE_NEWNET | // 新的网络命名空间
			syscall.CLONE_NEWIPC, // 新的IPC命名空间
		Unshareflags: syscall.CLONE_NEWNS, // 取消共享挂载命名空间
	}

	// 设置标准输入为测试用例的输入
	cmd.Stdin = strings.NewReader(testCase.Input)
	var stdoutBuf, stderrBuf bytes.Buffer
	cmd.Stdout = &stdoutBuf
	cmd.Stderr = &stderrBuf

	// 启动命令
	err := cmd.Start()
	if err != nil {
		testCase.Status = dto.StatusRuntimeError
		testCase.Message = fmt.Sprintf("启动进程失败: %v", err)
		logx.Errorf("启动进程失败: %v", testCase.Message)
		return err
	}
	// 加锁确保对进程操作的原子性
	e.mu.Lock()

	// 立即暂停进程，以便设置cgroup
	syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP)

	// 获取进程组ID
	pgid := cmd.Process.Pid

	// 将进程添加到cgroup进行资源限制
	if err := grutil.SetCgroupForProcess(cgroupPath, pgid); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL) // 将进程添加到cgroup进行资源限制
		testCase.Status = dto.StatusRuntimeError
		testCase.Message = fmt.Sprintf("设置cgroup失败: %v", err)
		logx.Errorf("设置cgroup失败: %v", testCase.Message)
		return err
	}

	startTime := time.Now() // 将进程添加到cgroup进行资源限制
	syscall.Kill(cmd.Process.Pid, syscall.SIGCONT)
	e.mu.Unlock() // 解锁，允许其他操作

	done := make(chan error, 1) // 创建通道等待命令执行完成
	go func() {
		done <- cmd.Wait()
	}()

	select {
	case <-ctx.Done():
		// 获取内存使用情况
		syscall.Kill(-pgid, syscall.SIGKILL)
		elapsed := time.Since(startTime)
		// 获取内存使用情况
		memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
		testCase.MaxMemory = int(grutil.FormatBytesKB(memoryUsed))
		testCase.MaxTime = int(elapsed.Microseconds())
		testCase.Status = dto.StatusTimeLimitExceeded
		testCase.Output = stdoutBuf.String()
		// 检查是否有错误输出
		stderr := stderrBuf.String()
		if strings.TrimSpace(stderr) != "" {
			testCase.Message = stderr
			testCase.Status = dto.StatusRuntimeError
		}
		// // 超时情况下退出码通常不可用，设置为特定值
		// testCase.ExitCode = -1 // 或者使用其他特殊值表示超时

		logx.Errorf("超时杀死整个进程组，运行时间: %v ms 时间限制 %v ms", elapsed, e.Sandbox.Workspace.judgeSubmit.MaxTime)
	case err := <-done:
		// 命令正常完成或出错
		elapsed := time.Since(startTime)
		memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
		testCase.MaxMemory = int(grutil.FormatBytesKB(memoryUsed))
		testCase.MaxTime = int(elapsed.Microseconds())

		// // 获取退出码
		// exitCode := 0
		// if err != nil {
		// 	if exitError, ok := err.(*exec.ExitError); ok {
		// 		// 获取进程退出状态
		// 		if status, ok := exitError.Sys().(syscall.WaitStatus); ok {
		// 			exitCode = status.ExitStatus()
		// 		} else {
		// 			// 如果无法获取详细的退出状态，使用通用错误码
		// 			exitCode = -1
		// 		}
		// 	} else {
		// 		// 非退出错误，设置特殊错误码
		// 		exitCode = -2
		// 	}
		// }
		// testCase.ExitCode = exitCode

		// 检查是否发生内存溢出
		if grutil.CheckOOMEvent(cgroupPath) {
			testCase.Status = dto.StatusMemoryLimitExceeded
		}
		testCase.Output = stdoutBuf.String()
		// 检查是否有错误输出
		stderr := stderrBuf.String()
		if strings.TrimSpace(stderr) != "" {
			testCase.Message = stderr
			testCase.Status = dto.StatusRuntimeError
		}

		// // 根据退出码设置状态（如果需要）
		// if exitCode != 0 {
		// 	testCase.Status = dto.StatusRuntimeError
		// 	testCase.Message = fmt.Sprintf("程序异常退出，退出码: %d", exitCode)
		// }

		logx.Infof("测试用例 %d 完成, 内存使用: %d bytes (峰值), 程序退出状态: %v", index, memoryUsed, err)
		logx.Infof("测试用例 %d 内存使用: %v KB", index, grutil.FormatBytesKB(memoryUsed))
	}

	return nil
}

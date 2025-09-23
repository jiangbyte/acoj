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

type Compiler struct {
	ctx     context.Context
	logger  logx.Logger
	Sandbox Sandbox
	mu      sync.Mutex // 添加互斥锁防止竞争条件
}

// 创建编译器，传入上下文，沙箱
func NewCompiler(ctx context.Context, sandbox Sandbox) *Compiler {
	return &Compiler{
		ctx:     ctx,
		logger:  logx.WithContext(ctx),
		Sandbox: sandbox,
	}
}

// 实际执行编译
func (e *Compiler) Execute() (*dto.JudgeResultDto, error) {
	result := dto.ConvertSubmitToResult(e.Sandbox.Workspace.judgeSubmit)
	comCmd := grutil.GetCompileCommand(e.Sandbox.Workspace.langConfig, e.Sandbox.Workspace.SourceFile, e.Sandbox.Workspace.BuildFile)

	cgroupPath, err := grutil.CreateCgroupNoMemory()
	if err != nil {
		logx.Errorf("创建cgroup失败: %v", err)
		result.Status = dto.StatusSystemError
		result.Message = fmt.Sprintf("创建cgroup失败: %v", err)
		return &result, err
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	cmd := exec.CommandContext(ctx, comCmd[0], comCmd[1:]...)
	cmd.Dir = e.Sandbox.Workspace.BuildPath

	cmd.SysProcAttr = &syscall.SysProcAttr{
		Setpgid: true,
		Cloneflags: syscall.CLONE_NEWNS |
			syscall.CLONE_NEWUTS |
			syscall.CLONE_NEWPID |
			syscall.CLONE_NEWNET |
			syscall.CLONE_NEWIPC,
		Unshareflags: syscall.CLONE_NEWNS,
	}

	var stdoutBuf, stderrBuf bytes.Buffer
	cmd.Stdout = &stdoutBuf
	cmd.Stderr = &stderrBuf

	err = cmd.Start()
	if err != nil {
		result.Status = dto.StatusRuntimeError
		result.Message = fmt.Sprintf("启动进程失败: %v", err)
		logx.Errorf("启动进程失败: %v", result.Message)
		return &result, err
	}
	e.mu.Lock()

	// 立即暂停进程
	syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP)

	pgid := cmd.Process.Pid
	if err := grutil.SetCgroupForProcess(cgroupPath, pgid); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		result.Status = dto.StatusRuntimeError
		result.Message = fmt.Sprintf("设置cgroup失败: %v", err)
		logx.Errorf("设置cgroup失败: %v", result.Message)
		grutil.CleanupCgroup(cgroupPath)
		return &result, err
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
		result.MaxMemory = int(grutil.FormatBytesKB(memoryUsed))
		result.MaxTime = int(elapsed.Microseconds())
		result.Status = dto.StatusCompilationError
		stderr := stderrBuf.String()
		if strings.TrimSpace(stderr) != "" {
			result.Message = stderr
			result.Status = dto.StatusCompilationError
		}
		// result.ExitCode = -1 // 或者使用其他特殊值表示超时

		logx.Errorf("超时杀死整个进程组，运行时间: %v ms 时间限制 %v ms", elapsed, e.Sandbox.Workspace.judgeSubmit.MaxTime)
		return &result, err
	case err := <-done:
		elapsed := time.Since(startTime)
		memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
		result.MaxMemory = int(grutil.FormatBytesKB(memoryUsed))
		result.MaxTime = int(elapsed.Microseconds())

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
		// result.ExitCode = exitCode

		stderr := stderrBuf.String()
		if strings.TrimSpace(stderr) != "" {
			result.Status = dto.StatusCompilationError
			result.Message = stderr
			grutil.CleanupCgroup(cgroupPath)
			// // 根据退出码设置状态（如果需要）
			// if exitCode != 0 {
			// 	result.Status = dto.StatusRuntimeError
			// 	result.Message = fmt.Sprintf("程序异常退出，退出码: %d", exitCode)
			// }
			return &result, err
		}

		logx.Infof("编译完成, 内存使用: %d bytes (峰值), 程序退出状态: %v", memoryUsed, err)
		result.Status = dto.StatusCompiledOK
		grutil.CleanupCgroup(cgroupPath)
		// 根据退出码设置状态（如果需要）
		// if exitCode != 0 {
		// 	result.Status = dto.StatusRuntimeError
		// 	result.Message = fmt.Sprintf("程序异常退出，退出码: %d", exitCode)
		// }
		return &result, err
	}
}

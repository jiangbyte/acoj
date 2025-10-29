// package judge

// import (
// 	"bytes"
// 	"context"
// 	"fmt"
// 	"judge-service/internal/grutil"
// 	"os/exec"
// 	"strings"
// 	"sync"
// 	"syscall"
// 	"time"

// 	"github.com/zeromicro/go-zero/core/logx"
// )

// type CompileResult struct {
// 	Success   bool
// 	Message   string
// 	ExitCode  int
// 	MaxTime   float64
// 	MaxMemory float64
// }

// type Compiler interface {
// 	Compile(workspace *Workspace) (*CompileResult, error)
// }

// type DefaultCompiler struct {
// 	mu sync.Mutex // 添加互斥锁防止竞争条件
// }

// func (c *DefaultCompiler) Compile(workspace *Workspace) (*CompileResult, error) {
// 	startTime := time.Now()
// 	result := &CompileResult{
// 		MaxTime: float64(time.Since(startTime).Milliseconds()),
// 	}
// 	compilerCmd := grutil.GetCompileCommand(workspace.langConfig, workspace.SourceFile, workspace.BuildFile)
// 	cgroupPath, err := grutil.CreateCgroupNoMemory()
// 	if err != nil {
// 		result.Success = false
// 		result.Message = fmt.Sprintf("创建cgroup失败: %v", err)
// 		result.ExitCode = 1
// 		return result, err
// 	}

// 	// 编译超时保护，默认编译时间限制 + 5 秒
// 	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
// 	defer cancel()

// 	cmd := exec.CommandContext(ctx, compilerCmd[0], compilerCmd[1:]...)
// 	cmd.Dir = workspace.BuildPath

// 	cmd.SysProcAttr = &syscall.SysProcAttr{
// 		Setpgid: true,
// 		Cloneflags: syscall.CLONE_NEWNS |
// 			syscall.CLONE_NEWUTS |
// 			syscall.CLONE_NEWPID |
// 			syscall.CLONE_NEWNET |
// 			syscall.CLONE_NEWIPC,
// 		Unshareflags: syscall.CLONE_NEWNS,
// 	}

// 	var stdoutBuf, stderrBuf bytes.Buffer
// 	cmd.Stdout = &stdoutBuf
// 	cmd.Stderr = &stderrBuf

// 	err = cmd.Start()
// 	if err != nil {
// 		result.Success = false
// 		result.Message = fmt.Sprintf("启动进程失败: %v", err)
// 		logx.Errorf("启动进程失败: %v", result.Message)
// 		return result, err
// 	}
// 	c.mu.Lock()

// 	// 立即暂停进程
// 	syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP)

// 	pgid := cmd.Process.Pid
// 	if err := grutil.SetCgroupForProcess(cgroupPath, pgid); err != nil {
// 		syscall.Kill(-pgid, syscall.SIGKILL)
// 		result.Message = fmt.Sprintf("设置cgroup失败: %v", err)
// 		logx.Errorf("设置cgroup失败: %v", result.Message)
// 		grutil.CleanupCgroup(cgroupPath)
// 		return result, err
// 	}

// 	// 进程即将恢复执行
// 	syscall.Kill(cmd.Process.Pid, syscall.SIGCONT)
// 	c.mu.Unlock()

// 	done := make(chan error, 1)
// 	go func() {
// 		done <- cmd.Wait()
// 	}()

// 	select {
// 	case <-ctx.Done():
// 		// 超时情况
// 		syscall.Kill(-pgid, syscall.SIGKILL)
// 		elapsed := time.Since(startTime)
// 		memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
// 		result.MaxMemory = grutil.FormatBytesKB(memoryUsed)
// 		result.MaxTime = float64(elapsed.Milliseconds())
// 		stderr := stderrBuf.String()
// 		if strings.TrimSpace(stderr) != "" {
// 			result.Message = stderr
// 		}
// 		result.Success = false

// 		logx.Errorf("超时杀死整个进程组，运行时间: %v ms 时间限制 %v ms", elapsed, workspace.judgeRequest.MaxTime)
// 		return result, err
// 	case err := <-done:
// 		// 其他情况
// 		elapsed := time.Since(startTime)
// 		memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
// 		result.MaxMemory = grutil.FormatBytesKB(memoryUsed)
// 		result.MaxTime = float64(elapsed.Milliseconds())

// 		stderr := stderrBuf.String()
// 		if strings.TrimSpace(stderr) != "" {
// 			result.Success = false
// 			result.Message = stderr
// 			grutil.CleanupCgroup(cgroupPath)
// 			return result, err
// 		}

// 		logx.Infof("编译完成, 内存使用: %d bytes (峰值), %f KB, 用时 %f ms", memoryUsed, result.MaxMemory, result.MaxTime)
// 		result.Success = true
// 		grutil.CleanupCgroup(cgroupPath)
// 		return result, err
// 	}
// }

// func (w *Workspace) compile() (*CompileResult, error) {
// 	compiler := &DefaultCompiler{}
// 	return compiler.Compile(w)
// }

package judge

import (
	"bytes"
	"context"
	"fmt"
	"judge-service/internal/grutil"
	"os/exec"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

type CompileResult struct {
	Success   bool
	Message   string
	ExitCode  int
	MaxTime   float64
	MaxMemory float64
}

type Compiler interface {
	Compile(workspace *Workspace) (*CompileResult, error)
}

type DefaultCompiler struct {
	mu sync.Mutex
}

// 编译超时时间常量
const compileTimeout = 5 * time.Second

func (c *DefaultCompiler) Compile(workspace *Workspace) (*CompileResult, error) {
	startTime := time.Now()
	result := &CompileResult{
		MaxTime: float64(time.Since(startTime).Milliseconds()),
	}

	cgroupPath, err := grutil.CreateCgroupNoMemory()
	if err != nil {
		return c.handleError(result, fmt.Sprintf("创建cgroup失败: %v", err), 1, err)
	}
	defer grutil.CleanupCgroup(cgroupPath) // 使用defer确保资源清理

	ctx, cancel := context.WithTimeout(context.Background(), compileTimeout)
	defer cancel()

	compilerCmd := grutil.GetCompileCommand(workspace.langConfig, workspace.SourceFile, workspace.BuildFile)
	cmd := c.createCommand(ctx, compilerCmd, workspace.BuildPath)

	var stdoutBuf, stderrBuf bytes.Buffer
	cmd.Stdout = &stdoutBuf
	cmd.Stderr = &stderrBuf

	if err := cmd.Start(); err != nil {
		return c.handleError(result, fmt.Sprintf("启动进程失败: %v", err), 1, err)
	}

	// 进程控制逻辑
	pgid, err := c.controlProcess(cmd, cgroupPath, result)
	if err != nil {
		return result, err
	}

	return c.waitForCompletion(cmd, ctx, pgid, cgroupPath, &stdoutBuf, &stderrBuf, startTime, result)
}

// 创建命令实例
func (c *DefaultCompiler) createCommand(ctx context.Context, compilerCmd []string, dir string) *exec.Cmd {
	cmd := exec.CommandContext(ctx, compilerCmd[0], compilerCmd[1:]...)
	cmd.Dir = dir

	cmd.SysProcAttr = &syscall.SysProcAttr{
		Setpgid: true,
		Cloneflags: syscall.CLONE_NEWNS |
			syscall.CLONE_NEWUTS |
			syscall.CLONE_NEWPID |
			syscall.CLONE_NEWNET |
			syscall.CLONE_NEWIPC,
		Unshareflags: syscall.CLONE_NEWNS,
	}

	return cmd
}

// 进程控制：暂停、设置cgroup、恢复
func (c *DefaultCompiler) controlProcess(cmd *exec.Cmd, cgroupPath string, result *CompileResult) (int, error) {
	c.mu.Lock()
	defer c.mu.Unlock()

	// 立即暂停进程
	if err := syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP); err != nil {
		return 0, fmt.Errorf("暂停进程失败: %v", err)
	}

	pgid := cmd.Process.Pid
	if err := grutil.SetCgroupForProcess(cgroupPath, pgid); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		return 0, fmt.Errorf("设置cgroup失败: %v", err)
	}

	// 恢复进程执行
	if err := syscall.Kill(cmd.Process.Pid, syscall.SIGCONT); err != nil {
		return 0, fmt.Errorf("恢复进程失败: %v", err)
	}

	return pgid, nil
}

// 等待命令完成并处理结果
func (c *DefaultCompiler) waitForCompletion(cmd *exec.Cmd, ctx context.Context, pgid int,
	cgroupPath string, stdoutBuf, stderrBuf *bytes.Buffer, startTime time.Time, result *CompileResult) (*CompileResult, error) {

	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	select {
	case <-ctx.Done():
		return c.handleTimeout(pgid, cgroupPath, stderrBuf, startTime, result)
	case err := <-done:
		return c.handleCompletion(err, cgroupPath, stdoutBuf, stderrBuf, startTime, result)
	}
}

// 处理超时情况
func (c *DefaultCompiler) handleTimeout(pgid int, cgroupPath string, stderrBuf *bytes.Buffer,
	startTime time.Time, result *CompileResult) (*CompileResult, error) {

	syscall.Kill(-pgid, syscall.SIGKILL)
	elapsed := time.Since(startTime)

	memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
	result.MaxMemory = grutil.FormatBytesKB(memoryUsed)
	result.MaxTime = float64(elapsed.Milliseconds())

	stderr := strings.TrimSpace(stderrBuf.String())
	if stderr != "" {
		result.Message = stderr
	}

	result.Success = false
	logx.Errorf("超时杀死整个进程组，运行时间: %v ms", elapsed)

	return result, context.DeadlineExceeded
}

// 处理正常完成情况
func (c *DefaultCompiler) handleCompletion(err error, cgroupPath string, stdoutBuf, stderrBuf *bytes.Buffer,
	startTime time.Time, result *CompileResult) (*CompileResult, error) {

	elapsed := time.Since(startTime)
	memoryUsed, _ := grutil.GetMemoryUsage(cgroupPath)
	result.MaxMemory = grutil.FormatBytesKB(memoryUsed)
	result.MaxTime = float64(elapsed.Milliseconds())

	stderr := strings.TrimSpace(stderrBuf.String())
	if stderr != "" || err != nil {
		result.Success = false
		result.Message = stderr
		if err != nil {
			result.Message = fmt.Sprintf("%v\n\n%s", err, result.Message)
		}
		return result, err
	}

	logx.Infof("编译完成, 内存使用: %d bytes (峰值), %f KB, 用时 %f ms",
		memoryUsed, result.MaxMemory, result.MaxTime)
	result.Success = true

	return result, nil
}

// 统一错误处理
func (c *DefaultCompiler) handleError(result *CompileResult, message string, exitCode int, err error) (*CompileResult, error) {
	result.Success = false
	result.Message = message
	result.ExitCode = exitCode
	logx.Errorf(message)
	return result, err
}

func (w *Workspace) compile() (*CompileResult, error) {
	compiler := &DefaultCompiler{}
	return compiler.Compile(w)
}

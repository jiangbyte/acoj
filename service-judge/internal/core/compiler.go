package core

import (
	"bytes"
	"context"
	"fmt"
	"os/exec"
	"service-judge/internal/utils"
	"strings"
	"sync"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

// CompilerManager 编译器管理器，管理编译器实例
type CompilerManager struct {
	compilers sync.Pool
}

var compilerManager = &CompilerManager{
	compilers: sync.Pool{
		New: func() interface{} {
			return &DefaultCompiler{}
		},
	},
}

func GetCompiler() *DefaultCompiler {
	return compilerManager.compilers.Get().(*DefaultCompiler)
}

func ReleaseCompiler(compiler *DefaultCompiler) {
	compilerManager.compilers.Put(compiler)
}

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
	// mu sync.Mutex
}

// 编译超时时间常量
const compileTimeout = 5 * time.Second

func (c *DefaultCompiler) Compile(workspace *Workspace) (*CompileResult, error) {
	result := &CompileResult{}

	// 编译不设内存上限，但必须进 cgroup，时间和内存都从内核计数读取
	group, err := utils.CreateCgroupNoMemory()
	if err != nil {
		return c.handleError(result, fmt.Sprintf("创建cgroup失败: %v", err), 1, err)
	}
	defer group.Close()

	compilerCmd := utils.GetCompileCommand(workspace.langConfig, workspace.SourceFile, workspace.BuildFile)
	cmd := c.createCommand(compilerCmd, workspace.BuildPath, group.FD())

	var stdoutBuf, stderrBuf bytes.Buffer
	cmd.Stdout = &stdoutBuf
	cmd.Stderr = &stderrBuf

	if err := cmd.Start(); err != nil {
		return c.handleError(result, fmt.Sprintf("启动进程失败: %v", err), 1, err)
	}

	pgid := cmd.Process.Pid
	procStart := time.Now()
	if group.FD() <= 0 {
		pgid, err = c.controlProcess(cmd, group)
		if err != nil {
			return result, err
		}
		procStart = time.Now()
	}

	return c.waitForCompletion(cmd, pgid, group, &stderrBuf, procStart, result)
}

// 创建命令实例
func (c *DefaultCompiler) createCommand(compilerCmd []string, dir string, cgroupFD int) *exec.Cmd {
	cmd := exec.Command(compilerCmd[0], compilerCmd[1:]...)
	cmd.Dir = dir
	cmd.SysProcAttr = buildSysProcAttr(cgroupFD)
	return cmd
}

// 进程控制：暂停、写入 cgroup、恢复。v1 没有 clone 进组的办法时走这里。
func (c *DefaultCompiler) controlProcess(cmd *exec.Cmd, group *utils.Group) (int, error) {
	if err := syscall.Kill(cmd.Process.Pid, syscall.SIGSTOP); err != nil {
		return 0, fmt.Errorf("暂停进程失败: %v", err)
	}

	pgid := cmd.Process.Pid
	if err := group.AddProc(pgid); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		_ = cmd.Wait()
		return 0, fmt.Errorf("设置cgroup失败: %v", err)
	}

	if err := syscall.Kill(cmd.Process.Pid, syscall.SIGCONT); err != nil {
		syscall.Kill(-pgid, syscall.SIGKILL)
		_ = cmd.Wait()
		return 0, fmt.Errorf("恢复进程失败: %v", err)
	}

	return pgid, nil
}

// 等待命令完成并处理结果。编译时限从进程开始跑起算。
func (c *DefaultCompiler) waitForCompletion(cmd *exec.Cmd, pgid int,
	group *utils.Group, stderrBuf *bytes.Buffer, procStart time.Time, result *CompileResult) (*CompileResult, error) {

	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	timer := time.NewTimer(compileTimeout)
	defer timer.Stop()

	select {
	case <-timer.C:
		syscall.Kill(-pgid, syscall.SIGKILL)
		<-done
		c.fillCompileMetrics(result, group, procStart)
		stderr := strings.TrimSpace(stderrBuf.String())
		if stderr != "" {
			result.Message = stderr
		}
		result.Success = false
		logx.Errorf("超时杀死整个进程组，运行时间: %.2f ms", result.MaxTime)
		return result, context.DeadlineExceeded
	case err := <-done:
		return c.handleCompletion(err, group, stderrBuf, procStart, result)
	}
}

// fillCompileMetrics 用进程墙钟和内核内存峰值填写编译消耗。
func (c *DefaultCompiler) fillCompileMetrics(result *CompileResult, group *utils.Group, procStart time.Time) {
	cpu, err := group.CPUTime()
	if err != nil {
		logx.Errorf("读取编译 CPU 时间失败: %v", err)
		cpu = time.Since(procStart)
	}
	mem, memErr := group.MemoryPeak()
	if memErr != nil {
		logx.Errorf("读取编译内存峰值失败: %v", memErr)
	}
	result.MaxTime = utils.DurationMS(cpu)
	result.MaxMemory = utils.FormatBytesKB(mem)
}

// 处理正常完成情况
func (c *DefaultCompiler) handleCompletion(err error, group *utils.Group, stderrBuf *bytes.Buffer,
	procStart time.Time, result *CompileResult) (*CompileResult, error) {

	c.fillCompileMetrics(result, group, procStart)

	stderr := strings.TrimSpace(stderrBuf.String())
	if stderr != "" || err != nil {
		result.Success = false
		result.Message = stderr
		if err != nil {
			result.Message = fmt.Sprintf("%v\n\n%s", err, result.Message)
		}
		return result, err
	}

	logx.Infof("编译完成, 内存峰值: %.2f KB, 用时 %.2f ms", result.MaxMemory, result.MaxTime)
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

package executor

import (
	"fmt"
	"os"
	"os/exec"
	"syscall"
	"time"

	"sandbox/internal/config"
	"sandbox/internal/result"
)

/* 编译目标程序 */
func Compile(argument config.Argument, resource config.Resource, additional config.Additional, result *result.Result) {
	startTime := time.Now()

	// 设置编译内存限制
	if resource.MaxCompileMem > 0 {
		if err := syscall.Setrlimit(syscall.RLIMIT_AS, &syscall.Rlimit{
			Cur: uint64(resource.MaxCompileMem * 1024),
			Max: uint64(resource.MaxCompileMem * 1024),
		}); err != nil {
			result.ExitCode = config.SystemError
			result.Message = fmt.Sprintf("设置编译内存限制失败: %v", err)
			return
		}
	}

	// 准备错误文件
	errFile, err := os.OpenFile(additional.ErrorFilePath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0644)
	if err != nil {
		result.ExitCode = config.FileError
		result.Message = "打开错误文件失败: " + err.Error()
		return
	}
	defer errFile.Close()

	// 创建命令对象
	cmd := exec.Command(argument.Argv[0], argument.Argv[1:]...)
	cmd.Stderr = errFile
	cmd.Env = argument.Envp

	// 设置进程组属性
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Setpgid: true,
	}

	// 启动编译过程
	err = cmd.Start()
	if err != nil {
		result.ExitCode = config.SystemError
		result.Message = "启动编译失败: " + err.Error()
		return
	}

	// 创建完成通道
	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	// 设置编译超时
	timeout := time.Duration(resource.MaxCompileTime) * time.Millisecond
	if timeout <= 0 {
		timeout = 30 * time.Second // 默认编译超时
	}

	select {
	case <-time.After(timeout):
		// 超时处理
		syscall.Kill(-cmd.Process.Pid, syscall.SIGKILL)
		result.ExitCode = config.TimeLimitExceeded
		result.Message = "COMPILE_TIME_LIMIT_EXCEEDED"
	case err := <-done:
		if err != nil {
			if exitErr, ok := err.(*exec.ExitError); ok {
				if status, ok := exitErr.Sys().(syscall.WaitStatus); ok {
					result.ExitCode = status.ExitStatus()
					if status.Signaled() {
						result.Signal = int(status.Signal())
						errFile.WriteString(fmt.Sprintf("编译进程被信号终止: %d\n", result.Signal))
					}
				}
			} else {
				result.ExitCode = config.CompileError
				result.Message = "编译错误: " + err.Error()
			}
		} else {
			result.ExitCode = config.Success
		}
	}

	// 计算编译时间
	result.RealTime = time.Since(startTime).Milliseconds()

	// 获取资源使用情况
	if cmd.ProcessState != nil {
		if sysUsage, ok := cmd.ProcessState.SysUsage().(*syscall.Rusage); ok {
			result.CpuTime = int64(sysUsage.Utime.Nano()/1e6) + int64(sysUsage.Stime.Nano()/1e6)
			result.Memory = sysUsage.Maxrss

			// 检查编译内存限制
			if resource.MaxCompileMem > 0 && result.Memory > resource.MaxCompileMem {
				result.ExitCode = config.MemoryLimitExceeded
				result.Message = "COMPILE_MEMORY_LIMIT_EXCEEDED"
			}
		}
	}
}

package executor

import (
	"fmt"
	"os"
	"os/exec"
	"syscall"
	"time"

	"sandbox/internal/config"
	"sandbox/internal/result"
	"sandbox/internal/utils"
)

/* 运行目标程序 */
func Run(argument config.Argument, resource config.Resource, additional config.Additional, result *result.Result) {
	startTime := time.Now()

	// 准备输入文件
	inFile, err := os.OpenFile(additional.InputFilePath, os.O_RDONLY, 0644)
	if err != nil {
		result.ExitCode = config.FileError
		result.Message = "打开输入文件失败: " + err.Error()
		return
	}
	defer inFile.Close()

	// 准备输出文件
	outFile, err := os.OpenFile(additional.OutputFilePath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0644)
	if err != nil {
		result.ExitCode = config.FileError
		result.Message = "打开输出文件失败: " + err.Error()
		return
	}
	defer outFile.Close()

	// 准备错误文件
	errFile, err := os.OpenFile(additional.ErrorFilePath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0644)
	if err != nil {
		result.ExitCode = config.FileError
		result.Message = "打开错误文件失败: " + err.Error()
		return
	}
	defer errFile.Close()

	// 创建带限制的Writer
	limitedOut := &utils.LimitedWriter{Writer: outFile, Max: resource.MaxOutputSize}
	limitedErr := &utils.LimitedWriter{Writer: errFile, Max: resource.MaxOutputSize}

	// 创建命令对象
	cmd := exec.Command(argument.Argv[0], argument.Argv[1:]...)
	cmd.Stdin = inFile
	cmd.Stdout = limitedOut
	cmd.Stderr = limitedErr
	cmd.Env = argument.Envp

	// 设置进程组属性
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Setpgid: true,
	}

	// 设置资源限制
	if err := SetResourceLimits(resource); err != nil {
		result.ExitCode = config.SystemError
		result.Message = "设置资源限制失败: " + err.Error()
		limitedErr.Write([]byte(result.Message + "\n"))
		return
	}

	// 启动程序
	err = cmd.Start()
	if err != nil {
		result.ExitCode = config.SystemError
		result.Message = "启动程序失败: " + err.Error()
		limitedErr.Write([]byte(result.Message + "\n"))
		return
	}

	// 创建完成通道
	done := make(chan error, 1)
	go func() {
		done <- cmd.Wait()
	}()

	// 设置超时时间
	timeout := time.Duration(resource.MaxRealTime) * time.Millisecond
	if timeout <= 0 {
		timeout = 10 * time.Second // 默认超时时间
	}

	select {
	case <-time.After(timeout):
		// 超时处理 - 杀死整个进程组
		syscall.Kill(-cmd.Process.Pid, syscall.SIGKILL)
		result.ExitCode = config.TimeLimitExceeded
		result.Message = "TIME_LIMIT_EXCEEDED"
		limitedErr.Write([]byte(result.Message + "\n"))
	case err := <-done:
		// 命令执行完成
		if err != nil {
			if exitErr, ok := err.(*exec.ExitError); ok {
				if status, ok := exitErr.Sys().(syscall.WaitStatus); ok {
					result.ExitCode = status.ExitStatus()
					if status.Signaled() {
						result.Signal = int(status.Signal())
						limitedErr.Write([]byte(fmt.Sprintf("进程被信号终止: %d\n", result.Signal)))
					}
				}
			} else {
				result.ExitCode = config.SystemError
				result.Message = "运行时错误: " + err.Error()
				limitedErr.Write([]byte(result.Message + "\n"))
			}
		} else {
			result.ExitCode = config.Success
		}
	}

	// 计算执行时间
	result.RealTime = time.Since(startTime).Milliseconds()

	// 获取资源使用情况
	if cmd.ProcessState != nil {
		if sysUsage, ok := cmd.ProcessState.SysUsage().(*syscall.Rusage); ok {
			result.CpuTime = int64(sysUsage.Utime.Nano()/1e6) + int64(sysUsage.Stime.Nano()/1e6)
			result.Memory = sysUsage.Maxrss

			// 检查内存限制
			if resource.MaxMemory > 0 && result.Memory > resource.MaxMemory {
				result.ExitCode = config.MemoryLimitExceeded
				result.Message = "MEMORY_LIMIT_EXCEEDED"
				limitedErr.Write([]byte(result.Message + "\n"))
			}
		}

		// 检查信号
		if status, ok := cmd.ProcessState.Sys().(syscall.WaitStatus); ok && status.Signaled() {
			sig := syscall.Signal(result.Signal)
			switch sig {
			case syscall.SIGSEGV:
				limitedErr.Write([]byte("发生段错误\n"))
				if resource.MaxStack > 0 {
					result.ExitCode = config.StackLimitExceeded
					result.Message = "STACK_LIMIT_EXCEEDED"
				} else {
					result.ExitCode = config.SystemError
					result.Message = "RUNTIME_ERROR"
				}
			case syscall.SIGXFSZ:
				result.ExitCode = config.OutputLimitExceeded
				result.Message = "OUTPUT_LIMIT_EXCEEDED"
			case syscall.SIGUSR1:
				result.ExitCode = config.ProcessLimitExceeded
				result.Message = "PROCESS_LIMIT_EXCEEDED"
			case syscall.SIGXCPU:
				result.ExitCode = config.TimeLimitExceeded
				result.Message = "CPU_TIME_LIMIT_EXCEEDED"
			}
			limitedErr.Write([]byte(result.Message + "\n"))
		}
	}
}

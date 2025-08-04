package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"os"
	"os/exec"
	"strings"
	"syscall"
	"time"

	"golang.org/x/sys/unix"
)

/* 系统级参数结构体 */
type Argument struct {
	Argv []string `json:"argv"` // 执行命令及其参数列表（如: ["./a.out", "arg1"]）
	Envp []string `json:"envp"` // 环境变量列表（如: ["PATH=/usr/bin", "HOME=/user"]）
}

/* 资源限制参数结构体 */
type Resource struct {
	MaxCpuTime     int64 `json:"maxCpuTime"`     // 最大CPU时间(毫秒)
	MaxRealTime    int64 `json:"maxRealTime"`    // 最大实际运行时间(毫秒)
	MaxMemory      int64 `json:"maxMemory"`      // 最大内存使用量(KB)
	MaxStack       int64 `json:"maxStack"`       // 最大栈大小(KB)
	MaxOutputSize  int64 `json:"maxOutputSize"`  // 最大输出大小(字节)
	MaxProcesses   int   `json:"maxProcesses"`   // 最大进程数
	MaxOpenFiles   int   `json:"maxOpenFiles"`   // 最大打开文件数
	MaxFileSize    int64 `json:"maxFileSize"`    // 最大文件大小(字节)
	MaxCompileTime int64 `json:"maxCompileTime"` // 最大编译时间(毫秒)
	MaxCompileMem  int64 `json:"maxCompileMem"`  // 最大编译内存(KB)
}

/* 额外配置参数结构体 */
type Additional struct {
	SeccompRuleName string `json:"seccompRuleName"` // seccomp安全规则名称
	InputFilePath   string `json:"inputFilePath"`   // 标准输入文件路径
	OutputFilePath  string `json:"outputFilePath"`  // 标准输出文件路径
	ErrorFilePath   string `json:"errorFilePath"`   // 标准错误文件路径
	LogFilePath     string `json:"logFilePath"`     // 日志文件路径
}

/* 执行结果结构体 */
type Result struct {
	ExitCode int    `json:"exitCode"` // 退出状态码
	Signal   int    `json:"signal"`   // 导致终止的信号
	Message  string `json:"message"`  // 结果消息

	CpuTime  int64 `json:"cpuTime"`  // 实际CPU时间(毫秒)
	RealTime int64 `json:"realTime"` // 实际运行时间(毫秒)
	Memory   int64 `json:"memory"`   // 实际内存使用(KB)
}

/* 错误代码常量定义 */
const (
	Success               = 0  // 成功
	TimeLimitExceeded     = 1  // 时间限制超出
	MemoryLimitExceeded   = 2  // 内存限制超出
	SystemError           = 3  // 系统错误
	FileError             = 4  // 文件操作错误
	ForkError             = 5  // 进程创建错误
	OutputLimitExceeded   = 6  // 输出大小超出限制
	StackLimitExceeded    = 7  // 栈大小超出限制
	ProcessLimitExceeded  = 8  // 进程数超出限制
	FileSizeLimitExceeded = 9  // 文件大小超出限制
	CompileError          = 10 // 编译错误
)

/* 带限制的Writer实现 */
type LimitedWriter struct {
	Writer  io.Writer // 底层Writer
	Max     int64     // 最大允许字节数
	Written int64     // 已写入字节数
}

// Write方法实现，带大小限制检查
func (l *LimitedWriter) Write(p []byte) (n int, err error) {
	if l.Max > 0 && l.Written+int64(len(p)) > l.Max {
		// 超过限制时只写入剩余允许的部分
		n, _ = l.Writer.Write(p[:l.Max-l.Written])
		l.Written = l.Max
		return n, fmt.Errorf("output size limit exceeded")
	}
	n, err = l.Writer.Write(p)
	l.Written += int64(n)
	return
}

/* 主函数 */
func main() {
	// 定义命令行参数变量
	var (
		isCompile       bool
		execArgv        string
		execEnvp        string
		maxCpuTime      int64
		maxRealTime     int64
		maxMemory       int64
		maxStack        int64
		maxOutputSize   int64
		maxProcesses    int
		maxOpenFiles    int
		maxFileSize     int64
		maxCompileTime  int64
		maxCompileMem   int64
		seccompRuleName string
		inputFilePath   string
		outputFilePath  string
		errorFilePath   string
		logFilePath     string
		printFlag       bool
		helpFlag        bool
	)

	// 绑定命令行参数
	flag.BoolVar(&isCompile, "compile", false, "启用编译模式")
	flag.StringVar(&execArgv, "execArgv", "", "执行参数列表（逗号分隔）")
	flag.StringVar(&execEnvp, "execEnvp", "", "环境变量列表（逗号分隔）")
	flag.Int64Var(&maxCpuTime, "maxCpuTime", 0, "最大CPU时间(毫秒)")
	flag.Int64Var(&maxRealTime, "maxRealTime", 0, "最大实际时间(毫秒)")
	flag.Int64Var(&maxMemory, "maxMemory", 0, "最大内存(KB)")
	flag.Int64Var(&maxStack, "maxStack", 0, "最大栈大小(KB)")
	flag.Int64Var(&maxOutputSize, "maxOutputSize", 0, "最大输出大小(字节)")
	flag.IntVar(&maxProcesses, "maxProcesses", 0, "最大进程数")
	flag.IntVar(&maxOpenFiles, "maxOpenFiles", 0, "最大打开文件数")
	flag.Int64Var(&maxFileSize, "maxFileSize", 0, "最大文件大小(字节)")
	flag.Int64Var(&maxCompileTime, "maxCompileTime", 0, "最大编译时间(毫秒)")
	flag.Int64Var(&maxCompileMem, "maxCompileMem", 0, "最大编译内存(KB)")
	flag.StringVar(&seccompRuleName, "seccompRuleName", "", "seccomp规则名称")
	flag.StringVar(&inputFilePath, "inputFilePath", "", "输入文件路径")
	flag.StringVar(&outputFilePath, "outputFilePath", "", "输出文件路径")
	flag.StringVar(&errorFilePath, "errorFilePath", "", "错误文件路径")
	flag.StringVar(&logFilePath, "logFilePath", "", "日志文件路径")
	flag.BoolVar(&printFlag, "print", false, "打印所有参数")
	flag.BoolVar(&helpFlag, "help", false, "显示帮助信息")

	flag.Parse()

	// 显示帮助信息
	if helpFlag {
		flag.PrintDefaults()
		os.Exit(0)
	}

	// 初始化参数结构体
	argument := Argument{}
	if execArgv != "" {
		argument.Argv = strings.Split(execArgv, ",")
	} else {
		fmt.Println("错误: execArgv不能为空")
		os.Exit(FileError)
	}

	if execEnvp != "" {
		argument.Envp = strings.Split(execEnvp, ",")
	}

	// 初始化资源限制
	resource := Resource{
		MaxCpuTime:     maxCpuTime,
		MaxRealTime:    maxRealTime,
		MaxMemory:      maxMemory,
		MaxStack:       maxStack,
		MaxOutputSize:  maxOutputSize,
		MaxProcesses:   maxProcesses,
		MaxOpenFiles:   maxOpenFiles,
		MaxFileSize:    maxFileSize,
		MaxCompileTime: maxCompileTime,
		MaxCompileMem:  maxCompileMem,
	}

	// 初始化额外配置
	additional := Additional{
		SeccompRuleName: seccompRuleName,
		InputFilePath:   inputFilePath,
		OutputFilePath:  outputFilePath,
		ErrorFilePath:   errorFilePath,
		LogFilePath:     logFilePath,
	}

	// 参数校验
	if !isCompile && additional.InputFilePath == "" {
		fmt.Println("错误: 运行模式下输入文件路径不能为空")
		os.Exit(FileError)
	}

	if additional.OutputFilePath == "" {
		fmt.Println("错误: 输出文件路径不能为空")
		os.Exit(FileError)
	}

	if additional.ErrorFilePath == "" {
		fmt.Println("错误: 错误文件路径不能为空")
		os.Exit(FileError)
	}

	// 调试模式打印参数
	if printFlag {
		printAll(argument, resource, additional)
	}

	// 执行编译或运行
	result := Result{}
	if isCompile {
		compile(argument, resource, additional, &result)
	} else {
		run(argument, resource, additional, &result)
	}

	// 输出结果
	printResult(result)

	// 记录日志
	logCommand(isCompile, argument, resource, additional, result)
}

/* 设置资源限制 */
func setResourceLimits(resource Resource) error {
	// 设置栈大小限制
	if resource.MaxStack > 0 {
		if err := syscall.Setrlimit(syscall.RLIMIT_STACK, &syscall.Rlimit{
			Cur: uint64(resource.MaxStack * 1024),
			Max: uint64(resource.MaxStack * 1024),
		}); err != nil {
			return fmt.Errorf("设置栈限制失败: %v", err)
		}
	}

	// 设置进程数限制
	if resource.MaxProcesses > 0 {
		if err := syscall.Setrlimit(unix.RLIMIT_NPROC, &syscall.Rlimit{
			Cur: uint64(resource.MaxProcesses),
			Max: uint64(resource.MaxProcesses),
		}); err != nil {
			return fmt.Errorf("设置进程数限制失败: %v", err)
		}
	}

	// 设置打开文件数限制
	if resource.MaxOpenFiles > 0 {
		if err := syscall.Setrlimit(syscall.RLIMIT_NOFILE, &syscall.Rlimit{
			Cur: uint64(resource.MaxOpenFiles),
			Max: uint64(resource.MaxOpenFiles),
		}); err != nil {
			return fmt.Errorf("设置打开文件数限制失败: %v", err)
		}
	}

	// 设置文件大小限制
	if resource.MaxFileSize > 0 {
		if err := syscall.Setrlimit(syscall.RLIMIT_FSIZE, &syscall.Rlimit{
			Cur: uint64(resource.MaxFileSize),
			Max: uint64(resource.MaxFileSize),
		}); err != nil {
			return fmt.Errorf("设置文件大小限制失败: %v", err)
		}
	}

	return nil
}

/* 运行目标程序 */
func run(argument Argument, resource Resource, additional Additional, result *Result) {
	startTime := time.Now()

	// 准备输入文件
	inFile, err := os.OpenFile(additional.InputFilePath, os.O_RDONLY, 0644)
	if err != nil {
		result.ExitCode = FileError
		result.Message = "打开输入文件失败: " + err.Error()
		return
	}
	defer inFile.Close()

	// 准备输出文件
	outFile, err := os.OpenFile(additional.OutputFilePath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0644)
	if err != nil {
		result.ExitCode = FileError
		result.Message = "打开输出文件失败: " + err.Error()
		return
	}
	defer outFile.Close()

	// 准备错误文件
	errFile, err := os.OpenFile(additional.ErrorFilePath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0644)
	if err != nil {
		result.ExitCode = FileError
		result.Message = "打开错误文件失败: " + err.Error()
		return
	}
	defer errFile.Close()

	// 创建带限制的Writer
	limitedOut := &LimitedWriter{Writer: outFile, Max: resource.MaxOutputSize}
	limitedErr := &LimitedWriter{Writer: errFile, Max: resource.MaxOutputSize}

	// 创建命令对象
	cmd := exec.Command(argument.Argv[0], argument.Argv[1:]...)
	cmd.Stdin = inFile
	cmd.Stdout = limitedOut
	cmd.Stderr = limitedErr
	cmd.Env = argument.Envp

	// 启动程序
	err = cmd.Start()
	if err != nil {
		result.ExitCode = SystemError
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
		result.ExitCode = TimeLimitExceeded
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
				result.ExitCode = SystemError
				result.Message = "运行时错误: " + err.Error()
				limitedErr.Write([]byte(result.Message + "\n"))
			}
		} else {
			result.ExitCode = Success
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
				result.ExitCode = MemoryLimitExceeded
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
					result.ExitCode = StackLimitExceeded
					result.Message = "STACK_LIMIT_EXCEEDED"
				} else {
					result.ExitCode = SystemError
					result.Message = "RUNTIME_ERROR"
				}
			case syscall.SIGXFSZ:
				result.ExitCode = OutputLimitExceeded
				result.Message = "OUTPUT_LIMIT_EXCEEDED"
			case syscall.SIGUSR1:
				result.ExitCode = ProcessLimitExceeded
				result.Message = "PROCESS_LIMIT_EXCEEDED"
			case syscall.SIGXCPU:
				result.ExitCode = TimeLimitExceeded
				result.Message = "CPU_TIME_LIMIT_EXCEEDED"
			}
			limitedErr.Write([]byte(result.Message + "\n"))
		}
	}
}

/* 编译目标程序 */
func compile(argument Argument, resource Resource, additional Additional, result *Result) {
	startTime := time.Now()

	// 设置编译内存限制
	if resource.MaxCompileMem > 0 {
		if err := syscall.Setrlimit(syscall.RLIMIT_AS, &syscall.Rlimit{
			Cur: uint64(resource.MaxCompileMem * 1024),
			Max: uint64(resource.MaxCompileMem * 1024),
		}); err != nil {
			result.ExitCode = SystemError
			result.Message = fmt.Sprintf("设置编译内存限制失败: %v", err)
			return
		}
	}

	// 准备输出文件
	outFile, err := os.OpenFile(additional.OutputFilePath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0644)
	if err != nil {
		result.ExitCode = FileError
		result.Message = "打开输出文件失败: " + err.Error()
		return
	}
	defer outFile.Close()

	// 准备错误文件
	errFile, err := os.OpenFile(additional.ErrorFilePath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0644)
	if err != nil {
		result.ExitCode = FileError
		result.Message = "打开错误文件失败: " + err.Error()
		return
	}
	defer errFile.Close()

	// 创建命令对象
	cmd := exec.Command(argument.Argv[0], argument.Argv[1:]...)
	cmd.Stdout = outFile
	cmd.Stderr = errFile
	cmd.Env = argument.Envp

	// 设置进程组属性
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Setpgid: true,
	}

	// 启动编译过程
	err = cmd.Start()
	if err != nil {
		result.ExitCode = SystemError
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
		result.ExitCode = TimeLimitExceeded
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
				result.ExitCode = CompileError
				result.Message = "编译错误: " + err.Error()
			}
		} else {
			result.ExitCode = Success
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
				result.ExitCode = MemoryLimitExceeded
				result.Message = "COMPILE_MEMORY_LIMIT_EXCEEDED"
			}
		}
	}
}

/* 打印执行结果 */
func printResult(result Result) {
	jsonData, _ := json.MarshalIndent(result, "", "    ")
	fmt.Println(string(jsonData))
}

/* 打印所有参数（调试用） */
func printAll(argument Argument, resource Resource, additional Additional) {
	data := map[string]interface{}{
		"Argument":   argument,
		"Resource":   resource,
		"Additional": additional,
	}
	jsonData, _ := json.MarshalIndent(data, "", "    ")
	fmt.Println(string(jsonData))
}

/* 记录执行日志 */
func logCommand(isCompile bool, argument Argument, resource Resource, additional Additional, result Result) {
	if additional.LogFilePath == "" {
		return
	}

	logFile, err := os.OpenFile(additional.LogFilePath, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		return
	}
	defer logFile.Close()

	logData := map[string]interface{}{
		"isCompile":  isCompile,
		"argument":   argument,
		"resource":   resource,
		"additional": additional,
		"result":     result,
		"timestamp":  time.Now().Format(time.RFC3339),
	}

	jsonData, _ := json.Marshal(logData)
	logFile.Write(jsonData)
	logFile.WriteString("\n")
}

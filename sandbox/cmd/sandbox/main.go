package main

import (
	"flag"
	"fmt"
	"os"
	"strings"

	"sandbox/internal/config"
	"sandbox/internal/executor"
	"sandbox/internal/result"
	"sandbox/internal/utils"
)

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
	argument := config.Argument{}
	if execArgv != "" {
		argument.Argv = strings.Split(execArgv, ",")
	} else {
		fmt.Println("错误: execArgv不能为空")
		os.Exit(config.FileError)
	}

	if execEnvp != "" {
		argument.Envp = strings.Split(execEnvp, ",")
	}

	// 初始化资源限制
	resource := config.Resource{
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
	additional := config.Additional{
		SeccompRuleName: seccompRuleName,
		InputFilePath:   inputFilePath,
		OutputFilePath:  outputFilePath,
		ErrorFilePath:   errorFilePath,
		LogFilePath:     logFilePath,
	}

	// 参数校验
	if !isCompile && additional.InputFilePath == "" {
		fmt.Println("错误: 运行模式下输入文件路径不能为空")
		os.Exit(config.FileError)
	}

	if additional.OutputFilePath == "" {
		fmt.Println("错误: 输出文件路径不能为空")
		os.Exit(config.FileError)
	}

	if additional.ErrorFilePath == "" {
		fmt.Println("错误: 错误文件路径不能为空")
		os.Exit(config.FileError)
	}

	// 调试模式打印参数
	if printFlag {
		utils.PrintAll(argument, resource, additional)
	}

	// 执行编译或运行
	result := result.Result{}
	if isCompile {
		executor.Compile(argument, resource, additional, &result)
	} else {
		//executor.RunWithoutFiles(argument, resource, additional, &result)
		executor.Run(argument, resource, additional, &result)
	}

	// 输出结果
	result.Print()

	// 记录日志
	utils.LogCommand(isCompile, argument, resource, additional, result)
}

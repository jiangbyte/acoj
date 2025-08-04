package config

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
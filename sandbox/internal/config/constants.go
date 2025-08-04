package config

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
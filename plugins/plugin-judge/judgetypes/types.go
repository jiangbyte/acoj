package judgetypes

// ExecRequest 单次执行请求
type ExecRequest struct {
	Code        string   `json:"code"`
	Language    string   `json:"language"`
	Stdin       string   `json:"stdin"`
	MaxCPUTime  int64    `json:"max_cpu_time"`
	MaxRealTime int64    `json:"max_real_time"`
	MaxMemory   int64    `json:"max_memory"`
	MaxStack    int64    `json:"max_stack"`
	MaxOutput   int64    `json:"max_output"`
	Env         []string `json:"env"`
	Binary      []byte   `json:"-"` // 预编译二进制: 非空时跳过编译阶段, 直接运行
}

// ExecResult 单次执行结果
type ExecResult struct {
	Status     string `json:"status"`
	ExitCode   int    `json:"exit_code"`
	TimeUsed   int64  `json:"time_used"`
	MemoryUsed int64  `json:"memory_used"`
	Stdout     string `json:"stdout"`
	Stderr     string `json:"stderr"`
	Error      string `json:"error"`
	Binary     []byte `json:"-"` // 编译产出的二进制文件 (引擎可缓存复用)
}

// HealthStatus 健康状态
type HealthStatus struct {
	Alive       bool   `json:"alive"`
	Version     string `json:"version"`
	BackendName string `json:"backend_name"`
	Error       string `json:"error,omitempty"`
}

// SandboxBackend 沙箱后端接口
type SandboxBackend interface {
	Name() string
	Exec(req *ExecRequest) (*ExecResult, error)
	BatchExec(reqs []*ExecRequest) ([]*ExecResult, error)
	Health() *HealthStatus
	// InteractiveExec 交互式判题
	// 用户程序与交互器通过管道连接, 在同一沙箱中并行运行
	// testInput: 测试用例输入, 以文件形式提供给交互器
	// 返回交互器的执行结果 (exit_code=0=AC)
	InteractiveExec(userReq, interactorReq *ExecRequest, testInput string) (*ExecResult, error)
}

const (
	StatusAccepted     = "Accepted"
	StatusCompileError = "CompileError"
	StatusTLE          = "TLE"
	StatusMLE          = "MLE"
	StatusRE           = "RE"
	StatusSE           = "SE"
	StatusWrongAnswer  = "WrongAnswer"
)

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

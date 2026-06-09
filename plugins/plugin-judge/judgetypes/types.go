package judgetypes

import "strings"

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

// 判题结果状态码
const (
	StatusAccepted        = "Accepted"
	StatusCompileError    = "CompileError"
	StatusTLE             = "TLE"
	StatusMLE             = "MLE"
	StatusOLE             = "OutputLimitExceeded"
	StatusRE              = "RE"
	StatusSE              = "SE"
	StatusWrongAnswer     = "WrongAnswer"
	StatusPE              = "PresentationError"
	StatusRF              = "RestrictedFunction"
)

// StatusWorse 判题结果严重程度排序（数值越小越严重）
// 用于聚合多个测试用例结果时取最严重状态
var StatusWorse = map[string]int{
	StatusSE: 0, StatusRF: 1, StatusOLE: 2, StatusMLE: 3,
	StatusTLE: 4, StatusRE: 5, StatusPE: 6, StatusWrongAnswer: 7,
	StatusCompileError: 8, StatusAccepted: 9,
}

// IsWorseThan returns true if newStatus represents a worse result than oldStatus
func IsWorseThan(newStatus, oldStatus string) bool {
	np, nok := StatusWorse[newStatus]
	op, _ := StatusWorse[oldStatus]
	if !nok {
		return false
	}
	return np < op
}

// IsAccepted returns true if the status is Accepted
func IsAccepted(status string) bool {
	return status == StatusAccepted
}

// HasStdoutStatus returns true if the sandbox result status has usable stdout
func HasStdoutStatus(status string) bool {
	return status == StatusAccepted || status == StatusWrongAnswer || status == StatusPE
}

// IsOutputTrimmable returns true if trailing whitespace differences may be ignored
func IsOutputTrimmable(strictCompare bool) bool {
	return !strictCompare
}

// NormalizeOutput 根据是否严格模式标准化输出字符串
func NormalizeOutput(s string, strictCompare bool) string {
	if strictCompare {
		return s
	}
	// 宽容模式: 移除行尾空白 + 末尾空行
	lines := strings.Split(strings.TrimRight(s, "\n"), "\n")
	for i, l := range lines {
		lines[i] = strings.TrimRight(l, " \t\r")
	}
	return strings.Join(lines, "\n")
}

package dto

type JudgeStatus string

const (
	// Basic states
	StatusPending    JudgeStatus = "PENDING"
	StatusJudging    JudgeStatus = "JUDGING"
	StatusCompiling  JudgeStatus = "COMPILING"
	StatusCompiledOK JudgeStatus = "COMPILED_OK"
	StatusRunning    JudgeStatus = "RUNNING"

	// Result states
	StatusAccepted            JudgeStatus = "ACCEPTED"
	StatusWrongAnswer         JudgeStatus = "WRONG_ANSWER"
	StatusTimeLimitExceeded   JudgeStatus = "TIME_LIMIT_EXCEEDED"
	StatusMemoryLimitExceeded JudgeStatus = "MEMORY_LIMIT_EXCEEDED"
	StatusRuntimeError        JudgeStatus = "RUNTIME_ERROR"
	StatusCompilationError    JudgeStatus = "COMPILATION_ERROR"
	StatusPresentationError   JudgeStatus = "PRESENTATION_ERROR"
	StatusOutputLimitExceeded JudgeStatus = "OUTPUT_LIMIT_EXCEEDED"

	// Special states
	StatusPartialAccepted    JudgeStatus = "PARTIAL_ACCEPTED"
	StatusSystemError        JudgeStatus = "SYSTEM_ERROR"
	StatusRestrictedFunction JudgeStatus = "RESTRICTED_FUNCTION"
	StatusDangerousOperation JudgeStatus = "DANGEROUS_OPERATION"
	StatusQueuing            JudgeStatus = "QUEUING"
	StatusHidden             JudgeStatus = "HIDDEN"
	StatusSkipped            JudgeStatus = "SKIPPED"
	StatusFrozen             JudgeStatus = "FROZEN"

	// Contest-specific states
	StatusFirstBlood     JudgeStatus = "FIRST_BLOOD"
	StatusHacked         JudgeStatus = "HACKED"
	StatusPendingRejudge JudgeStatus = "PENDING_REJUDGE"
	StatusRejudging      JudgeStatus = "REJUDGING"

	// Other states
	StatusUnknownError          JudgeStatus = "UNKNOWN_ERROR"
	StatusValidatorError        JudgeStatus = "VALIDATOR_ERROR"
	StatusCheckerError          JudgeStatus = "CHECKER_ERROR"
	StatusIdlenessLimitExceeded JudgeStatus = "IDLENESS_LIMIT_EXCEEDED"
	StatusSecurityViolation     JudgeStatus = "SECURITY_VIOLATION"
	StatusIgnored               JudgeStatus = "IGNORED"
)

/* 提交题目 */
type JudgeSubmitDto struct {
	UserId     string     `json:"userId"`
	ProblemId  string     `json:"problemId"`
	SetId      string     `json:"setId"`
	Language   string     `json:"language"`
	Code       string     `json:"code"`
	SubmitType bool       `json:"submitType"`
	MaxTime    int        `json:"maxTime"`   // ms
	MaxMemory  int        `json:"maxMemory"` // kb
	TestCase   []TestCase `json:"testCase"`
	ID         string     `json:"id"`
	IsSet      bool       `json:"isSet"`
	JudgeTaskId      string     `json:"judgeTaskId"`
}

type JudgeResultDto struct {
	UserId     string           `json:"userId"`
	ProblemId  string           `json:"problemId"`
	Language   string           `json:"language"`
	SetId      string           `json:"setId"`
	Code       string           `json:"code"`
	SubmitType bool             `json:"submitType"`
	MaxTime    int              `json:"maxTime"`   // ms
	MaxMemory  int              `json:"maxMemory"` // kb
	TestCase   []SubmitTestCase `json:"testCase"`
	Message    string           `json:"message"`
	ID         string           `json:"id"`
	IsSet      bool             `json:"isSet"`
	Status     JudgeStatus      `json:"status"`
	ExitCode   int              `json:"exitCode"` // 程序退出码
	JudgeTaskId      string     `json:"judgeTaskId"`
}

/* 测试用例 */
type TestCase struct {
	Input  string `json:"input"`
	Output string `json:"output"`
}

/* 返回测试用例 */
type SubmitTestCase struct {
	Input     string      `json:"input"`
	Output    string      `json:"output"`
	Except    string      `json:"except"`
	MaxTime   int         `json:"maxTime"`   // ms
	MaxMemory int         `json:"maxMemory"` // kb
	Message   string      `json:"message"`
	Status    JudgeStatus `json:"status"`
	ExitCode  int         `json:"exitCode"` // 程序退出码
}

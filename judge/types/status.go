package types

// Judge status constants.
const (
	StatusPending          = "PENDING"
	StatusJudging          = "JUDGING"
	StatusAccepted         = "AC"
	StatusWrongAnswer      = "WA"
	StatusTimeLimitExceeded  = "TLE"
	StatusMemoryLimitExceeded = "MLE"
	StatusRuntimeError     = "RE"
	StatusCompileError     = "COMPILE_ERROR"
	StatusPartial          = "PARTIAL"
	StatusSystemError      = "SE"
	StatusHacked           = "HACKED"
	StatusSkipped          = "SKIPPED"
)

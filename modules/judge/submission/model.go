package submission

import "time"

// JudgeSubmission represents a code submission for judging.
type JudgeSubmission struct {
	ID            string     `gorm:"primaryKey;size:32" json:"id"`
	UserID        string     `gorm:"size:32;index" json:"user_id"`
	ProblemID     string     `gorm:"size:32;index" json:"problem_id"`
	ContestID     *string    `gorm:"size:32;index" json:"contest_id"`
	SetID         *string    `gorm:"size:32;index" json:"set_id"`
	ContestMode   string     `gorm:"size:32" json:"contest_mode"` // ACM / OI / IOI / CF / HOMEWORK
	IsPretest     bool       `json:"is_pretest"`
	Language      string     `gorm:"size:32" json:"language"`
	Code          string     `gorm:"type:longtext" json:"code"`
	Status        string     `gorm:"size:32;default:PENDING" json:"status"`
	Score         int        `gorm:"default:0" json:"score"`
	TimeUsed      int64      `gorm:"default:0" json:"time_used"`      // μs
	MemoryUsed    int64      `gorm:"default:0" json:"memory_used"`    // KB
	TestcasePass  int        `gorm:"default:0" json:"testcase_pass"`
	TestcaseTotal int        `gorm:"default:0" json:"testcase_total"`
	ErrorInfo     string     `gorm:"type:text" json:"error_info"`
	CreatedAt     *time.Time `json:"created_at"`
	CreatedBy     *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt     *time.Time `json:"updated_at"`
	UpdatedBy     *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeSubmission) TableName() string { return "judge_submission" }

// JudgeTestcaseResult represents the result of a single test case.
type JudgeTestcaseResult struct {
	ID             string `gorm:"primaryKey;size:32" json:"id"`
	SubmissionID   string `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"submission_id"`
	Index          int    `json:"index"`
	Status         string `gorm:"size:32" json:"status"`
	Score          int    `json:"score"`
	TimeUsed       int64  `json:"time_used"`        // μs
	MemoryUsed     int64  `json:"memory_used"`      // KB
	Output         string `gorm:"type:text" json:"output"`
	ExpectedOutput string `gorm:"type:text" json:"expected_output"`
	Input          string `gorm:"type:text" json:"input"`
	SubtaskID      int    `json:"subtask_id"`
}

func (JudgeTestcaseResult) TableName() string { return "judge_testcase_result" }

const (
	StatusPending    = "PENDING"
	StatusJudging    = "JUDGING"
	StatusAccepted   = "AC"
	StatusWrongAnswer = "WA"
	StatusTimeLimitExceeded = "TLE"
	StatusMemoryLimitExceeded = "MLE"
	StatusRuntimeError = "RE"
	StatusCompileError = "COMPILE_ERROR"
	StatusPartial    = "PARTIAL"
	StatusSystemError = "SE"
	StatusHacked     = "HACKED"
	StatusSkipped    = "SKIPPED"
)

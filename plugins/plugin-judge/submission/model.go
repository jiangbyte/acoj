package submission

import "time"

// JudgeSubmission 提交记录实体
type JudgeSubmission struct {
	ID           string     `gorm:"primaryKey;size:32" json:"id"`
	ProblemID    string     `gorm:"size:32;index" json:"problem_id"`
	UserID       string     `gorm:"size:32;index" json:"user_id"`
	ContestID    string     `gorm:"size:32;index;default:''" json:"contest_id"`
	Language     string     `gorm:"size:32" json:"language"`
	Code         string     `gorm:"type:longtext" json:"code"`
	Status       string     `gorm:"size:32;default:PENDING;index" json:"status"` // PENDING / JUDGING / Accepted / CompileError / TLE / MLE / OLE / RE / SE / WrongAnswer / PresentationError / RestrictedFunction
	SubmissionType string   `gorm:"size:16;default:contest;index" json:"submission_type"` // contest / practice / test
	Score        int        `gorm:"default:0" json:"score"`
	TimeUsed     int64      `gorm:"default:0" json:"time_used"`     // ns
	MemoryUsed   int64      `gorm:"default:0" json:"memory_used"`  // byte
	ErrorMessage string     `gorm:"type:text" json:"error_message"`
	IP           string     `gorm:"size:64" json:"ip"`
	CreatedAt    *time.Time `json:"created_at"`
	CreatedBy    *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt    *time.Time `json:"updated_at"`
	UpdatedBy    *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeSubmission) TableName() string { return "judge_submission" }

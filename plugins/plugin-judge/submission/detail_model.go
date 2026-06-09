package submission

import (
	"time"
	"hei-gin/sdk/db"
)

// JudgeSubmissionDetail 提交记录详情（每个测试用例的判题结果）
type JudgeSubmissionDetail struct {
	ID           string `gorm:"primaryKey;size:32" json:"id"`
	SubmissionID string `gorm:"size:32;index:idx_submission_detail;index" json:"submission_id"`
	ProblemID    string `gorm:"size:32" json:"problem_id"`
	TestCaseID   string `gorm:"size:32;index" json:"testcase_id"`
	GroupID      string `gorm:"size:32;default:'';index" json:"group_id"` // 子任务分组ID
	Order        int    `gorm:"default:0" json:"order"`                   // 测试用例序号
	Status       string `gorm:"size:32;default:PENDING" json:"status"`    // 该用例的判题结果状态码
	Score        int    `gorm:"default:0" json:"score"`                   // 该用例得分
	TimeUsed     int64  `gorm:"default:0" json:"time_used"`               // ns
	MemoryUsed   int64  `gorm:"default:0" json:"memory_used"`             // byte
	Stderr       string     `gorm:"type:text" json:"stderr"`                  // 运行时的标准错误输出
	CreatedAt    *time.Time `json:"created_at"`
	CreatedBy    *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt    *time.Time `json:"updated_at"`
	UpdatedBy    *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeSubmissionDetail) TableName() string { return "judge_submission_detail" }

func init() {
	db.RegisterModel(&JudgeSubmissionDetail{})
}

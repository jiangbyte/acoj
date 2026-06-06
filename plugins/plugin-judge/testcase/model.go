package testcase

import "time"

// JudgeTestcase 测试用例实体
type JudgeTestcase struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	ProblemID string     `gorm:"size:32;index" json:"problem_id"`
	Input     string     `gorm:"type:longtext" json:"input"`
	Output    string     `gorm:"type:longtext" json:"output"`
	Order     int        `gorm:"default:0" json:"order"`
	IsSample  bool       `gorm:"default:false" json:"is_sample"`
	Score     int        `gorm:"default:100" json:"score"`
	CreatedAt *time.Time `json:"created_at"`
	UpdatedAt *time.Time `json:"updated_at"`
}

func (JudgeTestcase) TableName() string { return "judge_testcase" }

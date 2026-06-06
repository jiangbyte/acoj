package problemset

import "time"

// JudgeProblemset 题单实体
type JudgeProblemset struct {
	ID          string     `gorm:"primaryKey;size:32" json:"id"`
	Title       string     `gorm:"size:255;index" json:"title"`
	Description string     `gorm:"type:text" json:"description"`
	Status      string     `gorm:"size:16;default:ACTIVE" json:"status"` // ACTIVE / HIDDEN / DELETED
	Sort        int        `gorm:"default:0" json:"sort"`
	CreatedBy   string     `gorm:"size:32" json:"created_by"`
	CreatedAt   *time.Time `json:"created_at"`
	UpdatedAt   *time.Time `json:"updated_at"`
}

func (JudgeProblemset) TableName() string { return "judge_problemset" }

// RelProblemsetProblem 题单-题目关联
type RelProblemsetProblem struct {
	ID           string `gorm:"primaryKey;size:32" json:"id"`
	ProblemsetID string `gorm:"size:32;uniqueIndex:idx_ps_problem" json:"problemset_id"`
	ProblemID    string `gorm:"size:32;uniqueIndex:idx_ps_problem;index" json:"problem_id"`
	Sort         int    `gorm:"default:0" json:"sort"`
}

func (RelProblemsetProblem) TableName() string { return "rel_problemset_problem" }

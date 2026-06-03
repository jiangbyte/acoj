package problemset

import "time"

type JudgeProblemSet struct {
	ID            string     `gorm:"primaryKey;size:32" json:"id"`
	Title         string     `gorm:"size:255;not null" json:"title"`
	Description   string     `gorm:"type:longtext" json:"description"`
	Type          string     `gorm:"size:16" json:"type"`
	Visibility    string     `gorm:"size:16" json:"visibility"`
	UserID        string     `gorm:"size:32;index" json:"user_id"`
	Status        string     `gorm:"size:16;default:ENABLED" json:"status"`
	ProblemCount  int        `gorm:"default:0" json:"problem_count"`
	CreatedAt     *time.Time `json:"created_at"`
	CreatedBy     *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt     *time.Time `json:"updated_at"`
	UpdatedBy     *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeProblemSet) TableName() string { return "judge_problem_set" }

type JudgeProblemSetItem struct {
	ID        string `gorm:"primaryKey;size:32" json:"id"`
	SetID     string `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"set_id"`
	ProblemID string `gorm:"size:32;index" json:"problem_id"`
	SortOrder int    `json:"sort_order"`
	Note      string `gorm:"type:text" json:"note"`
}

func (JudgeProblemSetItem) TableName() string { return "judge_problem_set_item" }

type JudgeProblemSetProgress struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	SetID     string     `gorm:"size:32;index;uniqueIndex:idx_prog_sup;constraint:OnDelete:CASCADE" json:"set_id"`
	UserID    string     `gorm:"size:32;index;uniqueIndex:idx_prog_sup" json:"user_id"`
	ProblemID string     `gorm:"size:32;index;uniqueIndex:idx_prog_sup" json:"problem_id"`
	Solved    bool       `json:"solved"`
	SolvedAt  *time.Time `json:"solved_at"`
	Attempts  int        `gorm:"default:0" json:"attempts"`
}

func (JudgeProblemSetProgress) TableName() string { return "judge_problem_set_progress" }

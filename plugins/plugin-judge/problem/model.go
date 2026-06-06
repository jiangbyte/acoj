package problem

import "time"

// JudgeProblem 题目实体
// 资源限制已移至 judge_problem_language_limit 表, 按语言独立配置
type JudgeProblem struct {
	ID            string     `gorm:"primaryKey;size:32" json:"id"`
	Title         string     `gorm:"size:255;index" json:"title"`
	Description   string     `gorm:"type:longtext" json:"description"`
	InputDesc     string     `gorm:"type:text" json:"input_desc"`
	OutputDesc    string     `gorm:"type:text" json:"output_desc"`
	SampleInput   string     `gorm:"type:text" json:"sample_input"`
	SampleOutput  string     `gorm:"type:text" json:"sample_output"`
	Hint          string     `gorm:"type:text" json:"hint"`
	Source        string     `gorm:"size:255" json:"source"`
	JudgeType     string     `gorm:"size:16;default:default" json:"judge_type"` // default / spj / interactive
	SpjCode       string     `gorm:"type:text" json:"spj_code"`
	SpjLanguage   string     `gorm:"size:32" json:"spj_language"`
	InteractiveCode string   `gorm:"type:text" json:"interactive_code"`
	InteractiveLang string   `gorm:"size:32" json:"interactive_lang"`
	Difficulty    string     `gorm:"size:16;default:EASY" json:"difficulty"` // EASY / MEDIUM / HARD
	Status        string     `gorm:"size:16;default:ACTIVE" json:"status"`   // ACTIVE / HIDDEN / DELETED
	SubmitCount   int        `gorm:"default:0" json:"submit_count"`
	AcceptCount   int        `gorm:"default:0" json:"accept_count"`
	CreatedBy     string     `gorm:"size:32" json:"created_by"`
	CreatedAt     *time.Time `json:"created_at"`
	UpdatedAt     *time.Time `json:"updated_at"`
}

func (JudgeProblem) TableName() string { return "judge_problem" }

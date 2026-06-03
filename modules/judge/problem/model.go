package problem

import "time"

// JudgeProblem represents a problem in the judge system.
type JudgeProblem struct {
	ID            string     `gorm:"primaryKey;size:32" json:"id"`
	Title         string     `gorm:"size:255;not null" json:"title"`
	Description   string     `gorm:"type:longtext" json:"description"`
	InputDesc     string     `gorm:"type:longtext" json:"input_desc"`
	OutputDesc    string     `gorm:"type:longtext" json:"output_desc"`
	Hint          string     `gorm:"type:text" json:"hint"`
	JudgeMethod   string     `gorm:"size:32" json:"judge_method"` // STANDARD / SPJ / SUBTASK / INTERACTIVE
	TimeLimitMs   int        `gorm:"default:1000" json:"time_limit_ms"`
	MemoryLimitKb int64      `gorm:"default:262144" json:"memory_limit_kb"`
	SpjSource     string     `gorm:"type:longtext" json:"spj_source"`
	SpjLanguage   string     `gorm:"size:32" json:"spj_language"`
	InteractorSrc string     `gorm:"type:longtext" json:"interactor_src"`
	Status        string     `gorm:"size:16;default:ENABLED" json:"status"`
	CreatedAt     *time.Time `json:"created_at"`
	CreatedBy     *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt     *time.Time `json:"updated_at"`
	UpdatedBy     *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeProblem) TableName() string { return "judge_problem" }

// JudgeProblemLanguage represents a language configuration for a problem.
type JudgeProblemLanguage struct {
	ID            string  `gorm:"primaryKey;size:32" json:"id"`
	ProblemID     string  `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"problem_id"`
	Language      string  `gorm:"size:32" json:"language"`
	Template      string  `gorm:"type:text" json:"template"`
	TimeLimitMs   *int    `json:"time_limit_ms"`
	MemoryLimitKb *int64  `json:"memory_limit_kb"`
}

func (JudgeProblemLanguage) TableName() string { return "judge_problem_language" }

// JudgeProblemSample represents a sample test case for a problem.
type JudgeProblemSample struct {
	ID        string `gorm:"primaryKey;size:32" json:"id"`
	ProblemID string `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"problem_id"`
	SortOrder int    `json:"sort_order"`
	Input     string `gorm:"type:text" json:"input"`
	Output    string `gorm:"type:text" json:"output"`
}

func (JudgeProblemSample) TableName() string { return "judge_problem_sample" }

// JudgeProblemSubtask represents a subtask definition.
type JudgeProblemSubtask struct {
	ID          string `gorm:"primaryKey;size:32" json:"id"`
	ProblemID   string `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"problem_id"`
	SortOrder   int    `json:"sort_order"`
	Score       int    `json:"score"`
	JudgeMethod string `gorm:"size:32" json:"judge_method"`
}

func (JudgeProblemSubtask) TableName() string { return "judge_problem_subtask" }

// JudgeProblemSubtaskDep represents a subtask dependency.
type JudgeProblemSubtaskDep struct {
	ID                 string `gorm:"primaryKey;size:32" json:"id"`
	SubtaskID          string `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"subtask_id"`
	DependsOnSubtaskID string `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"depends_on_subtask_id"`
}

func (JudgeProblemSubtaskDep) TableName() string { return "judge_problem_subtask_dep" }

// JudgeProblemTestCase represents a test case.
type JudgeProblemTestCase struct {
	ID          string  `gorm:"primaryKey;size:32" json:"id"`
	ProblemID   string  `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"problem_id"`
	SubtaskID   *string `gorm:"size:32;index" json:"subtask_id"` // NULL for independent cases
	SortOrder   int     `json:"sort_order"`
	Input       string  `gorm:"type:mediumtext" json:"input"`
	Output      string  `gorm:"type:mediumtext" json:"output"`
	TimeLimitMs *int    `json:"time_limit_ms"`
	MemLimitKb  *int64  `json:"mem_limit_kb"`
	Score       int     `json:"score"`
}

func (JudgeProblemTestCase) TableName() string { return "judge_problem_testcase" }

const (
	JudgeMethodStandard   = "STANDARD"
	JudgeMethodSPJ        = "SPJ"
	JudgeMethodSubtask    = "SUBTASK"
	JudgeMethodInteractive = "INTERACTIVE"
)

package problem

// ProblemVO is the view object for a problem.
type ProblemVO struct {
	ID            string               `json:"id"`
	Title         string               `json:"title"`
	Description   string               `json:"description"`
	InputDesc     string               `json:"input_desc"`
	OutputDesc    string               `json:"output_desc"`
	Hint          string               `json:"hint"`
	JudgeMethod   string               `json:"judge_method"`
	TimeLimitMs   int                  `json:"time_limit_ms"`
	MemoryLimitKb int64                `json:"memory_limit_kb"`
	SpjSource     string               `json:"spj_source"`
	SpjLanguage   string               `json:"spj_language"`
	InteractorSrc string               `json:"interactor_src"`
	Status        string               `json:"status"`
	Languages     []LanguageVO         `json:"languages,omitempty"`
	Samples       []SampleVO           `json:"samples,omitempty"`
	TestCases     []TestCaseVO         `json:"testcases,omitempty"`
	Subtasks      []SubtaskVO          `json:"subtasks,omitempty"`
	CreatedAt     *string              `json:"created_at"`
	CreatedBy     *string              `json:"created_by"`
	UpdatedAt     *string              `json:"updated_at"`
	UpdatedBy     *string              `json:"updated_by"`
}

type LanguageVO struct {
	ID            string  `json:"id"`
	Language      string  `json:"language"`
	Template      string  `json:"template"`
	TimeLimitMs   *int    `json:"time_limit_ms"`
	MemoryLimitKb *int64  `json:"memory_limit_kb"`
}

type SampleVO struct {
	ID        string `json:"id"`
	SortOrder int    `json:"sort_order"`
	Input     string `json:"input"`
	Output    string `json:"output"`
}

type TestCaseVO struct {
	ID          string  `json:"id"`
	SubtaskID   *string `json:"subtask_id"`
	SortOrder   int     `json:"sort_order"`
	Input       string  `json:"input"`
	Output      string  `json:"output"`
	TimeLimitMs *int    `json:"time_limit_ms"`
	MemLimitKb  *int64  `json:"mem_limit_kb"`
	Score       int     `json:"score"`
}

type SubtaskVO struct {
	ID          string `json:"id"`
	SortOrder   int    `json:"sort_order"`
	Score       int    `json:"score"`
	JudgeMethod string `json:"judge_method"`
}

type ProblemCreateParam struct {
	Title         string `json:"title"`
	Description   string `json:"description"`
	InputDesc     string `json:"input_desc"`
	OutputDesc    string `json:"output_desc"`
	Hint          string `json:"hint"`
	JudgeMethod   string `json:"judge_method"`
	TimeLimitMs   int    `json:"time_limit_ms"`
	MemoryLimitKb int64  `json:"memory_limit_kb"`
	SpjSource     string `json:"spj_source"`
	SpjLanguage   string `json:"spj_language"`
	InteractorSrc string `json:"interactor_src"`
	Status        string `json:"status"`
}

type ProblemModifyParam struct {
	ID            string `json:"id"`
	Title         string `json:"title"`
	Description   string `json:"description"`
	InputDesc     string `json:"input_desc"`
	OutputDesc    string `json:"output_desc"`
	Hint          string `json:"hint"`
	JudgeMethod   string `json:"judge_method"`
	TimeLimitMs   int    `json:"time_limit_ms"`
	MemoryLimitKb int64  `json:"memory_limit_kb"`
	SpjSource     string `json:"spj_source"`
	SpjLanguage   string `json:"spj_language"`
	InteractorSrc string `json:"interactor_src"`
	Status        string `json:"status"`
}

type ProblemPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Title   string `json:"title" form:"title"`
	Status  string `json:"status" form:"status"`
}

// ===== Testcase Management Params =====

type TestcaseAddParam struct {
	ProblemID   string  `json:"problem_id"`
	SubtaskID   *string `json:"subtask_id"`
	SortOrder   int     `json:"sort_order"`
	Input       string  `json:"input"`
	Output      string  `json:"output"`
	TimeLimitMs *int    `json:"time_limit_ms"`
	MemLimitKb  *int64  `json:"mem_limit_kb"`
	Score       int     `json:"score"`
}

type TestcaseModifyParam struct {
	ID          string  `json:"id"`
	SubtaskID   *string `json:"subtask_id"`
	SortOrder   int     `json:"sort_order"`
	Input       string  `json:"input"`
	Output      string  `json:"output"`
	TimeLimitMs *int    `json:"time_limit_ms"`
	MemLimitKb  *int64  `json:"mem_limit_kb"`
	Score       int     `json:"score"`
}

type TestcaseRemoveParam struct {
	IDs []string `json:"ids"`
}

// ===== Language Sync Param =====

type LanguageItem struct {
	Language      string `json:"language"`
	Template      string `json:"template"`
	TimeLimitMs   *int   `json:"time_limit_ms"`
	MemoryLimitKb *int64 `json:"memory_limit_kb"`
}

type LanguageSyncParam struct {
	ProblemID string         `json:"problem_id"`
	Languages []LanguageItem `json:"languages"`
}

// ===== Sample Management Params =====

type SampleAddParam struct {
	ProblemID string `json:"problem_id"`
	SortOrder int    `json:"sort_order"`
	Input     string `json:"input"`
	Output    string `json:"output"`
}

type SampleRemoveParam struct {
	IDs []string `json:"ids"`
}

type SubtaskAddParam struct {
	ProblemID   string `json:"problem_id"`
	SortOrder   int    `json:"sort_order"`
	Score       int    `json:"score"`
	JudgeMethod string `json:"judge_method"`
}

type SubtaskModifyParam struct {
	ID          string `json:"id"`
	SortOrder   int    `json:"sort_order"`
	Score       int    `json:"score"`
	JudgeMethod string `json:"judge_method"`
}

type DepAddParam struct {
	SubtaskID          string `json:"subtask_id"`
	DependsOnSubtaskID string `json:"depends_on_subtask_id"`
}

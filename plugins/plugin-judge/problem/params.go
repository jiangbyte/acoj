package problem

import "hei-gin/sdk/pojo"

type ProblemVO struct {
	ID            string   `json:"id"`
	Title         string   `json:"title"`
	Description   string   `json:"description"`
	InputDesc     string   `json:"input_desc"`
	OutputDesc    string   `json:"output_desc"`
	SampleInput   string   `json:"sample_input"`
	SampleOutput  string   `json:"sample_output"`
	Hint          string   `json:"hint"`
	Source        string   `json:"source"`
	TimeLimit     int64    `json:"time_limit"`
	MemoryLimit   int64    `json:"memory_limit"`
	StackLimit    int64    `json:"stack_limit"`
	OutputLimit   int64    `json:"output_limit"`
	JudgeType     string   `json:"judge_type"`
	SpjCode       string   `json:"spj_code"`
	SpjLanguage   string   `json:"spj_language"`
	InteractiveCode string  `json:"interactive_code"`
	InteractiveLang string  `json:"interactive_lang"`
	Difficulty    string   `json:"difficulty"`
	Status        string   `json:"status"`
	SubmitCount   int      `json:"submit_count"`
	AcceptCount   int      `json:"accept_count"`
	CreatedBy     string   `json:"created_by"`
	CreatedAt     string   `json:"created_at"`
	UpdatedAt     string   `json:"updated_at"`
	Tags          []string `json:"tags"`
}

type ProblemPageParam struct {
	Current   int    `json:"current" form:"current"`
	Size      int    `json:"size" form:"size"`
	Keyword   string `json:"keyword" form:"keyword"`
	Difficulty string `json:"difficulty" form:"difficulty"`
	Status    string `json:"status" form:"status"`
	TagID     string `json:"tag_id" form:"tag_id"`
}

type ProblemCreateParam struct {
	Title           string   `json:"title" binding:"required"`
	Description     string   `json:"description"`
	InputDesc       string   `json:"input_desc"`
	OutputDesc      string   `json:"output_desc"`
	SampleInput     string   `json:"sample_input"`
	SampleOutput    string   `json:"sample_output"`
	Hint            string   `json:"hint"`
	Source          string   `json:"source"`
	TimeLimit       int64    `json:"time_limit"`
	MemoryLimit     int64    `json:"memory_limit"`
	StackLimit      int64    `json:"stack_limit"`
	OutputLimit     int64    `json:"output_limit"`
	JudgeType       string   `json:"judge_type"`
	SpjCode         string   `json:"spj_code"`
	SpjLanguage     string   `json:"spj_language"`
	InteractiveCode string   `json:"interactive_code"`
	InteractiveLang string   `json:"interactive_lang"`
	Difficulty      string   `json:"difficulty"`
	Status          string   `json:"status"`
	TagIDs          []string `json:"tag_ids"`
}

type ProblemModifyParam struct {
	ID              string   `json:"id" binding:"required"`
	Title           string    `json:"title"`
	Description     string    `json:"description"`
	InputDesc       string    `json:"input_desc"`
	OutputDesc      string    `json:"output_desc"`
	SampleInput     string    `json:"sample_input"`
	SampleOutput    string    `json:"sample_output"`
	Hint            string    `json:"hint"`
	Source          string    `json:"source"`
	TimeLimit       *int64    `json:"time_limit"`
	MemoryLimit     *int64    `json:"memory_limit"`
	StackLimit      *int64    `json:"stack_limit"`
	OutputLimit     *int64    `json:"output_limit"`
	JudgeType       string    `json:"judge_type"`
	SpjCode         string    `json:"spj_code"`
	SpjLanguage     string    `json:"spj_language"`
	InteractiveCode string    `json:"interactive_code"`
	InteractiveLang string    `json:"interactive_lang"`
	Difficulty      string    `json:"difficulty"`
	Status          string    `json:"status"`
	TagIDs          []string  `json:"tag_ids"`
}

type ProblemRemoveParam pojo.IdsParam

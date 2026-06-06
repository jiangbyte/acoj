package judge

import "hei-gin/sdk/pojo"

type JudgeConfigVO struct {
	ID    string `json:"id"`
	Key   string `json:"key"`
	Value string `json:"value"`
	Desc  string `json:"desc"`
}

type JudgeConfigUpdateParam struct {
	Key   string `json:"key" binding:"required"`
	Value string `json:"value" binding:"required"`
}

type JudgeConfigBatchUpdateParam []JudgeConfigUpdateParam

// JudgeTask 判题任务
type JudgeTask struct {
	SubmissionID    string `json:"submission_id"`
	ProblemID       string `json:"problem_id"`
	UserID          string `json:"user_id"`
	Language        string `json:"language"`
	Code            string `json:"code"`
	JudgeType       string `json:"judge_type"`        // default / spj / interactive
	ContestType     string `json:"contest_type"`      // "" / ACM / OI / IOI (竞赛模式)
	ContestID       string `json:"contest_id"`        // 竞赛ID (非空时启用竞赛模式判题)
	TimeLimit       int64  `json:"time_limit"`
	MemoryLimit     int64  `json:"memory_limit"`
	StackLimit      int64  `json:"stack_limit"`
	OutputLimit     int64  `json:"output_limit"`
	SpjCode         string `json:"spj_code"`
	SpjLanguage     string `json:"spj_language"`
	InteractiveCode string `json:"interactive_code"`
	InteractiveLang string `json:"interactive_lang"`
}

type JudgeResult struct {
	SubmissionID string           `json:"submission_id"`
	Status       string           `json:"status"`
	Score        int              `json:"score"`
	TimeUsed     int64            `json:"time_used"`
	MemoryUsed   int64            `json:"memory_used"`
	Details      []TestCaseResult `json:"details"`
	Error        string           `json:"error,omitempty"`
}

type TestCaseResult struct {
	Index      int    `json:"index"`
	Status     string `json:"status"`
	TimeUsed   int64  `json:"time_used"`
	MemoryUsed int64  `json:"memory_used"`
	Score      int    `json:"score"`
	Stderr     string `json:"stderr,omitempty"`
}

type SandboxHealthVO struct {
	BackendName string `json:"backend_name"`
	Endpoint    string `json:"endpoint"`
	Alive       bool   `json:"alive"`
	Version     string `json:"version"`
	Error       string `json:"error,omitempty"`
}

type SandboxCreateParam struct {
	Name     string `json:"name" binding:"required"`
	Endpoint string `json:"endpoint" binding:"required"`
	Timeout  int    `json:"timeout"`
}

type SandboxModifyParam struct {
	ID       string `json:"id" binding:"required"`
	Name     string `json:"name"`
	Endpoint string `json:"endpoint"`
	Timeout  *int   `json:"timeout"`
}

type SandboxRemoveParam pojo.IdsParam

package submission

// SubmissionVO is the view object for a submission.
type SubmissionVO struct {
	ID            string  `json:"id"`
	UserID        string  `json:"user_id"`
	ProblemID     string  `json:"problem_id"`
	ProblemTitle  string  `json:"problem_title,omitempty"`
	JudgeMethod   string  `json:"judge_method,omitempty"`
	ContestID     *string `json:"contest_id"`
	ContestMode   string  `json:"contest_mode"`
	IsPretest     bool    `json:"is_pretest"`
	Language      string  `json:"language"`
	Code          string  `json:"code"`
	Status        string  `json:"status"`
	Score         int     `json:"score"`
	TimeUsed      int64   `json:"time_used"`
	MemoryUsed    int64   `json:"memory_used"`
	TestcasePass  int     `json:"testcase_pass"`
	TestcaseTotal int     `json:"testcase_total"`
	ErrorInfo     string  `json:"error_info"`
	CreatedAt     *string `json:"created_at"`
	UpdatedAt     *string `json:"updated_at"`
}

// SubmissionPageParam holds pagination + filter parameters.
type SubmissionPageParam struct {
	Current   int    `json:"current" form:"current"`
	Size      int    `json:"size" form:"size"`
	ProblemID string `json:"problem_id" form:"problem_id"`
	UserID    string `json:"user_id" form:"user_id"`
	Status    string `json:"status" form:"status"`
	Language  string `json:"language" form:"language"`
	ContestID string `json:"contest_id" form:"contest_id"`
}

// SubmissionCreateParam is the request body for creating a submission.
type SubmissionCreateParam struct {
	ProblemID   string  `json:"problem_id"`
	ContestID   *string `json:"contest_id"`
	ContestMode string  `json:"contest_mode"`
	IsPretest   bool    `json:"is_pretest"`
	Language    string  `json:"language"`
	Code        string  `json:"code"`
}

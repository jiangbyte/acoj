package problemset

type ProblemSetVO struct {
	ID           string            `json:"id"`
	Title        string            `json:"title"`
	Description  string            `json:"description"`
	Type         string            `json:"type"`
	Visibility   string            `json:"visibility"`
	Status       string            `json:"status"`
	ProblemCount int               `json:"problem_count"`
	CreatedAt    *string           `json:"created_at"`
	UpdatedAt    *string           `json:"updated_at"`
	Problems     []ProblemSetItemVO `json:"problems,omitempty"`
}

type ProblemSetItemVO struct {
	ID        string `json:"id"`
	ProblemID string `json:"problem_id"`
	SortOrder int    `json:"sort_order"`
	Note      string `json:"note"`
}

type ProblemSetPageParam struct {
	Current    int    `json:"current" form:"current"`
	Size       int    `json:"size" form:"size"`
	Title      string `json:"title" form:"title"`
	Type       string `json:"type" form:"type"`
	Visibility string `json:"visibility" form:"visibility"`
}

type ProblemSetCreateParam struct {
	Title       string `json:"title"`
	Description string `json:"description"`
	Type        string `json:"type"`
	Visibility  string `json:"visibility"`
}

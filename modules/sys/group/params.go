package group

type GroupVO struct {
	ID          string   `json:"id"`
	Code        string   `json:"code"`
	Name        string   `json:"name"`
	Category    string   `json:"category"`
	ParentID    *string  `json:"parent_id"`
	OrgID       string   `json:"org_id"`
	Description *string  `json:"description"`
	Status      string   `json:"status"`
	SortCode    int      `json:"sort_code"`
	OrgNames    []string `json:"org_names"`
	Extra       *string  `json:"extra"`
	CreatedAt   string   `json:"created_at"`
	CreatedBy   *string  `json:"created_by"`
	UpdatedAt   string   `json:"updated_at"`
	UpdatedBy   *string  `json:"updated_by"`
}

type GroupPageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	Keyword  string `json:"keyword" form:"keyword"`
	Category string `json:"category" form:"category"`
	OrgID    string `json:"org_id" form:"org_id"`
}

type GroupTreeParam struct {
	Category string `json:"category" form:"category"`
	OrgID    string `json:"org_id" form:"org_id"`
}

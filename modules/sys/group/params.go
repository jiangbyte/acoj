package group

// GroupVO is the view object for a user group, used for create/modify requests and API responses.
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

// GroupPageParam holds pagination and filter parameters for the group page query.
type GroupPageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	ParentID string `json:"parent_id" form:"parent_id"`
	Keyword  string `json:"keyword" form:"keyword"`
	OrgID    string `json:"org_id" form:"org_id"`
}

// GroupTreeParam holds filter parameters for the group tree query.
type GroupTreeParam struct {
	OrgID   string `json:"org_id" form:"org_id"`
	Keyword string `json:"keyword" form:"keyword"`
}

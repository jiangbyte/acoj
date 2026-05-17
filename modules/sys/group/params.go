package group

// GroupVO is the view object for a user group, used for create/modify requests and API responses.
type GroupVO struct {
	ID          string   `json:"id,omitempty"`
	Code        string   `json:"code"`
	Name        string   `json:"name"`
	Category    string   `json:"category"`
	ParentID    *string  `json:"parent_id,omitempty"`
	OrgID       string   `json:"org_id"`
	Description *string  `json:"description,omitempty"`
	Status      string   `json:"status,omitempty"`
	SortCode    int      `json:"sort_code"`
	OrgNames    []string `json:"org_names,omitempty"`
	Extra       *string  `json:"extra,omitempty"`
	CreatedAt   string   `json:"created_at,omitempty"`
	CreatedBy   *string  `json:"created_by,omitempty"`
	UpdatedAt   string   `json:"updated_at,omitempty"`
	UpdatedBy   *string  `json:"updated_by,omitempty"`
}

// GroupPageParam holds pagination and filter parameters for the group page query.
type GroupPageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	ParentID string `json:"parent_id,omitempty" form:"parent_id"`
	Keyword  string `json:"keyword,omitempty" form:"keyword"`
	OrgID    string `json:"org_id,omitempty" form:"org_id"`
}

// GroupTreeParam holds filter parameters for the group tree query.
type GroupTreeParam struct {
	OrgID   string `json:"org_id,omitempty" form:"org_id"`
	Keyword string `json:"keyword,omitempty" form:"keyword"`
}

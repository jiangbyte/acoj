package position

type PositionVO struct {
	ID          string   `json:"id,omitempty"`
	Code        string   `json:"code"`
	Name        string   `json:"name"`
	Category    string   `json:"category"`
	OrgID       *string  `json:"org_id,omitempty"`
	GroupID     *string  `json:"group_id,omitempty"`
	Description *string  `json:"description,omitempty"`
	Status      string   `json:"status,omitempty"`
	SortCode    int      `json:"sort_code,omitempty"`
	OrgNames    []string `json:"org_names,omitempty"`
	GroupNames  []string `json:"group_names,omitempty"`
	Extra       *string  `json:"extra,omitempty"`
	CreatedAt   string   `json:"created_at,omitempty"`
	CreatedBy   *string  `json:"created_by,omitempty"`
	UpdatedAt   string   `json:"updated_at,omitempty"`
	UpdatedBy   *string  `json:"updated_by,omitempty"`
}

type PositionPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword,omitempty" form:"keyword"`
	GroupID string `json:"group_id,omitempty" form:"group_id"`
	OrgID   string `json:"org_id,omitempty" form:"org_id"`
}

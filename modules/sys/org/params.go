package org

type OrgVO struct {
	ID          string  `json:"id,omitempty"`
	Code        string  `json:"code"`
	Name        string  `json:"name"`
	Category    string  `json:"category"`
	ParentID    *string `json:"parent_id,omitempty"`
	Description *string `json:"description,omitempty"`
	Status      string  `json:"status,omitempty"`
	SortCode    int     `json:"sort_code,omitempty"`
	Extra       *string `json:"extra,omitempty"`
	CreatedAt   string  `json:"created_at,omitempty"`
	CreatedBy   *string `json:"created_by,omitempty"`
	UpdatedAt   string  `json:"updated_at,omitempty"`
	UpdatedBy   *string `json:"updated_by,omitempty"`
}

type OrgPageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	ParentID string `json:"parent_id,omitempty" form:"parent_id"`
	Keyword  string `json:"keyword,omitempty" form:"keyword"`
}

type OrgTreeParam struct {
	Category string `json:"category,omitempty" form:"category"`
}

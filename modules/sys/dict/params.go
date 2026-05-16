package dict

// DictVO is the view object for a dictionary entry, used for create/modify requests and API responses.
type DictVO struct {
	ID        string  `json:"id,omitempty"`
	Code      string  `json:"code"`
	Label     *string `json:"label,omitempty"`
	Value     *string `json:"value,omitempty"`
	Color     *string `json:"color,omitempty"`
	Category  *string `json:"category,omitempty"`
	ParentID  *string `json:"parent_id,omitempty"`
	Status    string  `json:"status,omitempty"`
	SortCode  int     `json:"sort_code"`
	CreatedAt string  `json:"created_at,omitempty"`
	CreatedBy *string `json:"created_by,omitempty"`
	UpdatedAt string  `json:"updated_at,omitempty"`
	UpdatedBy *string `json:"updated_by,omitempty"`
}

// DictPageParam holds pagination and filter parameters for the dict page query.
type DictPageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	ParentID string `json:"parent_id,omitempty" form:"parent_id"`
	Category string `json:"category,omitempty" form:"category"`
	Keyword  string `json:"keyword,omitempty" form:"keyword"`
}

// DictListParam holds filter parameters for the dict list query.
type DictListParam struct {
	ParentID string `json:"parent_id,omitempty" form:"parent_id"`
	Category string `json:"category,omitempty" form:"category"`
}

// DictTreeParam holds filter parameters for the dict tree query.
type DictTreeParam struct {
	Category string `json:"category,omitempty" form:"category"`
}

package dict

// DictVO is the view object for a dictionary entry, used for create/modify requests and API responses.
type DictVO struct {
	ID        string  `json:"id"`
	Code      string  `json:"code"`
	Label     *string `json:"label"`
	Value     *string `json:"value"`
	Color     *string `json:"color"`
	Category  *string `json:"category"`
	ParentID  *string `json:"parent_id"`
	Status    string  `json:"status"`
	SortCode  int     `json:"sort_code"`
	CreatedAt string  `json:"created_at"`
	CreatedBy *string `json:"created_by"`
	UpdatedAt string  `json:"updated_at"`
	UpdatedBy *string `json:"updated_by"`
}

// DictPageParam holds pagination and filter parameters for the dict page query.
type DictPageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	ParentID string `json:"parent_id" form:"parent_id"`
	Category string `json:"category" form:"category"`
	Keyword  string `json:"keyword" form:"keyword"`
}

// DictListParam holds filter parameters for the dict list query.
type DictListParam struct {
	ParentID string `json:"parent_id" form:"parent_id"`
	Category string `json:"category" form:"category"`
}

// DictTreeParam holds filter parameters for the dict tree query.
type DictTreeParam struct {
	Category string `json:"category" form:"category"`
}

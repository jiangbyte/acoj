package dict

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

type DictPageParam struct {
	DictGroup string `json:"dict_group" form:"dict_group"`
	ParentID string `json:"parent_id" form:"parent_id"`
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	Category string `json:"category" form:"category"`
	Keyword  string `json:"keyword" form:"keyword"`
}

type DictTreeParam struct {
	Category string `json:"category" form:"category"`
	DictGroup string `json:"dict_group" form:"dict_group"`
}

type DictOptionsParam struct {
	Category string `json:"category" form:"category"`
	ParentID string `json:"parent_id" form:"parent_id"`
}

type DictListParam struct {
	Category string `json:"category" form:"category"`
	Keyword  string `json:"keyword" form:"keyword"`
}

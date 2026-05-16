package notice

type NoticeVO struct {
	ID        string  `json:"id,omitempty"`
	Title     string  `json:"title"`
	Category  string  `json:"category"`
	Type      string  `json:"type"`
	Summary   *string `json:"summary,omitempty"`
	Content   *string `json:"content,omitempty"`
	Cover     *string `json:"cover,omitempty"`
	Level     string  `json:"level,omitempty"`
	ViewCount int     `json:"view_count,omitempty"`
	IsTop     string  `json:"is_top,omitempty"`
	Position  *string `json:"position,omitempty"`
	Status    string  `json:"status,omitempty"`
	SortCode  int     `json:"sort_code,omitempty"`
	CreatedAt string  `json:"created_at,omitempty"`
	CreatedBy *string `json:"created_by,omitempty"`
	UpdatedAt string  `json:"updated_at,omitempty"`
	UpdatedBy *string `json:"updated_by,omitempty"`
}

type NoticePageParam struct {
	Current int `json:"current" form:"current"`
	Size    int `json:"size" form:"size"`
}

package notice

type NoticeVO struct {
	ID        string  `json:"id"`
	Title     string  `json:"title"`
	Category  string  `json:"category"`
	Type      string  `json:"type"`
	Summary   *string `json:"summary"`
	Content   *string `json:"content"`
	Cover     *string `json:"cover"`
	Level     string  `json:"level"`
	ViewCount int     `json:"view_count"`
	IsTop     string  `json:"is_top"`
	Position  *string `json:"position"`
	Status    string  `json:"status"`
	SortCode  int     `json:"sort_code"`
	CreatedAt string  `json:"created_at"`
	CreatedBy *string `json:"created_by"`
	UpdatedAt string  `json:"updated_at"`
	UpdatedBy *string `json:"updated_by"`
}

type NoticePageParam struct {
	Current int `json:"current" form:"current"`
	Size    int `json:"size" form:"size"`
}

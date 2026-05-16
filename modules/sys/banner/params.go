package banner

type BannerVO struct {
	ID          string  `json:"id"`
	Title       string  `json:"title"`
	Image       string  `json:"image"`
	URL         *string `json:"url"`
	LinkType    string  `json:"link_type"`
	Summary     *string `json:"summary"`
	Description *string `json:"description"`
	Category    string  `json:"category"`
	Type        string  `json:"type"`
	Position    string  `json:"position"`
	SortCode    int     `json:"sort_code"`
	ViewCount   int     `json:"view_count"`
	ClickCount  int     `json:"click_count"`
}

type BannerPageParam struct {
	Current int `json:"current" form:"current"`
	Size    int `json:"size" form:"size"`
}

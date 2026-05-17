package banner

// BannerVO is the view object for a banner, used for create/modify requests and API responses.
type BannerVO struct {
	ID          string  `json:"id,omitempty"`
	Title       string  `json:"title,omitempty"`
	Image       string  `json:"image,omitempty"`
	URL         *string `json:"url,omitempty"`
	LinkType    string  `json:"link_type,omitempty"`
	Summary     *string `json:"summary,omitempty"`
	Description *string `json:"description,omitempty"`
	Category    string  `json:"category,omitempty"`
	Type        string  `json:"type,omitempty"`
	Position    string  `json:"position,omitempty"`
	SortCode    int     `json:"sort_code,omitempty"`
	ViewCount   int     `json:"view_count,omitempty"`
	ClickCount  int     `json:"click_count,omitempty"`
	CreatedAt   *string `json:"created_at,omitempty"`
	CreatedBy   *string `json:"created_by,omitempty"`
	UpdatedAt   *string `json:"updated_at,omitempty"`
	UpdatedBy   *string `json:"updated_by,omitempty"`
}

// BannerPageParam holds pagination parameters for the banner page query.
type BannerPageParam struct {
	Current int `json:"current" form:"current"`
	Size    int `json:"size" form:"size"`
}

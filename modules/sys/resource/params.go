package resource

import "time"

// ModuleVO represents a system module view object.
type ModuleVO struct {
	ID          string     `json:"id"`
	Code        string     `json:"code"`
	Name        string     `json:"name"`
	Category    string     `json:"category"`
	Icon        *string    `json:"icon,omitempty"`
	Color       *string    `json:"color,omitempty"`
	Description *string    `json:"description,omitempty"`
	IsVisible   string     `json:"is_visible"`
	Status      string     `json:"status"`
	SortCode    int        `json:"sort_code"`
	CreatedAt   *time.Time `json:"created_at,omitempty"`
	CreatedBy   *string    `json:"created_by,omitempty"`
	UpdatedAt   *time.Time `json:"updated_at,omitempty"`
	UpdatedBy   *string    `json:"updated_by,omitempty"`
}

// ModulePageParam holds pagination parameters for module page queries.
type ModulePageParam struct {
	Current int `json:"current" form:"current"`
	Size    int `json:"size" form:"size"`
}

// ResourceVO represents a system resource view object with optional children for tree rendering.
type ResourceVO struct {
	ID            string        `json:"id"`
	Code          string        `json:"code"`
	Name          string        `json:"name"`
	Category      string        `json:"category"`
	Type          string        `json:"type"`
	Description   *string       `json:"description,omitempty"`
	ParentID      *string       `json:"parent_id,omitempty"`
	RoutePath     *string       `json:"route_path,omitempty"`
	ComponentPath *string       `json:"component_path,omitempty"`
	RedirectPath  *string       `json:"redirect_path,omitempty"`
	Icon          *string       `json:"icon,omitempty"`
	Color         *string       `json:"color,omitempty"`
	IsVisible     string        `json:"is_visible"`
	IsCache       string        `json:"is_cache"`
	IsAffix       string        `json:"is_affix"`
	IsBreadcrumb  string        `json:"is_breadcrumb"`
	ExternalURL   *string       `json:"external_url,omitempty"`
	Extra         *string       `json:"extra,omitempty"`
	Status        string        `json:"status"`
	SortCode      int           `json:"sort_code"`
	CreatedAt     *time.Time    `json:"created_at,omitempty"`
	CreatedBy     *string       `json:"created_by,omitempty"`
	UpdatedAt     *time.Time    `json:"updated_at,omitempty"`
	UpdatedBy     *string       `json:"updated_by,omitempty"`
	Children      []*ResourceVO `json:"children,omitempty"`
}

// ResourcePageParam holds pagination parameters for resource page queries.
type ResourcePageParam struct {
	Current int `json:"current" form:"current"`
	Size    int `json:"size" form:"size"`
}

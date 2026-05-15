package v1

import (
	"github.com/gogf/gf/v2/frame/g"
)

type ResourceTreeReq struct {
	g.Meta `path:"/api/v1/sys/resource/tree" method:"get" summary:"获取资源树" tags:"资源管理"`
}

type ResourceTreeNode struct {
	Id            string              `json:"id"`
	Code          string              `json:"code"`
	Name          string              `json:"name"`
	Category      string              `json:"category"`
	Type          string              `json:"type"`
	Description   string              `json:"description"`
	ParentId      string              `json:"parent_id"`
	RoutePath     string              `json:"route_path"`
	ComponentPath string              `json:"component_path"`
	RedirectPath  string              `json:"redirect_path"`
	Icon          string              `json:"icon"`
	Color         string              `json:"color"`
	IsVisible     string              `json:"is_visible"`
	IsCache       string              `json:"is_cache"`
	IsAffix       string              `json:"is_affix"`
	IsBreadcrumb  string              `json:"is_breadcrumb"`
	ExternalUrl   string              `json:"external_url"`
	Extra         string              `json:"extra"`
	Status        string              `json:"status"`
	SortCode      int                 `json:"sort_code"`
	Children      []*ResourceTreeNode `json:"children"`
}

type ResourceTreeRes struct {
	List []*ResourceTreeNode `json:"list"`
}

type ResourceDetailReq struct {
	g.Meta `path:"/api/v1/sys/resource/detail" method:"get" summary:"获取资源详情" tags:"资源管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type ResourceDetailRes struct {
	Id            string `json:"id"`
	Code          string `json:"code"`
	Name          string `json:"name"`
	Category      string `json:"category"`
	Type          string `json:"type"`
	Description   string `json:"description"`
	ParentId      string `json:"parent_id"`
	RoutePath     string `json:"route_path"`
	ComponentPath string `json:"component_path"`
	RedirectPath  string `json:"redirect_path"`
	Icon          string `json:"icon"`
	Color         string `json:"color"`
	IsVisible     string `json:"is_visible"`
	IsCache       string `json:"is_cache"`
	IsAffix       string `json:"is_affix"`
	IsBreadcrumb  string `json:"is_breadcrumb"`
	ExternalUrl   string `json:"external_url"`
	Extra         string `json:"extra"`
	Status        string `json:"status"`
	SortCode      int    `json:"sort_code"`
	CreatedAt     string `json:"created_at"`
	CreatedBy     string `json:"created_by"`
	UpdatedAt     string `json:"updated_at"`
	UpdatedBy     string `json:"updated_by"`
}

type ResourceCreateReq struct {
	g.Meta        `path:"/api/v1/sys/resource/create" method:"post" summary:"添加资源" tags:"资源管理"`
	Code          string `json:"code" v:"required#编码不能为空"`
	Name          string `json:"name" v:"required#名称不能为空"`
	Category      string `json:"category" v:"required#分类不能为空"`
	Type          string `json:"type" v:"required#类型不能为空"`
	Description   string `json:"description"`
	ParentId      string `json:"parent_id"`
	RoutePath     string `json:"route_path"`
	ComponentPath string `json:"component_path"`
	RedirectPath  string `json:"redirect_path"`
	Icon          string `json:"icon"`
	Color         string `json:"color"`
	IsVisible     string `json:"is_visible"`
	IsCache       string `json:"is_cache"`
	IsAffix       string `json:"is_affix"`
	IsBreadcrumb  string `json:"is_breadcrumb"`
	ExternalUrl   string `json:"external_url"`
	Extra         string `json:"extra"`
	Status        string `json:"status"`
	SortCode      int    `json:"sort_code"`
}

type ResourceCreateRes struct{}

type ResourceModifyReq struct {
	g.Meta        `path:"/api/v1/sys/resource/modify" method:"post" summary:"编辑资源" tags:"资源管理"`
	Id            string `json:"id" v:"required#ID不能为空"`
	Code          string `json:"code"`
	Name          string `json:"name"`
	Category      string `json:"category"`
	Type          string `json:"type"`
	Description   string `json:"description"`
	ParentId      string `json:"parent_id"`
	RoutePath     string `json:"route_path"`
	ComponentPath string `json:"component_path"`
	RedirectPath  string `json:"redirect_path"`
	Icon          string `json:"icon"`
	Color         string `json:"color"`
	IsVisible     string `json:"is_visible"`
	IsCache       string `json:"is_cache"`
	IsAffix       string `json:"is_affix"`
	IsBreadcrumb  string `json:"is_breadcrumb"`
	ExternalUrl   string `json:"external_url"`
	Extra         string `json:"extra"`
	Status        string `json:"status"`
	SortCode      int    `json:"sort_code"`
}

type ResourceModifyRes struct{}

type ResourceRemoveReq struct {
	g.Meta `path:"/api/v1/sys/resource/remove" method:"post" summary:"删除资源" tags:"资源管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type ResourceRemoveRes struct{}

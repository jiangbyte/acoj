package v1

import (
	"github.com/gogf/gf/v2/frame/g"
)

type GetHomeReq struct {
	g.Meta `path:"/api/v1/sys/home" method:"get" summary:"获取首页数据" tags:"首页管理"`
}

type GetHomeRes struct {
	QuickActions      []QuickActionItem `json:"quick_actions"`
	AvailableResource []ResourceItem    `json:"available_resources"`
	NoticeCount       int               `json:"notice_count"`
	Stats             HomeStats         `json:"stats"`
}

type QuickActionItem struct {
	Id        string `json:"id"`
	SortCode  int    `json:"sort_code"`
	Name      string `json:"name"`
	Icon      string `json:"icon"`
	RoutePath string `json:"route_path"`
}

type ResourceItem struct {
	Id        string `json:"id"`
	Code      string `json:"code"`
	Name      string `json:"name"`
	Icon      string `json:"icon"`
	RoutePath string `json:"route_path"`
	Category  string `json:"category"`
	Type      string `json:"type"`
}

type HomeStats struct {
	TotalUsers int `json:"total_users"`
}

type AddQuickActionReq struct {
	g.Meta     `path:"/api/v1/sys/home/quick-actions/add" method:"post" summary:"添加快捷操作" tags:"首页管理"`
	ResourceId string `json:"resource_id" v:"required#资源ID不能为空"`
}

type AddQuickActionRes struct{}

type RemoveQuickActionReq struct {
	g.Meta `path:"/api/v1/sys/home/quick-actions/remove" method:"post" summary:"删除快捷操作" tags:"首页管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type RemoveQuickActionRes struct{}

type SortQuickActionReq struct {
	g.Meta `path:"/api/v1/sys/home/quick-actions/sort" method:"post" summary:"排序快捷操作" tags:"首页管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type SortQuickActionRes struct{}

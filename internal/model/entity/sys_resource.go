package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysResource struct {
	gmeta.Meta    `orm:"table:sys_resource"`
	Id            string      `json:"id"           description:"主键"`
	Code          string      `json:"code"         description:"资源编码"`
	Name          string      `json:"name"         description:"资源名称"`
	Category      string      `json:"category"     description:"资源分类"`
	Type          string      `json:"type"         description:"资源类型"`
	Description   string      `json:"description"  description:"资源描述"`
	ParentId      string      `json:"parentId"     description:"父资源ID"`
	RoutePath     string      `json:"routePath"    description:"路由路径"`
	ComponentPath string      `json:"componentPath" description:"组件路径"`
	RedirectPath  string      `json:"redirectPath" description:"重定向路径"`
	Icon          string      `json:"icon"         description:"资源图标"`
	Color         string      `json:"color"        description:"资源颜色"`
	IsVisible     string      `json:"isVisible"    description:"是否可见"`
	IsCache       string      `json:"isCache"      description:"是否缓存"`
	IsAffix       string      `json:"isAffix"      description:"是否固定"`
	IsBreadcrumb  string      `json:"isBreadcrumb" description:"是否显示面包屑"`
	ExternalUrl   string      `json:"externalUrl"  description:"外链地址"`
	Extra         string      `json:"extra"        description:"扩展信息"`
	Status        string      `json:"status"       description:"状态"`
	SortCode      int         `json:"sortCode"     description:"排序"`
	CreatedAt     *gtime.Time `json:"createdAt"    description:"创建时间"`
	CreatedBy     string      `json:"createdBy"    description:"创建用户"`
	UpdatedAt     *gtime.Time `json:"updatedAt"    description:"更新时间"`
	UpdatedBy     string      `json:"updatedBy"    description:"更新用户"`
}

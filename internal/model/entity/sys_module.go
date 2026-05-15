package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysModule struct {
	gmeta.Meta  `orm:"table:sys_module"`
	Id          string      `json:"id"          description:"主键"`
	Code        string      `json:"code"        description:"模块编码"`
	Name        string      `json:"name"        description:"模块名称"`
	Category    string      `json:"category"    description:"模块类别"`
	Icon        string      `json:"icon"        description:"模块图标"`
	Color       string      `json:"color"       description:"模块颜色"`
	Description string      `json:"description" description:"模块描述"`
	IsVisible   string      `json:"isVisible"   description:"是否可见"`
	Status      string      `json:"status"      description:"状态"`
	SortCode    int         `json:"sortCode"    description:"排序"`
	CreatedAt   *gtime.Time `json:"createdAt"   description:"创建时间"`
	CreatedBy   string      `json:"createdBy"   description:"创建用户"`
	UpdatedAt   *gtime.Time `json:"updatedAt"   description:"更新时间"`
	UpdatedBy   string      `json:"updatedBy"   description:"更新用户"`
}

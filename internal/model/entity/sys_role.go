package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysRole struct {
	gmeta.Meta  `orm:"table:sys_role"`
	Id          string      `json:"id"          description:"主键"`
	Code        string      `json:"code"        description:"角色编码"`
	Name        string      `json:"name"        description:"角色名称"`
	Category    string      `json:"category"    description:"角色类别"`
	Description string      `json:"description" description:"角色描述"`
	Status      string      `json:"status"      description:"状态"`
	SortCode    int         `json:"sortCode"    description:"排序"`
	Extra       string      `json:"extra"       description:"扩展信息"`
	CreatedAt   *gtime.Time `json:"createdAt"   description:"创建时间"`
	CreatedBy   string      `json:"createdBy"   description:"创建用户"`
	UpdatedAt   *gtime.Time `json:"updatedAt"   description:"更新时间"`
	UpdatedBy   string      `json:"updatedBy"   description:"更新用户"`
}

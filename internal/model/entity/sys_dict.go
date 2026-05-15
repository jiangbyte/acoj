package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysDict struct {
	gmeta.Meta `orm:"table:sys_dict"`
	Id         string      `json:"id"          description:"主键"`
	Code       string      `json:"code"        description:"字典编码"`
	Label      string      `json:"label"       description:"字典标签"`
	Value      string      `json:"value"       description:"字典值"`
	Color      string      `json:"color"       description:"字典颜色"`
	Category   string      `json:"category"    description:"字典分类"`
	ParentId   string      `json:"parentId"    description:"父字典ID"`
	Status     string      `json:"status"      description:"状态"`
	SortCode   int         `json:"sortCode"    description:"排序"`
	CreatedAt  *gtime.Time `json:"createdAt"   description:"创建时间"`
	CreatedBy  string      `json:"createdBy"   description:"创建用户"`
	UpdatedAt  *gtime.Time `json:"updatedAt"   description:"更新时间"`
	UpdatedBy  string      `json:"updatedBy"   description:"更新用户"`
}

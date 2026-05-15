package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysQuickAction struct {
	gmeta.Meta `orm:"table:sys_quick_action"`
	Id         string      `json:"id"          description:"主键"`
	UserId     string      `json:"userId"      description:"用户ID"`
	ResourceId string      `json:"resourceId"  description:"资源ID"`
	SortCode   int         `json:"sortCode"    description:"排序"`
	CreatedAt  *gtime.Time `json:"createdAt"   description:"创建时间"`
	CreatedBy  string      `json:"createdBy"   description:"创建用户"`
	UpdatedAt  *gtime.Time `json:"updatedAt"   description:"更新时间"`
	UpdatedBy  string      `json:"updatedBy"   description:"更新用户"`
}

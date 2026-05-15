package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysQuickAction = sysQuickActionDao{}

type sysQuickActionDao struct {
	Table   string
	Columns sysQuickActionColumns
}

type sysQuickActionColumns struct {
	Id         string
	UserId     string
	ResourceId string
	SortCode   string
	CreatedAt  string
	CreatedBy  string
	UpdatedAt  string
	UpdatedBy  string
}

func init() {
	SysQuickAction.Table = "sys_quick_action"
	SysQuickAction.Columns = sysQuickActionColumns{
		Id:         "id",
		UserId:     "user_id",
		ResourceId: "resource_id",
		SortCode:   "sort_code",
		CreatedAt:  "created_at",
		CreatedBy:  "created_by",
		UpdatedAt:  "updated_at",
		UpdatedBy:  "updated_by",
	}
}

func (d sysQuickActionDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysGroup = sysGroupDao{}

type sysGroupDao struct {
	Table   string
	Columns sysGroupColumns
}

type sysGroupColumns struct {
	Id          string
	Code        string
	Name        string
	Category    string
	ParentId    string
	OrgId       string
	Description string
	Status      string
	SortCode    string
	Extra       string
	CreatedAt   string
	CreatedBy   string
	UpdatedAt   string
	UpdatedBy   string
}

func init() {
	SysGroup.Table = "sys_group"
	SysGroup.Columns = sysGroupColumns{
		Id:          "id",
		Code:        "code",
		Name:        "name",
		Category:    "category",
		ParentId:    "parent_id",
		OrgId:       "org_id",
		Description: "description",
		Status:      "status",
		SortCode:    "sort_code",
		Extra:       "extra",
		CreatedAt:   "created_at",
		CreatedBy:   "created_by",
		UpdatedAt:   "updated_at",
		UpdatedBy:   "updated_by",
	}
}

func (d sysGroupDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

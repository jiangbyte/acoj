package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysRole = sysRoleDao{}

type sysRoleDao struct {
	Table   string
	Columns sysRoleColumns
}

type sysRoleColumns struct {
	Id          string
	Code        string
	Name        string
	Category    string
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
	SysRole.Table = "sys_role"
	SysRole.Columns = sysRoleColumns{
		Id:          "id",
		Code:        "code",
		Name:        "name",
		Category:    "category",
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

func (d sysRoleDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

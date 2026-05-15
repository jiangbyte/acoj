package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysOrg = sysOrgDao{}

type sysOrgDao struct {
	Table   string
	Columns sysOrgColumns
}

type sysOrgColumns struct {
	Id          string
	Code        string
	Name        string
	Category    string
	ParentId    string
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
	SysOrg.Table = "sys_org"
	SysOrg.Columns = sysOrgColumns{
		Id:          "id",
		Code:        "code",
		Name:        "name",
		Category:    "category",
		ParentId:    "parent_id",
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

func (d sysOrgDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysPosition = sysPositionDao{}

type sysPositionDao struct {
	Table   string
	Columns sysPositionColumns
}

type sysPositionColumns struct {
	Id          string
	Code        string
	Name        string
	Category    string
	OrgId       string
	GroupId     string
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
	SysPosition.Table = "sys_position"
	SysPosition.Columns = sysPositionColumns{
		Id:          "id",
		Code:        "code",
		Name:        "name",
		Category:    "category",
		OrgId:       "org_id",
		GroupId:     "group_id",
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

func (d sysPositionDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

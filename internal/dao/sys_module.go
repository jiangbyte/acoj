package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysModule = sysModuleDao{}

type sysModuleDao struct {
	Table   string
	Columns sysModuleColumns
}

type sysModuleColumns struct {
	Id          string
	Code        string
	Name        string
	Category    string
	Icon        string
	Color       string
	Description string
	IsVisible   string
	Status      string
	SortCode    string
	CreatedAt   string
	CreatedBy   string
	UpdatedAt   string
	UpdatedBy   string
}

func init() {
	SysModule.Table = "sys_module"
	SysModule.Columns = sysModuleColumns{
		Id:          "id",
		Code:        "code",
		Name:        "name",
		Category:    "category",
		Icon:        "icon",
		Color:       "color",
		Description: "description",
		IsVisible:   "is_visible",
		Status:      "status",
		SortCode:    "sort_code",
		CreatedAt:   "created_at",
		CreatedBy:   "created_by",
		UpdatedAt:   "updated_at",
		UpdatedBy:   "updated_by",
	}
}

func (d sysModuleDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

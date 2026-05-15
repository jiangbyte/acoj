package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysDict = sysDictDao{}

type sysDictDao struct {
	Table   string
	Columns sysDictColumns
}

type sysDictColumns struct {
	Id        string
	Code      string
	Label     string
	Value     string
	Color     string
	Category  string
	ParentId  string
	Status    string
	SortCode  string
	CreatedAt string
	CreatedBy string
	UpdatedAt string
	UpdatedBy string
}

func init() {
	SysDict.Table = "sys_dict"
	SysDict.Columns = sysDictColumns{
		Id:        "id",
		Code:      "code",
		Label:     "label",
		Value:     "value",
		Color:     "color",
		Category:  "category",
		ParentId:  "parent_id",
		Status:    "status",
		SortCode:  "sort_code",
		CreatedAt: "created_at",
		CreatedBy: "created_by",
		UpdatedAt: "updated_at",
		UpdatedBy: "updated_by",
	}
}

func (d sysDictDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

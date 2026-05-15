package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysConfig = sysConfigDao{}

type sysConfigDao struct {
	Table   string
	Columns sysConfigColumns
}

type sysConfigColumns struct {
	Id          string
	ConfigKey   string
	ConfigValue string
	Category    string
	Remark      string
	SortCode    string
	Extra       string
	CreatedAt   string
	CreatedBy   string
	UpdatedAt   string
	UpdatedBy   string
}

func init() {
	SysConfig.Table = "sys_config"
	SysConfig.Columns = sysConfigColumns{
		Id:          "id",
		ConfigKey:   "config_key",
		ConfigValue: "config_value",
		Category:    "category",
		Remark:      "remark",
		SortCode:    "sort_code",
		Extra:       "extra",
		CreatedAt:   "created_at",
		CreatedBy:   "created_by",
		UpdatedAt:   "updated_at",
		UpdatedBy:   "updated_by",
	}
}

func (d sysConfigDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

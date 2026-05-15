package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysConfig struct {
	gmeta.Meta  `orm:"table:sys_config"`
	Id          string      `json:"id"          description:"主键"`
	ConfigKey   string      `json:"configKey"   description:"配置键"`
	ConfigValue string      `json:"configValue" description:"配置值"`
	Category    string      `json:"category"    description:"分类"`
	Remark      string      `json:"remark"      description:"备注"`
	SortCode    int         `json:"sortCode"    description:"排序码"`
	Extra       string      `json:"extra"       description:"扩展信息"`
	CreatedAt   *gtime.Time `json:"createdAt"   description:"创建时间"`
	CreatedBy   string      `json:"createdBy"   description:"创建用户"`
	UpdatedAt   *gtime.Time `json:"updatedAt"   description:"修改时间"`
	UpdatedBy   string      `json:"updatedBy"   description:"修改用户"`
}

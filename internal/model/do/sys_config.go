package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysConfig struct {
	gmeta.Meta  `orm:"table:sys_config"`
	Id          interface{} `json:"id"`
	ConfigKey   interface{} `json:"configKey"`
	ConfigValue interface{} `json:"configValue"`
	Category    interface{} `json:"category"`
	Remark      interface{} `json:"remark"`
	SortCode    interface{} `json:"sortCode"`
	Extra       interface{} `json:"extra"`
	CreatedAt   interface{} `json:"createdAt"`
	CreatedBy   interface{} `json:"createdBy"`
	UpdatedAt   interface{} `json:"updatedAt"`
	UpdatedBy   interface{} `json:"updatedBy"`
}

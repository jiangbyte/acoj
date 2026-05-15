package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysQuickAction struct {
	gmeta.Meta `orm:"table:sys_quick_action"`
	Id         interface{} `json:"id"`
	UserId     interface{} `json:"userId"`
	ResourceId interface{} `json:"resourceId"`
	SortCode   interface{} `json:"sortCode"`
	CreatedAt  interface{} `json:"createdAt"`
	CreatedBy  interface{} `json:"createdBy"`
	UpdatedAt  interface{} `json:"updatedAt"`
	UpdatedBy  interface{} `json:"updatedBy"`
}

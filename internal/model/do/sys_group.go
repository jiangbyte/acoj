package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysGroup struct {
	gmeta.Meta  `orm:"table:sys_group"`
	Id          interface{} `json:"id"`
	Code        interface{} `json:"code"`
	Name        interface{} `json:"name"`
	Category    interface{} `json:"category"`
	ParentId    interface{} `json:"parentId"`
	OrgId       interface{} `json:"orgId"`
	Description interface{} `json:"description"`
	Status      interface{} `json:"status"`
	SortCode    interface{} `json:"sortCode"`
	Extra       interface{} `json:"extra"`
	CreatedAt   interface{} `json:"createdAt"`
	CreatedBy   interface{} `json:"createdBy"`
	UpdatedAt   interface{} `json:"updatedAt"`
	UpdatedBy   interface{} `json:"updatedBy"`
}

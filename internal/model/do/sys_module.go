package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysModule struct {
	gmeta.Meta  `orm:"table:sys_module"`
	Id          interface{} `json:"id"`
	Code        interface{} `json:"code"`
	Name        interface{} `json:"name"`
	Category    interface{} `json:"category"`
	Icon        interface{} `json:"icon"`
	Color       interface{} `json:"color"`
	Description interface{} `json:"description"`
	IsVisible   interface{} `json:"isVisible"`
	Status      interface{} `json:"status"`
	SortCode    interface{} `json:"sortCode"`
	CreatedAt   interface{} `json:"createdAt"`
	CreatedBy   interface{} `json:"createdBy"`
	UpdatedAt   interface{} `json:"updatedAt"`
	UpdatedBy   interface{} `json:"updatedBy"`
}

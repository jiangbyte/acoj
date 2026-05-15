package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysDict struct {
	gmeta.Meta `orm:"table:sys_dict"`
	Id         interface{} `json:"id"`
	Code       interface{} `json:"code"`
	Label      interface{} `json:"label"`
	Value      interface{} `json:"value"`
	Color      interface{} `json:"color"`
	Category   interface{} `json:"category"`
	ParentId   interface{} `json:"parentId"`
	Status     interface{} `json:"status"`
	SortCode   interface{} `json:"sortCode"`
	CreatedAt  interface{} `json:"createdAt"`
	CreatedBy  interface{} `json:"createdBy"`
	UpdatedAt  interface{} `json:"updatedAt"`
	UpdatedBy  interface{} `json:"updatedBy"`
}

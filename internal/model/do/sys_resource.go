package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysResource struct {
	gmeta.Meta    `orm:"table:sys_resource"`
	Id            interface{} `json:"id"`
	Code          interface{} `json:"code"`
	Name          interface{} `json:"name"`
	Category      interface{} `json:"category"`
	Type          interface{} `json:"type"`
	Description   interface{} `json:"description"`
	ParentId      interface{} `json:"parentId"`
	RoutePath     interface{} `json:"routePath"`
	ComponentPath interface{} `json:"componentPath"`
	RedirectPath  interface{} `json:"redirectPath"`
	Icon          interface{} `json:"icon"`
	Color         interface{} `json:"color"`
	IsVisible     interface{} `json:"isVisible"`
	IsCache       interface{} `json:"isCache"`
	IsAffix       interface{} `json:"isAffix"`
	IsBreadcrumb  interface{} `json:"isBreadcrumb"`
	ExternalUrl   interface{} `json:"externalUrl"`
	Extra         interface{} `json:"extra"`
	Status        interface{} `json:"status"`
	SortCode      interface{} `json:"sortCode"`
	CreatedAt     interface{} `json:"createdAt"`
	CreatedBy     interface{} `json:"createdBy"`
	UpdatedAt     interface{} `json:"updatedAt"`
	UpdatedBy     interface{} `json:"updatedBy"`
}

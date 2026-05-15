package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysBanner struct {
	gmeta.Meta  `orm:"table:sys_banner"`
	Id          interface{} `json:"id"`
	Title       interface{} `json:"title"`
	Image       interface{} `json:"image"`
	Category    interface{} `json:"category"`
	Type        interface{} `json:"type"`
	Position    interface{} `json:"position"`
	Url         interface{} `json:"url"`
	LinkType    interface{} `json:"linkType"`
	Summary     interface{} `json:"summary"`
	Description interface{} `json:"description"`
	SortCode    interface{} `json:"sortCode"`
	ViewCount   interface{} `json:"viewCount"`
	ClickCount  interface{} `json:"clickCount"`
	CreatedAt   interface{} `json:"createdAt"`
	CreatedBy   interface{} `json:"createdBy"`
	UpdatedAt   interface{} `json:"updatedAt"`
	UpdatedBy   interface{} `json:"updatedBy"`
}

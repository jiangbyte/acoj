package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysNotice struct {
	gmeta.Meta `orm:"table:sys_notice"`
	Id         interface{} `json:"id"`
	Title      interface{} `json:"title"`
	Category   interface{} `json:"category"`
	Type       interface{} `json:"type"`
	Summary    interface{} `json:"summary"`
	Content    interface{} `json:"content"`
	Cover      interface{} `json:"cover"`
	Level      interface{} `json:"level"`
	ViewCount  interface{} `json:"viewCount"`
	IsTop      interface{} `json:"isTop"`
	Position   interface{} `json:"position"`
	Status     interface{} `json:"status"`
	SortCode   interface{} `json:"sortCode"`
	CreatedAt  interface{} `json:"createdAt"`
	CreatedBy  interface{} `json:"createdBy"`
	UpdatedAt  interface{} `json:"updatedAt"`
	UpdatedBy  interface{} `json:"updatedBy"`
}

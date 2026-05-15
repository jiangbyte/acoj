package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysNotice struct {
	gmeta.Meta `orm:"table:sys_notice"`
	Id         string      `json:"id"          description:"主键"`
	Title      string      `json:"title"       description:"通知标题"`
	Category   string      `json:"category"    description:"通知类别"`
	Type       string      `json:"type"        description:"通知类型"`
	Summary    string      `json:"summary"     description:"通知摘要"`
	Content    string      `json:"content"     description:"通知内容"`
	Cover      string      `json:"cover"       description:"封面图片"`
	Level      string      `json:"level"       description:"通知级别"`
	ViewCount  int         `json:"viewCount"   description:"浏览次数"`
	IsTop      string      `json:"isTop"       description:"是否置顶"`
	Position   string      `json:"position"    description:"通知位置"`
	Status     string      `json:"status"      description:"状态"`
	SortCode   int         `json:"sortCode"    description:"排序"`
	CreatedAt  *gtime.Time `json:"createdAt"   description:"创建时间"`
	CreatedBy  string      `json:"createdBy"   description:"创建用户"`
	UpdatedAt  *gtime.Time `json:"updatedAt"   description:"更新时间"`
	UpdatedBy  string      `json:"updatedBy"   description:"更新用户"`
}

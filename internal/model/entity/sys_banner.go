package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysBanner struct {
	gmeta.Meta  `orm:"table:sys_banner"`
	Id          string      `json:"id"          description:"主键"`
	Title       string      `json:"title"       description:"轮播标题"`
	Image       string      `json:"image"       description:"轮播图片"`
	Category    string      `json:"category"    description:"轮播类别"`
	Type        string      `json:"type"        description:"轮播类型"`
	Position    string      `json:"position"    description:"展示位置"`
	Url         string      `json:"url"         description:"跳转地址"`
	LinkType    string      `json:"linkType"    description:"链接类型"`
	Summary     string      `json:"summary"     description:"轮播摘要"`
	Description string      `json:"description" description:"轮播描述"`
	SortCode    int         `json:"sortCode"    description:"排序"`
	ViewCount   int         `json:"viewCount"   description:"浏览次数"`
	ClickCount  int         `json:"clickCount"  description:"点击次数"`
	CreatedAt   *gtime.Time `json:"createdAt"   description:"创建时间"`
	CreatedBy   string      `json:"createdBy"   description:"创建用户"`
	UpdatedAt   *gtime.Time `json:"updatedAt"   description:"更新时间"`
	UpdatedBy   string      `json:"updatedBy"   description:"更新用户"`
}

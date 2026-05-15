package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysBanner = sysBannerDao{}

type sysBannerDao struct {
	Table   string
	Columns sysBannerColumns
}

type sysBannerColumns struct {
	Id          string
	Title       string
	Image       string
	Category    string
	Type        string
	Position    string
	Url         string
	LinkType    string
	Summary     string
	Description string
	SortCode    string
	ViewCount   string
	ClickCount  string
	CreatedAt   string
	CreatedBy   string
	UpdatedAt   string
	UpdatedBy   string
}

func init() {
	SysBanner.Table = "sys_banner"
	SysBanner.Columns = sysBannerColumns{
		Id:          "id",
		Title:       "title",
		Image:       "image",
		Category:    "category",
		Type:        "type",
		Position:    "position",
		Url:         "url",
		LinkType:    "link_type",
		Summary:     "summary",
		Description: "description",
		SortCode:    "sort_code",
		ViewCount:   "view_count",
		ClickCount:  "click_count",
		CreatedAt:   "created_at",
		CreatedBy:   "created_by",
		UpdatedAt:   "updated_at",
		UpdatedBy:   "updated_by",
	}
}

func (d sysBannerDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

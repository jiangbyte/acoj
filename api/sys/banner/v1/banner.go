package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

type BannerPageReq struct {
	g.Meta `path:"/api/v1/sys/banner/page" method:"get" summary:"分页查询轮播图" tags:"轮播图管理"`
	utility.PageReq
}

type BannerPageRes struct {
	utility.PageRes
}

type BannerCreateReq struct {
	g.Meta      `path:"/api/v1/sys/banner/create" method:"post" summary:"添加轮播图" tags:"轮播图管理"`
	Title       string `json:"title" v:"required#标题不能为空"`
	Image       string `json:"image" v:"required#图片不能为空"`
	Category    string `json:"category"`
	Type        string `json:"type"`
	Position    string `json:"position"`
	Url         string `json:"url"`
	LinkType    string `json:"link_type"`
	Summary     string `json:"summary"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
}

type BannerCreateRes struct{}

type BannerModifyReq struct {
	g.Meta      `path:"/api/v1/sys/banner/modify" method:"post" summary:"编辑轮播图" tags:"轮播图管理"`
	Id          string `json:"id" v:"required#ID不能为空"`
	Title       string `json:"title"`
	Image       string `json:"image"`
	Category    string `json:"category"`
	Type        string `json:"type"`
	Position    string `json:"position"`
	Url         string `json:"url"`
	LinkType    string `json:"link_type"`
	Summary     string `json:"summary"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
}

type BannerModifyRes struct{}

type BannerRemoveReq struct {
	g.Meta `path:"/api/v1/sys/banner/remove" method:"post" summary:"删除轮播图" tags:"轮播图管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type BannerRemoveRes struct{}

type BannerDetailReq struct {
	g.Meta `path:"/api/v1/sys/banner/detail" method:"get" summary:"获取轮播图详情" tags:"轮播图管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type BannerDetailRes struct {
	Id          string `json:"id"`
	Title       string `json:"title"`
	Image       string `json:"image"`
	Category    string `json:"category"`
	Type        string `json:"type"`
	Position    string `json:"position"`
	Url         string `json:"url"`
	LinkType    string `json:"link_type"`
	Summary     string `json:"summary"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
	ViewCount   int    `json:"view_count"`
	ClickCount  int    `json:"click_count"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

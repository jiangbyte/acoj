package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

type NoticePageReq struct {
	g.Meta `path:"/api/v1/sys/notice/page" method:"get" summary:"分页查询通知" tags:"通知管理"`
	utility.PageReq
}

type NoticePageRes struct {
	utility.PageRes
}

type NoticeCreateReq struct {
	g.Meta   `path:"/api/v1/sys/notice/create" method:"post" summary:"添加通知" tags:"通知管理"`
	Title    string `json:"title" v:"required#标题不能为空"`
	Category string `json:"category"`
	Type     string `json:"type"`
	Summary  string `json:"summary"`
	Content  string `json:"content"`
	Cover    string `json:"cover"`
	Level    string `json:"level"`
	Position string `json:"position"`
	Status   string `json:"status"`
	SortCode int    `json:"sort_code"`
}

type NoticeCreateRes struct{}

type NoticeModifyReq struct {
	g.Meta   `path:"/api/v1/sys/notice/modify" method:"post" summary:"编辑通知" tags:"通知管理"`
	Id       string `json:"id" v:"required#ID不能为空"`
	Title    string `json:"title"`
	Category string `json:"category"`
	Type     string `json:"type"`
	Summary  string `json:"summary"`
	Content  string `json:"content"`
	Cover    string `json:"cover"`
	Level    string `json:"level"`
	Position string `json:"position"`
	Status   string `json:"status"`
	SortCode int    `json:"sort_code"`
}

type NoticeModifyRes struct{}

type NoticeRemoveReq struct {
	g.Meta `path:"/api/v1/sys/notice/remove" method:"post" summary:"删除通知" tags:"通知管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type NoticeRemoveRes struct{}

type NoticeDetailReq struct {
	g.Meta `path:"/api/v1/sys/notice/detail" method:"get" summary:"获取通知详情" tags:"通知管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type NoticeDetailRes struct {
	Id        string `json:"id"`
	Title     string `json:"title"`
	Category  string `json:"category"`
	Type      string `json:"type"`
	Summary   string `json:"summary"`
	Content   string `json:"content"`
	Cover     string `json:"cover"`
	Level     string `json:"level"`
	ViewCount int    `json:"view_count"`
	IsTop     string `json:"is_top"`
	Position  string `json:"position"`
	Status    string `json:"status"`
	SortCode  int    `json:"sort_code"`
	CreatedAt string `json:"created_at"`
	CreatedBy string `json:"created_by"`
	UpdatedAt string `json:"updated_at"`
	UpdatedBy string `json:"updated_by"`
}

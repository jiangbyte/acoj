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
	Id          string `json:"id"`
	Title       string `json:"title"`
	Category    string `json:"category"`
	Type        string `json:"type"`
	Summary     string `json:"summary"`
	Content     string `json:"content"`
	Cover       string `json:"cover"`
	Level       string `json:"level"`
	ViewCount   int    `json:"view_count"`
	IsTop       string `json:"is_top"`
	Position    string `json:"position"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	CreatedName string `json:"created_name"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
	UpdatedName string `json:"updated_name"`
}

type NoticeExportReq struct {
	g.Meta     `path:"/api/v1/sys/notice/export" method:"get" summary:"导出通知数据" tags:"通知管理"`
	ExportType string `json:"export_type" v:"required#导出类型不能为空"`
	SelectedId string `json:"selected_id"`
	utility.PageReq
}

type NoticeExportRes struct{}

type NoticeTemplateReq struct {
	g.Meta `path:"/api/v1/sys/notice/template" method:"get" summary:"下载通知导入模板" tags:"通知管理"`
}

type NoticeTemplateRes struct{}

type NoticeImportReq struct {
	g.Meta `path:"/api/v1/sys/notice/import" method:"post" summary:"导入通知数据" tags:"通知管理"`
}

type NoticeImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

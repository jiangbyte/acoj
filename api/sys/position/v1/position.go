package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

// --- Page ---

type PositionPageReq struct {
	g.Meta  `path:"/api/v1/sys/position/page" method:"get" summary:"分页查询职位" tags:"职位管理"`
	Keyword string `json:"keyword"`
	Status  string `json:"status"`
	GroupId string `json:"group_id"`
	OrgId   string `json:"org_id"`
	utility.PageReq
}

type PositionPageRes struct {
	utility.PageRes
}

// --- Create ---

type PositionCreateReq struct {
	g.Meta      `path:"/api/v1/sys/position/create" method:"post" summary:"添加职位" tags:"职位管理"`
	Code        string `json:"code" v:"required#编码不能为空"`
	Name        string `json:"name" v:"required#名称不能为空"`
	Category    string `json:"category"`
	OrgId       string `json:"org_id"`
	GroupId     string `json:"group_id"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type PositionCreateRes struct{}

// --- Modify ---

type PositionModifyReq struct {
	g.Meta      `path:"/api/v1/sys/position/modify" method:"post" summary:"编辑职位" tags:"职位管理"`
	Id          string `json:"id" v:"required#ID不能为空"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	OrgId       string `json:"org_id"`
	GroupId     string `json:"group_id"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type PositionModifyRes struct{}

// --- Remove ---

type PositionRemoveReq struct {
	g.Meta `path:"/api/v1/sys/position/remove" method:"post" summary:"删除职位" tags:"职位管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type PositionRemoveRes struct{}

// --- Detail ---

type PositionDetailReq struct {
	g.Meta `path:"/api/v1/sys/position/detail" method:"get" summary:"获取职位详情" tags:"职位管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type PositionDetailRes struct {
	Id          string `json:"id"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	OrgId       string `json:"org_id"`
	GroupId     string `json:"group_id"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
	Extra       string `json:"extra"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	CreatedName string `json:"created_name"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
	UpdatedName string `json:"updated_name"`
}

// --- Export ---

type PositionExportReq struct {
	g.Meta     `path:"/api/v1/sys/position/export" method:"get" summary:"导出职位数据" tags:"职位管理"`
	ExportType string `json:"export_type" v:"required#导出类型不能为空"`
	SelectedId string `json:"selected_id"`
	utility.PageReq
}

type PositionExportRes struct{}

// --- Template ---

type PositionTemplateReq struct {
	g.Meta `path:"/api/v1/sys/position/template" method:"get" summary:"下载职位导入模板" tags:"职位管理"`
}

type PositionTemplateRes struct{}

// --- Import ---

type PositionImportReq struct {
	g.Meta `path:"/api/v1/sys/position/import" method:"post" summary:"导入职位数据" tags:"职位管理"`
}

type PositionImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

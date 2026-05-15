package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

// --- Page ---

type GroupPageReq struct {
	g.Meta   `path:"/api/v1/sys/group/page" method:"get" summary:"分页查询用户组" tags:"用户组管理"`
	Keyword  string `json:"keyword"`
	Status   string `json:"status"`
	ParentId string `json:"parent_id"`
	OrgId    string `json:"org_id"`
	utility.PageReq
}

type GroupPageRes struct {
	utility.PageRes
}

// --- Create ---

type GroupCreateReq struct {
	g.Meta      `path:"/api/v1/sys/group/create" method:"post" summary:"添加用户组" tags:"用户组管理"`
	Code        string `json:"code" v:"required#编码不能为空"`
	Name        string `json:"name" v:"required#名称不能为空"`
	Category    string `json:"category"`
	ParentId    string `json:"parent_id"`
	OrgId       string `json:"org_id"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type GroupCreateRes struct{}

// --- Modify ---

type GroupModifyReq struct {
	g.Meta      `path:"/api/v1/sys/group/modify" method:"post" summary:"编辑用户组" tags:"用户组管理"`
	Id          string `json:"id" v:"required#ID不能为空"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	ParentId    string `json:"parent_id"`
	OrgId       string `json:"org_id"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type GroupModifyRes struct{}

// --- Remove ---

type GroupRemoveReq struct {
	g.Meta `path:"/api/v1/sys/group/remove" method:"post" summary:"删除用户组" tags:"用户组管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type GroupRemoveRes struct{}

// --- Detail ---

type GroupDetailReq struct {
	g.Meta `path:"/api/v1/sys/group/detail" method:"get" summary:"获取用户组详情" tags:"用户组管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type GroupDetailRes struct {
	Id          string `json:"id"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	ParentId    string `json:"parent_id"`
	OrgId       string `json:"org_id"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	CreatedName string `json:"created_name"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
	UpdatedName string `json:"updated_name"`
}

// --- Tree ---

type GroupTreeNode struct {
	Id          string           `json:"id"`
	Code        string           `json:"code"`
	Name        string           `json:"name"`
	Category    string           `json:"category"`
	ParentId    string           `json:"parent_id"`
	OrgId       string           `json:"org_id"`
	Description string           `json:"description"`
	Status      string           `json:"status"`
	SortCode    int              `json:"sort_code"`
	Children    []*GroupTreeNode `json:"children"`
}

type GroupTreeReq struct {
	g.Meta  `path:"/api/v1/sys/group/tree" method:"get" summary:"获取用户组树" tags:"用户组管理"`
	OrgId   string `json:"org_id"`
	Keyword string `json:"keyword"`
}

type GroupTreeRes []*GroupTreeNode

// --- Union Tree ---

type UnionGroupTreeNode struct {
	Id          string                `json:"id"`
	Code        string                `json:"code"`
	Name        string                `json:"name"`
	Category    string                `json:"category"`
	ParentId    string                `json:"parent_id"`
	OrgId       string                `json:"org_id"`
	Type        string                `json:"type"`
	Description string                `json:"description"`
	Status      string                `json:"status"`
	SortCode    int                   `json:"sort_code"`
	Children    []*UnionGroupTreeNode `json:"children"`
}

type GroupUnionTreeReq struct {
	g.Meta `path:"/api/v1/sys/group/union-tree" method:"get" summary:"获取用户组联合树" tags:"用户组管理"`
}

type GroupUnionTreeRes []*UnionGroupTreeNode

// --- Export ---

type GroupExportReq struct {
	g.Meta     `path:"/api/v1/sys/group/export" method:"get" summary:"导出用户组数据" tags:"用户组管理"`
	ExportType string `json:"export_type" v:"required#导出类型不能为空"`
	SelectedId string `json:"selected_id"`
	utility.PageReq
}

type GroupExportRes struct{}

// --- Template ---

type GroupTemplateReq struct {
	g.Meta `path:"/api/v1/sys/group/template" method:"get" summary:"下载用户组导入模板" tags:"用户组管理"`
}

type GroupTemplateRes struct{}

// --- Import ---

type GroupImportReq struct {
	g.Meta `path:"/api/v1/sys/group/import" method:"post" summary:"导入用户组数据" tags:"用户组管理"`
}

type GroupImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

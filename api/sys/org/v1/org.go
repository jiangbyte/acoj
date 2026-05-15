package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

// --- Page ---

type OrgPageReq struct {
	g.Meta   `path:"/api/v1/sys/org/page" method:"get" summary:"分页查询组织" tags:"组织管理"`
	Keyword  string `json:"keyword"`
	Status   string `json:"status"`
	ParentId string `json:"parent_id"`
	utility.PageReq
}

type OrgPageRes struct {
	utility.PageRes
}

// --- Create ---

type OrgCreateReq struct {
	g.Meta      `path:"/api/v1/sys/org/create" method:"post" summary:"添加组织" tags:"组织管理"`
	Code        string `json:"code" v:"required#编码不能为空"`
	Name        string `json:"name" v:"required#名称不能为空"`
	Category    string `json:"category"`
	ParentId    string `json:"parent_id"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type OrgCreateRes struct{}

// --- Modify ---

type OrgModifyReq struct {
	g.Meta      `path:"/api/v1/sys/org/modify" method:"post" summary:"编辑组织" tags:"组织管理"`
	Id          string `json:"id" v:"required#ID不能为空"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	ParentId    string `json:"parent_id"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type OrgModifyRes struct{}

// --- Remove ---

type OrgRemoveReq struct {
	g.Meta `path:"/api/v1/sys/org/remove" method:"post" summary:"删除组织" tags:"组织管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type OrgRemoveRes struct{}

// --- Detail ---

type OrgDetailReq struct {
	g.Meta `path:"/api/v1/sys/org/detail" method:"get" summary:"获取组织详情" tags:"组织管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type OrgDetailRes struct {
	Id          string `json:"id"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	ParentId    string `json:"parent_id"`
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

type OrgTreeNode struct {
	Id          string         `json:"id"`
	Code        string         `json:"code"`
	Name        string         `json:"name"`
	Category    string         `json:"category"`
	ParentId    string         `json:"parent_id"`
	Description string         `json:"description"`
	Status      string         `json:"status"`
	SortCode    int            `json:"sort_code"`
	Children    []*OrgTreeNode `json:"children"`
}

type OrgTreeReq struct {
	g.Meta   `path:"/api/v1/sys/org/tree" method:"get" summary:"获取组织树" tags:"组织管理"`
	Category string `json:"category"`
}

type OrgTreeRes []*OrgTreeNode

// --- Export ---

type OrgExportReq struct {
	g.Meta     `path:"/api/v1/sys/org/export" method:"get" summary:"导出组织数据" tags:"组织管理"`
	ExportType string `json:"export_type" v:"required#导出类型不能为空"`
	SelectedId string `json:"selected_id"`
	utility.PageReq
}

type OrgExportRes struct{}

// --- Template ---

type OrgTemplateReq struct {
	g.Meta `path:"/api/v1/sys/org/template" method:"get" summary:"下载组织导入模板" tags:"组织管理"`
}

type OrgTemplateRes struct{}

// --- Import ---

type OrgImportReq struct {
	g.Meta `path:"/api/v1/sys/org/import" method:"post" summary:"导入组织数据" tags:"组织管理"`
}

type OrgImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

// --- Grant Org Role ---

type GrantOrgRoleReq struct {
	g.Meta              `path:"/api/v1/sys/org/grant-role" method:"post" summary:"分配组织角色" tags:"组织管理"`
	OrgId               string   `json:"org_id" v:"required#组织ID不能为空"`
	RoleIds             []string `json:"role_ids" v:"required#角色ID不能为空"`
	Scope               string   `json:"scope"`
	CustomScopeGroupIds string   `json:"custom_scope_group_ids"`
	CustomScopeOrgIds   string   `json:"custom_scope_org_ids"`
}

type GrantOrgRoleRes struct{}

// --- Own Roles ---

type OrgOwnRolesReq struct {
	g.Meta `path:"/api/v1/sys/org/own-roles" method:"get" summary:"获取组织角色ID列表" tags:"组织管理"`
	OrgId  string `json:"org_id" v:"required#组织ID不能为空"`
}

type OrgOwnRolesRes []string

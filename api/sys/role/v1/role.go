package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

type RolePageReq struct {
	g.Meta `path:"/api/v1/sys/role/page" method:"get" summary:"分页查询角色" tags:"角色管理"`
	utility.PageReq
}

type RolePageRes struct {
	utility.PageRes
}

type RoleCreateReq struct {
	g.Meta      `path:"/api/v1/sys/role/create" method:"post" summary:"添加角色" tags:"角色管理"`
	Code        string `json:"code" v:"required#编码不能为空"`
	Name        string `json:"name" v:"required#名称不能为空"`
	Category    string `json:"category"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type RoleCreateRes struct{}

type RoleModifyReq struct {
	g.Meta      `path:"/api/v1/sys/role/modify" method:"post" summary:"编辑角色" tags:"角色管理"`
	Id          string `json:"id" v:"required#ID不能为空"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	Description string `json:"description"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type RoleModifyRes struct{}

type RoleRemoveReq struct {
	g.Meta `path:"/api/v1/sys/role/remove" method:"post" summary:"删除角色" tags:"角色管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type RoleRemoveRes struct{}

type RoleDetailReq struct {
	g.Meta `path:"/api/v1/sys/role/detail" method:"get" summary:"获取角色详情" tags:"角色管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type RoleDetailRes struct {
	Id          string `json:"id"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
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

type PermissionItem struct {
	PermissionCode      string `json:"permission_code"`
	Scope               string `json:"scope"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
}

type GrantPermissionReq struct {
	g.Meta      `path:"/api/v1/sys/role/grant-permission" method:"post" summary:"分配角色权限" tags:"角色管理"`
	RoleId      string           `json:"role_id" v:"required#角色ID不能为空"`
	Permissions []PermissionItem `json:"permissions" v:"required#权限列表不能为空"`
}

type GrantPermissionRes struct{}

type GrantResourceReq struct {
	g.Meta      `path:"/api/v1/sys/role/grant-resource" method:"post" summary:"分配角色资源" tags:"角色管理"`
	RoleId      string   `json:"role_id" v:"required#角色ID不能为空"`
	ResourceIds []string `json:"resource_ids"`
}

type GrantResourceRes struct{}

type OwnPermissionReq struct {
	g.Meta `path:"/api/v1/sys/role/own-permission" method:"get" summary:"获取角色权限编码列表" tags:"角色管理"`
	RoleId string `json:"role_id" v:"required#角色ID不能为空"`
}

type OwnPermissionRes struct {
	Codes []string `json:"codes"`
}

type OwnPermissionDetailReq struct {
	g.Meta `path:"/api/v1/sys/role/own-permission-detail" method:"get" summary:"获取角色权限详情" tags:"角色管理"`
	RoleId string `json:"role_id" v:"required#角色ID不能为空"`
}

type PermissionDetailItem struct {
	PermissionCode      string `json:"permission_code"`
	Scope               string `json:"scope"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
}

type OwnPermissionDetailRes struct {
	List []PermissionDetailItem `json:"list"`
}

type OwnResourceReq struct {
	g.Meta `path:"/api/v1/sys/role/own-resource" method:"get" summary:"获取角色资源ID列表" tags:"角色管理"`
	RoleId string `json:"role_id" v:"required#角色ID不能为空"`
}

type OwnResourceRes struct {
	ResourceIds []string `json:"resource_ids"`
}

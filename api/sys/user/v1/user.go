package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

// --- Page ---

type UserPageReq struct {
	g.Meta  `path:"/api/v1/sys/user/page" method:"get" summary:"分页查询用户" tags:"用户管理"`
	Keyword string `json:"keyword"`
	Status  string `json:"status"`
	utility.PageReq
}

type UserPageRes struct {
	utility.PageRes
}

// --- Create ---

type UserCreateReq struct {
	g.Meta     `path:"/api/v1/sys/user/create" method:"post" summary:"创建用户" tags:"用户管理"`
	Account    string   `json:"account" v:"required#账号不能为空"`
	Nickname   string   `json:"nickname"`
	Avatar     string   `json:"avatar"`
	Motto      string   `json:"motto"`
	Gender     string   `json:"gender"`
	Birthday   string   `json:"birthday"`
	Email      string   `json:"email"`
	Github     string   `json:"github"`
	Phone      string   `json:"phone"`
	OrgId      string   `json:"org_id"`
	PositionId string   `json:"position_id"`
	GroupId    string   `json:"group_id"`
	Status     string   `json:"status"`
	RoleIds    []string `json:"role_ids"`
}

type UserCreateRes struct{}

// --- Modify ---

type UserModifyReq struct {
	g.Meta     `path:"/api/v1/sys/user/modify" method:"post" summary:"编辑用户" tags:"用户管理"`
	Id         string   `json:"id" v:"required#ID不能为空"`
	Account    string   `json:"account"`
	Nickname   string   `json:"nickname"`
	Avatar     string   `json:"avatar"`
	Motto      string   `json:"motto"`
	Gender     string   `json:"gender"`
	Birthday   string   `json:"birthday"`
	Email      string   `json:"email"`
	Github     string   `json:"github"`
	Phone      string   `json:"phone"`
	OrgId      string   `json:"org_id"`
	PositionId string   `json:"position_id"`
	GroupId    string   `json:"group_id"`
	Status     string   `json:"status"`
	RoleIds    []string `json:"role_ids"`
}

type UserModifyRes struct{}

// --- Remove ---

type UserRemoveReq struct {
	g.Meta `path:"/api/v1/sys/user/remove" method:"post" summary:"删除用户" tags:"用户管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type UserRemoveRes struct{}

// --- Detail ---

type UserDetailReq struct {
	g.Meta `path:"/api/v1/sys/user/detail" method:"get" summary:"获取用户详情" tags:"用户管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type UserDetailRes struct {
	Id           string   `json:"id"`
	Account      string   `json:"account"`
	Nickname     string   `json:"nickname"`
	Avatar       string   `json:"avatar"`
	Motto        string   `json:"motto"`
	Gender       string   `json:"gender"`
	Birthday     string   `json:"birthday"`
	Email        string   `json:"email"`
	Github       string   `json:"github"`
	Phone        string   `json:"phone"`
	OrgId        string   `json:"org_id"`
	PositionId   string   `json:"position_id"`
	GroupId      string   `json:"group_id"`
	OrgNames     []string `json:"org_names"`
	GroupNames   []string `json:"group_names"`
	PositionName string   `json:"position_name"`
	Status       string   `json:"status"`
	LastLoginAt  string   `json:"last_login_at"`
	LastLoginIp  string   `json:"last_login_ip"`
	LoginCount   int      `json:"login_count"`
	CreatedAt    string   `json:"created_at"`
	CreatedBy    string   `json:"created_by"`
	CreatedName  string   `json:"created_name"`
	UpdatedAt    string   `json:"updated_at"`
	UpdatedBy    string   `json:"updated_by"`
	UpdatedName  string   `json:"updated_name"`
	RoleIds      []string `json:"role_ids"`
}

// --- Grant Role ---

type GrantRoleReq struct {
	g.Meta              `path:"/api/v1/sys/user/grant-role" method:"post" summary:"分配用户角色" tags:"用户管理"`
	UserId              string   `json:"user_id" v:"required#用户ID不能为空"`
	RoleIds             []string `json:"role_ids" v:"required#角色ID不能为空"`
	Scope               string   `json:"scope"`
	CustomScopeGroupIds string   `json:"custom_scope_group_ids"`
}

type GrantRoleRes struct{}

// --- Grant Permission ---

type PermissionItem struct {
	PermissionCode      string `json:"permission_code"`
	Scope               string `json:"scope"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
}

type GrantPermissionReq struct {
	g.Meta      `path:"/api/v1/sys/user/grant-permission" method:"post" summary:"分配用户权限" tags:"用户管理"`
	UserId      string           `json:"user_id" v:"required#用户ID不能为空"`
	Permissions []PermissionItem `json:"permissions"`
}

type GrantPermissionRes struct{}

// --- Permission Detail ---

type OwnPermissionDetailReq struct {
	g.Meta `path:"/api/v1/sys/user/own-permission-detail" method:"get" summary:"获取用户权限详情" tags:"用户管理"`
	UserId string `json:"user_id" v:"required#用户ID不能为空"`
}

type PermissionDetailItem struct {
	PermissionCode      string `json:"permission_code"`
	Scope               string `json:"scope"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
}

type OwnPermissionDetailRes struct{}

// --- Own Roles ---

type OwnRolesReq struct {
	g.Meta `path:"/api/v1/sys/user/own-roles" method:"get" summary:"获取用户角色ID" tags:"用户管理"`
	UserId string `json:"user_id" v:"required#用户ID不能为空"`
}

type OwnRolesRes struct{}

// --- Current User Info ---

type CurrentUserReq struct {
	g.Meta `path:"/api/v1/sys/user/current" method:"get" summary:"获取当前用户信息" tags:"用户管理"`
}

type CurrentUserRes struct {
	Id           string   `json:"id"`
	Account      string   `json:"account"`
	Nickname     string   `json:"nickname"`
	Avatar       string   `json:"avatar"`
	Motto        string   `json:"motto"`
	Gender       string   `json:"gender"`
	Birthday     string   `json:"birthday"`
	Email        string   `json:"email"`
	Github       string   `json:"github"`
	Phone        string   `json:"phone"`
	Status       string   `json:"status"`
	OrgNames     []string `json:"org_names"`
	GroupNames   []string `json:"group_names"`
	PositionName string   `json:"position_name"`
	LastLoginAt  string   `json:"last_login_at"`
	LastLoginIp  string   `json:"last_login_ip"`
	LoginCount   int      `json:"login_count"`
}

// --- Menus ---

type MenuNode struct {
	Id            string      `json:"id"`
	Code          string      `json:"code"`
	Name          string      `json:"name"`
	Type          string      `json:"type"`
	ParentId      string      `json:"parent_id"`
	RoutePath     string      `json:"route_path"`
	ComponentPath string      `json:"component_path"`
	RedirectPath  string      `json:"redirect_path"`
	Icon          string      `json:"icon"`
	IsVisible     bool        `json:"is_visible"`
	IsCache       bool        `json:"is_cache"`
	IsAffix       bool        `json:"is_affix"`
	IsBreadcrumb  bool        `json:"is_breadcrumb"`
	SortCode      int         `json:"sort_code"`
	Children      []*MenuNode `json:"children"`
}

type MenusReq struct {
	g.Meta `path:"/api/v1/sys/user/menus" method:"get" summary:"获取当前用户菜单" tags:"用户管理"`
}

type MenusRes []*MenuNode

// --- User Permissions ---

type UserPermissionsReq struct {
	g.Meta `path:"/api/v1/sys/user/permissions" method:"get" summary:"获取当前用户权限码" tags:"用户管理"`
}

type UserPermissionsRes struct{}

// --- Update Profile ---

type UpdateProfileReq struct {
	g.Meta   `path:"/api/v1/sys/user/update-profile" method:"post" summary:"更新个人信息" tags:"用户管理"`
	Account  string `json:"account"`
	Nickname string `json:"nickname"`
	Motto    string `json:"motto"`
	Gender   string `json:"gender"`
	Birthday string `json:"birthday"`
	Email    string `json:"email"`
	Github   string `json:"github"`
	Phone    string `json:"phone"`
}

type UpdateProfileRes struct{}

// --- Update Avatar ---

type UpdateAvatarReq struct {
	g.Meta `path:"/api/v1/sys/user/update-avatar" method:"post" summary:"更新头像" tags:"用户管理"`
	Avatar string `json:"avatar" v:"required#头像不能为空"`
}

type UpdateAvatarRes struct{}

// --- Update Password ---

type UpdatePasswordReq struct {
	g.Meta          `path:"/api/v1/sys/user/update-password" method:"post" summary:"修改密码" tags:"用户管理"`
	CurrentPassword string `json:"current_password" v:"required#当前密码不能为空"`
	NewPassword     string `json:"new_password" v:"required#新密码不能为空"`
}

type UpdatePasswordRes struct{}

// --- Export ---

type UserExportReq struct {
	g.Meta     `path:"/api/v1/sys/user/export" method:"get" summary:"导出用户数据" tags:"用户管理"`
	ExportType string `json:"export_type" v:"required#导出类型不能为空"`
	SelectedId string `json:"selected_id"`
	utility.PageReq
}

type UserExportRes struct{}

// --- Template ---

type UserTemplateReq struct {
	g.Meta `path:"/api/v1/sys/user/template" method:"get" summary:"下载用户导入模板" tags:"用户管理"`
}

type UserTemplateRes struct{}

// --- Import ---

type UserImportReq struct {
	g.Meta `path:"/api/v1/sys/user/import" method:"post" summary:"导入用户数据" tags:"用户管理"`
}

type UserImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

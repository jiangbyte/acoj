package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

// --- Page ---

type ClientUserPageReq struct {
	g.Meta  `path:"/api/v1/client-user/page" method:"get" summary:"分页查询C端用户" tags:"C端用户管理"`
	Keyword string `json:"keyword"`
	Status  string `json:"status"`
	utility.PageReq
}

type ClientUserPageRes struct {
	utility.PageRes
}

// --- Create ---

type ClientUserCreateReq struct {
	g.Meta   `path:"/api/v1/client-user/create" method:"post" summary:"创建C端用户" tags:"C端用户管理"`
	Account  string `json:"account" v:"required#账号不能为空"`
	Nickname string `json:"nickname"`
	Avatar   string `json:"avatar"`
	Motto    string `json:"motto"`
	Gender   string `json:"gender"`
	Birthday string `json:"birthday"`
	Email    string `json:"email"`
	Github   string `json:"github"`
	Phone    string `json:"phone"`
	Password string `json:"password"`
	Status   string `json:"status"`
}

type ClientUserCreateRes struct{}

// --- Modify ---

type ClientUserModifyReq struct {
	g.Meta   `path:"/api/v1/client-user/modify" method:"post" summary:"编辑C端用户" tags:"C端用户管理"`
	Id       string `json:"id" v:"required#ID不能为空"`
	Account  string `json:"account"`
	Nickname string `json:"nickname"`
	Avatar   string `json:"avatar"`
	Motto    string `json:"motto"`
	Gender   string `json:"gender"`
	Birthday string `json:"birthday"`
	Email    string `json:"email"`
	Github   string `json:"github"`
	Phone    string `json:"phone"`
	Status   string `json:"status"`
}

type ClientUserModifyRes struct{}

// --- Remove ---

type ClientUserRemoveReq struct {
	g.Meta `path:"/api/v1/client-user/remove" method:"post" summary:"删除C端用户" tags:"C端用户管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type ClientUserRemoveRes struct{}

// --- Detail ---

type ClientUserDetailReq struct {
	g.Meta `path:"/api/v1/client-user/detail" method:"get" summary:"获取C端用户详情" tags:"C端用户管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type ClientUserDetailRes struct {
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
	LastLoginAt  string   `json:"last_login_at"`
	LastLoginIp  string   `json:"last_login_ip"`
	LoginCount   int      `json:"login_count"`
	OrgNames     []string `json:"org_names"`
	GroupNames   []string `json:"group_names"`
	PositionName string   `json:"position_name"`
	CreatedAt    string   `json:"created_at"`
	CreatedBy    string   `json:"created_by"`
	CreatedName  string   `json:"created_name"`
	UpdatedAt    string   `json:"updated_at"`
	UpdatedBy    string   `json:"updated_by"`
	UpdatedName  string   `json:"updated_name"`
}

// --- Current User ---

type ClientUserCurrentReq struct {
	g.Meta `path:"/api/v1/client-user/current" method:"get" summary:"获取当前C端用户信息" tags:"C端用户管理"`
}

type ClientUserCurrentRes struct {
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
	LastLoginAt  string   `json:"last_login_at"`
	LastLoginIp  string   `json:"last_login_ip"`
	LoginCount   int      `json:"login_count"`
	OrgNames     []string `json:"org_names"`
	GroupNames   []string `json:"group_names"`
	PositionName string   `json:"position_name"`
	CreatedBy    string   `json:"created_by"`
	CreatedName  string   `json:"created_name"`
	UpdatedBy    string   `json:"updated_by"`
	UpdatedName  string   `json:"updated_name"`
}

// --- Update Profile ---

type ClientUserUpdateProfileReq struct {
	g.Meta   `path:"/api/v1/client-user/update-profile" method:"post" summary:"更新个人信息" tags:"C端用户管理"`
	Account  string `json:"account"`
	Nickname string `json:"nickname"`
	Motto    string `json:"motto"`
	Gender   string `json:"gender"`
	Birthday string `json:"birthday"`
	Email    string `json:"email"`
	Github   string `json:"github"`
}

type ClientUserUpdateProfileRes struct{}

// --- Update Avatar ---

type ClientUserUpdateAvatarReq struct {
	g.Meta `path:"/api/v1/client-user/update-avatar" method:"post" summary:"更新头像" tags:"C端用户管理"`
	Avatar string `json:"avatar" v:"required#头像不能为空"`
}

type ClientUserUpdateAvatarRes struct{}

// --- Update Password ---

type ClientUserUpdatePasswordReq struct {
	g.Meta          `path:"/api/v1/client-user/update-password" method:"post" summary:"修改密码" tags:"C端用户管理"`
	CurrentPassword string `json:"current_password" v:"required#当前密码不能为空"`
	NewPassword     string `json:"new_password" v:"required#新密码不能为空"`
}

type ClientUserUpdatePasswordRes struct{}

// --- Export ---

type ClientUserExportReq struct {
	g.Meta     `path:"/api/v1/client-user/export" method:"get" summary:"导出C端用户数据" tags:"C端用户管理"`
	ExportType string `json:"export_type" v:"required#导出类型不能为空"`
	SelectedId string `json:"selected_id"`
	utility.PageReq
}

type ClientUserExportRes struct{}

// --- Template ---

type ClientUserTemplateReq struct {
	g.Meta `path:"/api/v1/client-user/template" method:"get" summary:"下载C端用户导入模板" tags:"C端用户管理"`
}

type ClientUserTemplateRes struct{}

// --- Import ---

type ClientUserImportReq struct {
	g.Meta `path:"/api/v1/client-user/import" method:"post" summary:"导入C端用户数据" tags:"C端用户管理"`
}

type ClientUserImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

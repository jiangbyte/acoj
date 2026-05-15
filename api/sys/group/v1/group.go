package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

// --- Page ---

type GroupPageReq struct {
	g.Meta  `path:"/api/v1/sys/group/page" method:"get" summary:"分页查询用户组" tags:"用户组管理"`
	Keyword string `json:"keyword"`
	Status  string `json:"status"`
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
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
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
	g.Meta `path:"/api/v1/sys/group/tree" method:"get" summary:"获取用户组树" tags:"用户组管理"`
}

type GroupTreeRes struct {
	List []*GroupTreeNode `json:"list"`
}

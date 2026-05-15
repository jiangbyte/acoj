package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

// --- Page ---

type OrgPageReq struct {
	g.Meta  `path:"/api/v1/sys/org/page" method:"get" summary:"分页查询组织" tags:"组织管理"`
	Keyword string `json:"keyword"`
	Status  string `json:"status"`
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
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
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
	g.Meta `path:"/api/v1/sys/org/tree" method:"get" summary:"获取组织树" tags:"组织管理"`
}

type OrgTreeRes struct {
	List []*OrgTreeNode `json:"list"`
}

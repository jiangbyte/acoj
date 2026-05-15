package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

type DictPageReq struct {
	g.Meta  `path:"/api/v1/sys/dict/page" method:"get" summary:"分页查询字典" tags:"字典管理"`
	Keyword string `json:"keyword"`
	Status  string `json:"status"`
	utility.PageReq
}

type DictPageRes struct {
	utility.PageRes
}

type DictCreateReq struct {
	g.Meta   `path:"/api/v1/sys/dict/create" method:"post" summary:"添加字典" tags:"字典管理"`
	Code     string `json:"code" v:"required#编码不能为空"`
	Label    string `json:"label" v:"required#标签不能为空"`
	Value    string `json:"value"`
	Color    string `json:"color"`
	Category string `json:"category"`
	ParentId string `json:"parent_id"`
	Status   string `json:"status"`
	SortCode int    `json:"sort_code"`
}

type DictCreateRes struct{}

type DictModifyReq struct {
	g.Meta   `path:"/api/v1/sys/dict/modify" method:"post" summary:"编辑字典" tags:"字典管理"`
	Id       string `json:"id" v:"required#ID不能为空"`
	Code     string `json:"code"`
	Label    string `json:"label"`
	Value    string `json:"value"`
	Color    string `json:"color"`
	Category string `json:"category"`
	ParentId string `json:"parent_id"`
	Status   string `json:"status"`
	SortCode int    `json:"sort_code"`
}

type DictModifyRes struct{}

type DictRemoveReq struct {
	g.Meta `path:"/api/v1/sys/dict/remove" method:"post" summary:"删除字典" tags:"字典管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type DictRemoveRes struct{}

type DictDetailReq struct {
	g.Meta `path:"/api/v1/sys/dict/detail" method:"get" summary:"获取字典详情" tags:"字典管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type DictDetailRes struct {
	Id        string `json:"id"`
	Code      string `json:"code"`
	Label     string `json:"label"`
	Value     string `json:"value"`
	Color     string `json:"color"`
	Category  string `json:"category"`
	ParentId  string `json:"parent_id"`
	Status    string `json:"status"`
	SortCode  int    `json:"sort_code"`
	CreatedAt string `json:"created_at"`
	CreatedBy string `json:"created_by"`
	UpdatedAt string `json:"updated_at"`
	UpdatedBy string `json:"updated_by"`
}

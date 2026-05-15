package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

type DictPageReq struct {
	g.Meta   `path:"/api/v1/sys/dict/page" method:"get" summary:"分页查询字典" tags:"字典管理"`
	Keyword  string `json:"keyword"`
	ParentId string `json:"parent_id"`
	Category string `json:"category"`
	utility.PageReq
}

type DictPageRes struct {
	utility.PageRes
}

type DictCreateReq struct {
	g.Meta   `path:"/api/v1/sys/dict/create" method:"post" summary:"添加字典" tags:"字典管理"`
	Code     string `json:"code"`
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
	Id          string `json:"id"`
	Code        string `json:"code"`
	Label       string `json:"label"`
	Value       string `json:"value"`
	Color       string `json:"color"`
	Category    string `json:"category"`
	ParentId    string `json:"parent_id"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	CreatedName string `json:"created_name"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
	UpdatedName string `json:"updated_name"`
}

// --- List ---

type DictListReq struct {
	g.Meta   `path:"/api/v1/sys/dict/list" method:"get" summary:"获取字典列表" tags:"字典管理"`
	ParentId string `json:"parent_id"`
	Category string `json:"category"`
}

type DictListRes []g.Map

// --- Tree ---

type DictTreeReq struct {
	g.Meta   `path:"/api/v1/sys/dict/tree" method:"get" summary:"获取字典树" tags:"字典管理"`
	Category string `json:"category"`
	Status   string `json:"status"`
}

type DictTreeRes []g.Map

// --- Get Label ---

type DictGetLabelReq struct {
	g.Meta   `path:"/api/v1/sys/dict/get-label" method:"get" summary:"根据字典编码和值获取字典标签" tags:"字典管理"`
	TypeCode string `json:"type_code" v:"required#编码不能为空"`
	Value    string `json:"value" v:"required#值不能为空"`
}

type DictGetLabelRes struct {
	TypeCode string `json:"type_code"`
	Value    string `json:"value"`
	Label    string `json:"label"`
}

// --- Get Children ---

type DictGetChildrenReq struct {
	g.Meta   `path:"/api/v1/sys/dict/get-children" method:"get" summary:"根据字典编码获取子字典列表" tags:"字典管理"`
	TypeCode string `json:"type_code" v:"required#编码不能为空"`
}

type DictGetChildrenRes []g.Map

// --- Export ---

type DictExportReq struct {
	g.Meta     `path:"/api/v1/sys/dict/export" method:"get" summary:"导出字典数据" tags:"字典管理"`
	ExportType string `json:"export_type" v:"required#导出类型不能为空"`
	SelectedId string `json:"selected_id"`
	utility.PageReq
}

type DictExportRes struct{}

// --- Template ---

type DictTemplateReq struct {
	g.Meta `path:"/api/v1/sys/dict/template" method:"get" summary:"下载字典导入模板" tags:"字典管理"`
}

type DictTemplateRes struct{}

// --- Import ---

type DictImportReq struct {
	g.Meta `path:"/api/v1/sys/dict/import" method:"post" summary:"导入字典数据" tags:"字典管理"`
}

type DictImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

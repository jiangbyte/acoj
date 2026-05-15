package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

type ConfigPageReq struct {
	g.Meta   `path:"/api/v1/sys/config/page" method:"get" summary:"分页查询配置" tags:"配置管理"`
	Keyword  string `json:"keyword"`
	Category string `json:"category"`
	utility.PageReq
}

type ConfigPageRes struct {
	utility.PageRes
}

type ConfigCreateReq struct {
	g.Meta      `path:"/api/v1/sys/config/create" method:"post" summary:"添加配置" tags:"配置管理"`
	ConfigKey   string `json:"config_key" v:"required#键不能为空"`
	ConfigValue string `json:"config_value" v:"required#值不能为空"`
	Category    string `json:"category"`
	Remark      string `json:"remark"`
	SortCode    int    `json:"sort_code"`
	ExtJson     string `json:"ext_json"`
}

type ConfigCreateRes struct{}

type ConfigModifyReq struct {
	g.Meta      `path:"/api/v1/sys/config/modify" method:"post" summary:"编辑配置" tags:"配置管理"`
	Id          string `json:"id" v:"required#ID不能为空"`
	ConfigKey   string `json:"config_key"`
	ConfigValue string `json:"config_value"`
	Category    string `json:"category"`
	Remark      string `json:"remark"`
	SortCode    int    `json:"sort_code"`
	ExtJson     string `json:"ext_json"`
}

type ConfigModifyRes struct{}

type ConfigRemoveReq struct {
	g.Meta `path:"/api/v1/sys/config/remove" method:"post" summary:"删除配置" tags:"配置管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type ConfigRemoveRes struct{}

type ConfigDetailReq struct {
	g.Meta `path:"/api/v1/sys/config/detail" method:"get" summary:"获取配置详情" tags:"配置管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type ConfigDetailRes struct {
	Id          string `json:"id"`
	ConfigKey   string `json:"config_key"`
	ConfigValue string `json:"config_value"`
	Category    string `json:"category"`
	Remark      string `json:"remark"`
	SortCode    int    `json:"sort_code"`
	ExtJson     string `json:"ext_json"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	CreatedName string `json:"created_name"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
	UpdatedName string `json:"updated_name"`
}

// --- List By Category ---

type ConfigListByCategoryReq struct {
	g.Meta   `path:"/api/v1/sys/config/list-by-category" method:"get" summary:"按分类查询配置列表" tags:"配置管理"`
	Category string `json:"category"`
}

type ConfigListByCategoryRes struct{}

// --- Batch Edit ---

type ConfigBatchEditItem struct {
	Id          string `json:"id" v:"required#ID不能为空"`
	ConfigKey   string `json:"config_key"`
	ConfigValue string `json:"config_value"`
	SortCode    int    `json:"sort_code"`
	Remark      string `json:"remark"`
	ExtJson     string `json:"ext_json"`
}

type ConfigBatchEditReq struct {
	g.Meta  `path:"/api/v1/sys/config/edit-batch" method:"post" summary:"批量编辑配置" tags:"配置管理"`
	Configs []ConfigBatchEditItem `json:"configs" v:"required#配置列表不能为空"`
}

type ConfigBatchEditRes struct{}

// --- Category Edit ---

type ConfigCategoryEditItem struct {
	ConfigKey   string `json:"config_key"`
	ConfigValue string `json:"config_value"`
}

type ConfigCategoryEditReq struct {
	g.Meta   `path:"/api/v1/sys/config/edit-by-category" method:"post" summary:"按分类批量编辑配置" tags:"配置管理"`
	Category string                   `json:"category"`
	Configs  []ConfigCategoryEditItem `json:"configs" v:"required#配置列表不能为空"`
}

type ConfigCategoryEditRes struct{}

// --- Export ---

type ConfigExportReq struct {
	g.Meta     `path:"/api/v1/sys/config/export" method:"post" summary:"导出配置数据" tags:"配置管理"`
	ExportType string `json:"export_type" v:"required#导出类型不能为空"`
	SelectedId string `json:"selected_id"`
	utility.PageReq
}

type ConfigExportRes struct{}

// --- Template ---

type ConfigTemplateReq struct {
	g.Meta `path:"/api/v1/sys/config/template" method:"get" summary:"下载配置导入模板" tags:"配置管理"`
}

type ConfigTemplateRes struct{}

// --- Import ---

type ConfigImportReq struct {
	g.Meta `path:"/api/v1/sys/config/import" method:"post" summary:"导入配置数据" tags:"配置管理"`
}

type ConfigImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

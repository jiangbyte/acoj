package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

type ConfigPageReq struct {
	g.Meta  `path:"/api/v1/sys/config/page" method:"get" summary:"分页查询配置" tags:"配置管理"`
	Keyword string `json:"keyword"`
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
	Extra       string `json:"extra"`
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
	Extra       string `json:"extra"`
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
	Extra       string `json:"extra"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

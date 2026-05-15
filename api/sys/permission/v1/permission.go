package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

type PermissionPageReq struct {
	g.Meta `path:"/api/v1/sys/permission/page" method:"get" summary:"获取所有权限列表" tags:"权限管理"`
	utility.PageReq
}

type PermissionPageRes struct {
	utility.PageRes
}

type PermissionScanReq struct {
	g.Meta `path:"/api/v1/sys/permission/scan" method:"post" summary:"触发权限扫描" tags:"权限管理"`
}

type PermissionScanRes struct{}

type PermissionModulesReq struct {
	g.Meta `path:"/api/v1/sys/permission/modules" method:"get" summary:"获取模块列表" tags:"权限管理"`
}

type PermissionModulesRes struct {
	Modules []string `json:"modules"`
}

type PermissionListByModuleReq struct {
	g.Meta `path:"/api/v1/sys/permission/list-by-module" method:"get" summary:"按模块获取权限列表" tags:"权限管理"`
	Module string `json:"module" v:"required#模块不能为空"`
}

type PermissionItem struct {
	Code     string `json:"code"`
	Module   string `json:"module"`
	Category string `json:"category"`
	Name     string `json:"name"`
}

type PermissionListByModuleRes struct {
	List []*PermissionItem `json:"list"`
}

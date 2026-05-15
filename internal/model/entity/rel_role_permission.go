package entity

import "github.com/gogf/gf/v2/util/gmeta"

type RelRolePermission struct {
	gmeta.Meta          `orm:"table:rel_role_permission"`
	Id                  string `json:"id"                   description:"主键"`
	RoleId              string `json:"roleId"               description:"角色ID"`
	PermissionCode      string `json:"permissionCode"       description:"权限编码"`
	Scope               string `json:"scope"                description:"数据范围"`
	CustomScopeGroupIds string `json:"customScopeGroupIds"  description:"自定义用户组ID列表"`
	CustomScopeOrgIds   string `json:"customScopeOrgIds"    description:"自定义组织ID列表"`
}

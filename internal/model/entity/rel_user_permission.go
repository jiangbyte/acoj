package entity

import "github.com/gogf/gf/v2/util/gmeta"

type RelUserPermission struct {
	gmeta.Meta          `orm:"table:rel_user_permission"`
	Id                  string `json:"id"                   description:"主键"`
	UserId              string `json:"userId"               description:"用户ID"`
	PermissionCode      string `json:"permissionCode"       description:"权限编码"`
	Scope               string `json:"scope"                description:"数据范围"`
	CustomScopeGroupIds string `json:"customScopeGroupIds"  description:"自定义用户组ID列表"`
	CustomScopeOrgIds   string `json:"customScopeOrgIds"    description:"自定义组织ID列表"`
}

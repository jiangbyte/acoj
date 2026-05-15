package entity

import "github.com/gogf/gf/v2/util/gmeta"

type RelOrgRole struct {
	gmeta.Meta          `orm:"table:rel_org_role"`
	Id                  string `json:"id"                   description:"主键"`
	OrgId               string `json:"orgId"                description:"组织ID"`
	RoleId              string `json:"roleId"               description:"角色ID"`
	Scope               string `json:"scope"                description:"数据范围覆盖"`
	CustomScopeGroupIds string `json:"customScopeGroupIds"  description:"自定义用户组ID列表"`
	CustomScopeOrgIds   string `json:"customScopeOrgIds"    description:"自定义组织ID列表"`
}

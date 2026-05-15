package do

import "github.com/gogf/gf/v2/util/gmeta"

type RelRolePermission struct {
	gmeta.Meta          `orm:"table:rel_role_permission"`
	Id                  interface{} `json:"id"`
	RoleId              interface{} `json:"roleId"`
	PermissionCode      interface{} `json:"permissionCode"`
	Scope               interface{} `json:"scope"`
	CustomScopeGroupIds interface{} `json:"customScopeGroupIds"`
	CustomScopeOrgIds   interface{} `json:"customScopeOrgIds"`
}

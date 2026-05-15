package do

import "github.com/gogf/gf/v2/util/gmeta"

type RelUserPermission struct {
	gmeta.Meta          `orm:"table:rel_user_permission"`
	Id                  interface{} `json:"id"`
	UserId              interface{} `json:"userId"`
	PermissionCode      interface{} `json:"permissionCode"`
	Scope               interface{} `json:"scope"`
	CustomScopeGroupIds interface{} `json:"customScopeGroupIds"`
	CustomScopeOrgIds   interface{} `json:"customScopeOrgIds"`
}

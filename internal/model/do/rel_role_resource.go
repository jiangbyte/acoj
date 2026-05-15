package do

import "github.com/gogf/gf/v2/util/gmeta"

type RelRoleResource struct {
	gmeta.Meta `orm:"table:rel_role_resource"`
	Id         interface{} `json:"id"`
	RoleId     interface{} `json:"roleId"`
	ResourceId interface{} `json:"resourceId"`
}

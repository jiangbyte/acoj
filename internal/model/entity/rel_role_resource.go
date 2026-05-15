package entity

import "github.com/gogf/gf/v2/util/gmeta"

type RelRoleResource struct {
	gmeta.Meta `orm:"table:rel_role_resource"`
	Id         string `json:"id"          description:"主键"`
	RoleId     string `json:"roleId"      description:"角色ID"`
	ResourceId string `json:"resourceId"  description:"资源ID"`
}

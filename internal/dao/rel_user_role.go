package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var RelUserRole = relUserRoleDao{}

type relUserRoleDao struct {
	Table   string
	Columns relUserRoleColumns
}

type relUserRoleColumns struct {
	Id                  string
	UserId              string
	RoleId              string
	Scope               string
	CustomScopeGroupIds string
}

func init() {
	RelUserRole.Table = "rel_user_role"
	RelUserRole.Columns = relUserRoleColumns{
		Id:                  "id",
		UserId:              "user_id",
		RoleId:              "role_id",
		Scope:               "scope",
		CustomScopeGroupIds: "custom_scope_group_ids",
	}
}

func (d relUserRoleDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

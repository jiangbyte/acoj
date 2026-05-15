package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var RelUserPermission = relUserPermissionDao{}

type relUserPermissionDao struct {
	Table   string
	Columns relUserPermissionColumns
}

type relUserPermissionColumns struct {
	Id                  string
	UserId              string
	PermissionCode      string
	Scope               string
	CustomScopeGroupIds string
	CustomScopeOrgIds   string
}

func init() {
	RelUserPermission.Table = "rel_user_permission"
	RelUserPermission.Columns = relUserPermissionColumns{
		Id:                  "id",
		UserId:              "user_id",
		PermissionCode:      "permission_code",
		Scope:               "scope",
		CustomScopeGroupIds: "custom_scope_group_ids",
		CustomScopeOrgIds:   "custom_scope_org_ids",
	}
}

func (d relUserPermissionDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

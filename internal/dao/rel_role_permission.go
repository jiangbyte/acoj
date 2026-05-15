package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var RelRolePermission = relRolePermissionDao{}

type relRolePermissionDao struct {
	Table   string
	Columns relRolePermissionColumns
}

type relRolePermissionColumns struct {
	Id                  string
	RoleId              string
	PermissionCode      string
	Scope               string
	CustomScopeGroupIds string
	CustomScopeOrgIds   string
}

func init() {
	RelRolePermission.Table = "rel_role_permission"
	RelRolePermission.Columns = relRolePermissionColumns{
		Id:                  "id",
		RoleId:              "role_id",
		PermissionCode:      "permission_code",
		Scope:               "scope",
		CustomScopeGroupIds: "custom_scope_group_ids",
		CustomScopeOrgIds:   "custom_scope_org_ids",
	}
}

func (d relRolePermissionDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

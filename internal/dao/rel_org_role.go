package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var RelOrgRole = relOrgRoleDao{}

type relOrgRoleDao struct {
	Table   string
	Columns relOrgRoleColumns
}

type relOrgRoleColumns struct {
	Id                  string
	OrgId               string
	RoleId              string
	Scope               string
	CustomScopeGroupIds string
	CustomScopeOrgIds   string
}

func init() {
	RelOrgRole.Table = "rel_org_role"
	RelOrgRole.Columns = relOrgRoleColumns{
		Id:                  "id",
		OrgId:               "org_id",
		RoleId:              "role_id",
		Scope:               "scope",
		CustomScopeGroupIds: "custom_scope_group_ids",
		CustomScopeOrgIds:   "custom_scope_org_ids",
	}
}

func (d relOrgRoleDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

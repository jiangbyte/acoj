package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var RelRoleResource = relRoleResourceDao{}

type relRoleResourceDao struct {
	Table   string
	Columns relRoleResourceColumns
}

type relRoleResourceColumns struct {
	Id         string
	RoleId     string
	ResourceId string
}

func init() {
	RelRoleResource.Table = "rel_role_resource"
	RelRoleResource.Columns = relRoleResourceColumns{
		Id:         "id",
		RoleId:     "role_id",
		ResourceId: "resource_id",
	}
}

func (d relRoleResourceDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

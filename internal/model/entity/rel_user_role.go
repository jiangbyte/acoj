package entity

import "github.com/gogf/gf/v2/util/gmeta"

type RelUserRole struct {
	gmeta.Meta          `orm:"table:rel_user_role"`
	Id                  string `json:"id"                   description:"主键"`
	UserId              string `json:"userId"               description:"用户ID"`
	RoleId              string `json:"roleId"               description:"角色ID"`
	Scope               string `json:"scope"                description:"数据范围覆盖"`
	CustomScopeGroupIds string `json:"customScopeGroupIds"  description:"自定义数据范围组ID列表"`
}

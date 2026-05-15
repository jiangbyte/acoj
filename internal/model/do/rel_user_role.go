package do

import "github.com/gogf/gf/v2/util/gmeta"

type RelUserRole struct {
	gmeta.Meta          `orm:"table:rel_user_role"`
	Id                  interface{} `json:"id"`
	UserId              interface{} `json:"userId"`
	RoleId              interface{} `json:"roleId"`
	Scope               interface{} `json:"scope"`
	CustomScopeGroupIds interface{} `json:"customScopeGroupIds"`
}

package do

import "github.com/gogf/gf/v2/util/gmeta"

type RelOrgRole struct {
	gmeta.Meta          `orm:"table:rel_org_role"`
	Id                  interface{} `json:"id"`
	OrgId               interface{} `json:"orgId"`
	RoleId              interface{} `json:"roleId"`
	Scope               interface{} `json:"scope"`
	CustomScopeGroupIds interface{} `json:"customScopeGroupIds"`
	CustomScopeOrgIds   interface{} `json:"customScopeOrgIds"`
}

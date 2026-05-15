package do

import "github.com/gogf/gf/v2/util/gmeta"

type ClientUser struct {
	gmeta.Meta  `orm:"table:client_user"`
	Id          interface{} `json:"id"`
	Account     interface{} `json:"account"`
	Password    interface{} `json:"password"`
	Nickname    interface{} `json:"nickname"`
	Avatar      interface{} `json:"avatar"`
	Motto       interface{} `json:"motto"`
	Gender      interface{} `json:"gender"`
	Birthday    interface{} `json:"birthday"`
	Email       interface{} `json:"email"`
	Github      interface{} `json:"github"`
	Phone       interface{} `json:"phone"`
	OrgId       interface{} `json:"orgId"`
	PositionId  interface{} `json:"positionId"`
	GroupId     interface{} `json:"groupId"`
	Status      interface{} `json:"status"`
	LastLoginAt interface{} `json:"lastLoginAt"`
	LastLoginIp interface{} `json:"lastLoginIp"`
	LoginCount  interface{} `json:"loginCount"`
	CreatedAt   interface{} `json:"createdAt"`
	CreatedBy   interface{} `json:"createdBy"`
	UpdatedAt   interface{} `json:"updatedAt"`
	UpdatedBy   interface{} `json:"updatedBy"`
}

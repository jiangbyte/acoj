package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysUser struct {
	gmeta.Meta  `orm:"table:sys_user"`
	Id          string      `json:"id"          description:"主键"`
	Account     string      `json:"account"     description:"账号"`
	Password    string      `json:"password"    description:"密码"`
	Nickname    string      `json:"nickname"    description:"昵称"`
	Avatar      string      `json:"avatar"      description:"头像"`
	Motto       string      `json:"motto"       description:"座右铭"`
	Gender      string      `json:"gender"      description:"性别"`
	Birthday    *gtime.Time `json:"birthday"    description:"生日"`
	Email       string      `json:"email"       description:"电子邮箱"`
	Github      string      `json:"github"      description:"GitHub"`
	Phone       string      `json:"phone"       description:"手机号"`
	OrgId       string      `json:"orgId"       description:"所属组织ID"`
	PositionId  string      `json:"positionId"  description:"所属职位ID"`
	GroupId     string      `json:"groupId"     description:"所属用户组ID"`
	Status      string      `json:"status"      description:"状态"`
	LastLoginAt *gtime.Time `json:"lastLoginAt" description:"最后登录时间"`
	LastLoginIp string      `json:"lastLoginIp" description:"最后登录IP"`
	LoginCount  int         `json:"loginCount"  description:"登录次数"`
	CreatedAt   *gtime.Time `json:"createdAt"   description:"创建时间"`
	CreatedBy   string      `json:"createdBy"   description:"创建用户"`
	UpdatedAt   *gtime.Time `json:"updatedAt"   description:"更新时间"`
	UpdatedBy   string      `json:"updatedBy"   description:"更新用户"`
}

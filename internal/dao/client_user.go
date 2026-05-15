package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var ClientUser = clientUserDao{}

type clientUserDao struct {
	Table   string
	Columns clientUserColumns
}

type clientUserColumns struct {
	Id          string
	Account     string
	Password    string
	Nickname    string
	Avatar      string
	Motto       string
	Gender      string
	Birthday    string
	Email       string
	Github      string
	Phone       string
	OrgId       string
	PositionId  string
	GroupId     string
	Status      string
	LastLoginAt string
	LastLoginIp string
	LoginCount  string
	CreatedAt   string
	CreatedBy   string
	UpdatedAt   string
	UpdatedBy   string
}

func init() {
	ClientUser.Table = "client_user"
	ClientUser.Columns = clientUserColumns{
		Id:          "id",
		Account:     "account",
		Password:    "password",
		Nickname:    "nickname",
		Avatar:      "avatar",
		Motto:       "motto",
		Gender:      "gender",
		Birthday:    "birthday",
		Email:       "email",
		Github:      "github",
		Phone:       "phone",
		OrgId:       "org_id",
		PositionId:  "position_id",
		GroupId:     "group_id",
		Status:      "status",
		LastLoginAt: "last_login_at",
		LastLoginIp: "last_login_ip",
		LoginCount:  "login_count",
		CreatedAt:   "created_at",
		CreatedBy:   "created_by",
		UpdatedAt:   "updated_at",
		UpdatedBy:   "updated_by",
	}
}

func (d clientUserDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

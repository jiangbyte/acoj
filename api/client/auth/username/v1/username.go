package v1

import (
	"github.com/gogf/gf/v2/frame/g"
)

type LoginReq struct {
	g.Meta      `path:"/api/v1/public/c/login" method:"post" summary:"C端用户名密码登录" tags:"C端认证"`
	Username    string `json:"username" v:"required#用户名不能为空"`
	Password    string `json:"password" v:"required#密码不能为空"`
	CaptchaCode string `json:"captcha_code"`
	CaptchaId   string `json:"captcha_id"`
	DeviceId    string `json:"device_id"`
}

type LoginRes struct {
	Token string `json:"token"`
}

type RegisterReq struct {
	g.Meta      `path:"/api/v1/public/c/register" method:"post" summary:"C端用户注册" tags:"C端认证"`
	Username    string `json:"username" v:"required#用户名不能为空"`
	Password    string `json:"password" v:"required#密码不能为空"`
	CaptchaCode string `json:"captcha_code"`
	CaptchaId   string `json:"captcha_id"`
}

type RegisterRes struct {
	Message string `json:"message"`
}

type LogoutReq struct {
	g.Meta `path:"/api/v1/c/logout" method:"post" summary:"C端用户登出" tags:"C端认证"`
}

type LogoutRes struct {
	Message string `json:"message"`
}

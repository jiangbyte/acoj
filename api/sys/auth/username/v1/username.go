package v1

import (
	"github.com/gogf/gf/v2/frame/g"
)

type LoginReq struct {
	g.Meta      `path:"/api/v1/public/b/login" method:"post" summary:"B端用户名密码登录" tags:"B端认证"`
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
	g.Meta      `path:"/api/v1/public/b/register" method:"post" summary:"B端用户注册" tags:"B端认证"`
	Username    string `json:"username" v:"required#用户名不能为空"`
	Password    string `json:"password" v:"required#密码不能为空"`
	CaptchaCode string `json:"captcha_code"`
	CaptchaId   string `json:"captcha_id"`
}

type RegisterRes struct {
	Message string `json:"message"`
}

type LogoutReq struct {
	g.Meta `path:"/api/v1/b/logout" method:"post" summary:"B端用户登出" tags:"B端认证"`
}

type LogoutRes struct {
	Message string `json:"message"`
}

package v1

import (
	"github.com/gogf/gf/v2/frame/g"
)

type CaptchaReq struct {
	g.Meta `path:"/api/v1/public/c/captcha" method:"get" summary:"获取C端验证码" tags:"C端认证"`
}

type CaptchaRes struct {
	CaptchaBase64 string `json:"captcha_base64"`
	CaptchaId     string `json:"captcha_id"`
	CaptchaCode   string `json:"captcha_code,omitempty"`
}

package v1

import (
	"context"

	api "hei-goframe/api/client/auth/captcha/v1"
	"hei-goframe/internal/service/captcha"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Captcha(ctx context.Context, req *api.CaptchaReq) (res *api.CaptchaRes, err error) {
	id, b64, err := captcha.GenerateCaptcha(captcha.ConsumerCaptcha)
	if err != nil {
		return nil, err
	}
	res = &api.CaptchaRes{
		CaptchaBase64: b64,
		CaptchaId:     id,
		CaptchaCode:   captcha.GetDebugCode(captcha.ConsumerCaptcha, id),
	}
	return
}

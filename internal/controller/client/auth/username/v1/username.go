package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	api "hei-goframe/api/client/auth/username/v1"
	"hei-goframe/internal/service/auth"
	service "hei-goframe/internal/service/client/auth"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Login(ctx context.Context, req *api.LoginReq) (res *api.LoginRes, err error) {
	r := g.RequestFromCtx(ctx)
	ip := r.GetClientIp()
	userAgent := r.Header.Get("User-Agent")

	token, err := service.Login(ctx, req.Username, req.Password, req.CaptchaCode, req.CaptchaId, req.DeviceId, ip, userAgent)
	if err != nil {
		return nil, err
	}
	return &api.LoginRes{Token: token}, nil
}

func (c *ControllerV1) Register(ctx context.Context, req *api.RegisterReq) (res *api.RegisterRes, err error) {
	err = service.Register(ctx, req.Username, req.Password, req.CaptchaCode, req.CaptchaId)
	if err != nil {
		return nil, err
	}
	return &api.RegisterRes{Message: "注册成功"}, nil
}

func (c *ControllerV1) Logout(ctx context.Context, req *api.LogoutReq) (res *api.LogoutRes, err error) {
	r := g.RequestFromCtx(ctx)
	tokenStr := r.Header.Get(auth.ConsumerAuth.GetTokenName())

	err = service.Logout(ctx, tokenStr)
	if err != nil {
		return nil, err
	}
	return &api.LogoutRes{Message: "登出成功"}, nil
}

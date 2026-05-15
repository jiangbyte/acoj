package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	api "hei-goframe/api/public/sm2/v1"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) PublicKey(ctx context.Context, req *api.PublicKeyReq) (res *api.PublicKeyRes, err error) {
	key := g.Cfg().MustGet(ctx, "hei.sm2.publicKey").String()
	r := g.RequestFromCtx(ctx)
	r.Response.WriteJson(utility.Success(key))
	return
}

func (c *ControllerV1) ConsumerPublicKey(ctx context.Context, req *api.ConsumerPublicKeyReq) (res *api.ConsumerPublicKeyRes, err error) {
	key := g.Cfg().MustGet(ctx, "hei.sm2.publicKey").String()
	r := g.RequestFromCtx(ctx)
	r.Response.WriteJson(utility.Success(key))
	return
}

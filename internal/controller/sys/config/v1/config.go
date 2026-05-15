package v1

import (
	"context"

	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/config/v1"
	configService "hei-goframe/internal/service/sys/config"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.ConfigPageReq) (res *api.ConfigPageRes, err error) {
	result, err := configService.Page(ctx, req.Keyword, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.ConfigPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.ConfigCreateReq) (res *api.ConfigCreateRes, err error) {
	err = configService.Create(ctx, req.ConfigKey, req.ConfigValue, req.Category, req.Remark, req.SortCode, req.Extra)
	if err != nil {
		return nil, err
	}
	return &api.ConfigCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.ConfigModifyReq) (res *api.ConfigModifyRes, err error) {
	err = configService.Modify(ctx, req.Id, req.ConfigKey, req.ConfigValue, req.Category, req.Remark, req.SortCode, req.Extra)
	if err != nil {
		return nil, err
	}
	return &api.ConfigModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.ConfigRemoveReq) (res *api.ConfigRemoveRes, err error) {
	err = configService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.ConfigRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.ConfigDetailReq) (res *api.ConfigDetailRes, err error) {
	data, err := configService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.ConfigDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

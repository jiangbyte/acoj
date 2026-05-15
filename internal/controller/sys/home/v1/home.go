package v1

import (
	"context"

	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/home/v1"
	homeService "hei-goframe/internal/service/sys/home"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) GetHome(ctx context.Context, req *api.GetHomeReq) (res *api.GetHomeRes, err error) {
	data, err := homeService.GetHome(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.GetHomeRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) AddQuickAction(ctx context.Context, req *api.AddQuickActionReq) (res *api.AddQuickActionRes, err error) {
	err = homeService.AddQuickAction(ctx, req.ResourceId)
	if err != nil {
		return nil, err
	}
	return &api.AddQuickActionRes{}, nil
}

func (c *ControllerV1) RemoveQuickAction(ctx context.Context, req *api.RemoveQuickActionReq) (res *api.RemoveQuickActionRes, err error) {
	err = homeService.RemoveQuickAction(ctx, req.Id)
	if err != nil {
		return nil, err
	}
	return &api.RemoveQuickActionRes{}, nil
}

func (c *ControllerV1) SortQuickAction(ctx context.Context, req *api.SortQuickActionReq) (res *api.SortQuickActionRes, err error) {
	err = homeService.SortQuickAction(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.SortQuickActionRes{}, nil
}

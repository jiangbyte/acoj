package v1

import (
	"context"

	api "hei-goframe/api/sys/home/v1"
	homeService "hei-goframe/internal/service/sys/home"
	"hei-goframe/internal/service/sys/log"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) GetHome(ctx context.Context, req *api.GetHomeReq) (res *api.GetHomeRes, err error) {
	return homeService.GetHome(ctx)
}

func (c *ControllerV1) AddQuickAction(ctx context.Context, req *api.AddQuickActionReq) (res *api.AddQuickActionRes, err error) {
	defer log.SysLog(ctx, "添加快捷方式")()
	err = homeService.AddQuickAction(ctx, req.ResourceId)
	if err != nil {
		return nil, err
	}
	return &api.AddQuickActionRes{}, nil
}

func (c *ControllerV1) RemoveQuickAction(ctx context.Context, req *api.RemoveQuickActionReq) (res *api.RemoveQuickActionRes, err error) {
	defer log.SysLog(ctx, "移除快捷方式")()
	err = homeService.RemoveQuickAction(ctx, req.Id)
	if err != nil {
		return nil, err
	}
	return &api.RemoveQuickActionRes{}, nil
}

func (c *ControllerV1) SortQuickAction(ctx context.Context, req *api.SortQuickActionReq) (res *api.SortQuickActionRes, err error) {
	defer log.SysLog(ctx, "排序快捷方式")()
	err = homeService.SortQuickAction(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.SortQuickActionRes{}, nil
}

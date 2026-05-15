package v1

import (
	"context"

	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/position/v1"
	positionService "hei-goframe/internal/service/sys/position"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.PositionPageReq) (res *api.PositionPageRes, err error) {
	result, err := positionService.Page(ctx, req.Keyword, req.Status, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.PositionPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.PositionCreateReq) (res *api.PositionCreateRes, err error) {
	err = positionService.Create(ctx, req.Code, req.Name, req.Category, req.OrgId, req.GroupId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.PositionCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.PositionModifyReq) (res *api.PositionModifyRes, err error) {
	err = positionService.Modify(ctx, req.Id, req.Code, req.Name, req.Category, req.OrgId, req.GroupId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.PositionModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.PositionRemoveReq) (res *api.PositionRemoveRes, err error) {
	err = positionService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.PositionRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.PositionDetailReq) (res *api.PositionDetailRes, err error) {
	data, err := positionService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.PositionDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

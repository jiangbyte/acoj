package v1

import (
	"context"

	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/dict/v1"
	dictService "hei-goframe/internal/service/sys/dict"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.DictPageReq) (res *api.DictPageRes, err error) {
	result, err := dictService.Page(ctx, req.Keyword, req.Status, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.DictPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.DictCreateReq) (res *api.DictCreateRes, err error) {
	err = dictService.Create(ctx, req.Code, req.Label, req.Value, req.Color, req.Category, req.ParentId, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.DictCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.DictModifyReq) (res *api.DictModifyRes, err error) {
	err = dictService.Modify(ctx, req.Id, req.Code, req.Label, req.Value, req.Color, req.Category, req.ParentId, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.DictModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.DictRemoveReq) (res *api.DictRemoveRes, err error) {
	err = dictService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.DictRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.DictDetailReq) (res *api.DictDetailRes, err error) {
	data, err := dictService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.DictDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

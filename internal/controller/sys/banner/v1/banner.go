package v1

import (
	"context"

	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/banner/v1"
	bannerService "hei-goframe/internal/service/sys/banner"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.BannerPageReq) (res *api.BannerPageRes, err error) {
	result, err := bannerService.Page(ctx, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.BannerPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.BannerCreateReq) (res *api.BannerCreateRes, err error) {
	err = bannerService.Create(ctx, req.Title, req.Image, req.Category, req.Type, req.Position, req.Url, req.LinkType, req.Summary, req.Description, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.BannerCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.BannerModifyReq) (res *api.BannerModifyRes, err error) {
	err = bannerService.Modify(ctx, req.Id, req.Title, req.Image, req.Category, req.Type, req.Position, req.Url, req.LinkType, req.Summary, req.Description, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.BannerModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.BannerRemoveReq) (res *api.BannerRemoveRes, err error) {
	err = bannerService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.BannerRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.BannerDetailReq) (res *api.BannerDetailRes, err error) {
	data, err := bannerService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.BannerDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

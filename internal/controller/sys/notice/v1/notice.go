package v1

import (
	"context"

	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/notice/v1"
	noticeService "hei-goframe/internal/service/sys/notice"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.NoticePageReq) (res *api.NoticePageRes, err error) {
	result, err := noticeService.Page(ctx, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.NoticePageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.NoticeCreateReq) (res *api.NoticeCreateRes, err error) {
	err = noticeService.Create(ctx, req.Title, req.Category, req.Type, req.Summary, req.Content, req.Cover, req.Level, req.Position, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.NoticeCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.NoticeModifyReq) (res *api.NoticeModifyRes, err error) {
	err = noticeService.Modify(ctx, req.Id, req.Title, req.Category, req.Type, req.Summary, req.Content, req.Cover, req.Level, req.Position, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.NoticeModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.NoticeRemoveReq) (res *api.NoticeRemoveRes, err error) {
	err = noticeService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.NoticeRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.NoticeDetailReq) (res *api.NoticeDetailRes, err error) {
	data, err := noticeService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.NoticeDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

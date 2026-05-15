package v1

import (
	"context"
	"os"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/os/gfile"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/file/v1"
	fileService "hei-goframe/internal/service/sys/file"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Upload(ctx context.Context, req *api.FileUploadReq) (res *api.FileUploadRes, err error) {
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	data, err := fileService.Upload(ctx, *file, req.Engine)
	if err != nil {
		return nil, err
	}
	res = &api.FileUploadRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) Download(ctx context.Context, req *api.FileDownloadReq) (res *api.FileDownloadRes, err error) {
	data, err := fileService.Download(ctx, req.Id)
	if err != nil {
		return nil, err
	}
	if data == nil {
		return nil, gerror.New("文件不存在")
	}

	r := g.RequestFromCtx(ctx)
	absPath := data["abs_path"].(string)
	if !gfile.Exists(absPath) {
		return nil, os.ErrNotExist
	}

	r.Response.ServeFileDownload(absPath, data["name"].(string))
	return &api.FileDownloadRes{}, nil
}

func (c *ControllerV1) Page(ctx context.Context, req *api.FilePageReq) (res *api.FilePageRes, err error) {
	result, err := fileService.Page(ctx, req.Keyword, req.Engine, req.DateRangeStart, req.DateRangeEnd, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.FilePageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.FileDetailReq) (res *api.FileDetailRes, err error) {
	data, err := fileService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.FileDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.FileRemoveReq) (res *api.FileRemoveRes, err error) {
	err = fileService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.FileRemoveRes{}, nil
}

func (c *ControllerV1) RemoveAbsolute(ctx context.Context, req *api.FileRemoveAbsoluteReq) (res *api.FileRemoveAbsoluteRes, err error) {
	err = fileService.RemoveAbsolute(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.FileRemoveAbsoluteRes{}, nil
}

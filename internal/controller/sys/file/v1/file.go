package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/file/v1"
	"hei-goframe/internal/service/auth"
	fileService "hei-goframe/internal/service/sys/file"
	"hei-goframe/internal/service/sys/log"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Upload(ctx context.Context, req *api.FileUploadReq) (res *api.FileUploadRes, err error) {
	if err := auth.MustPerm(ctx, "sys:file:upload"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "上传文件")()
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
	if err := auth.MustPerm(ctx, "sys:file:download"); err != nil {
		return nil, err
	}
	data, name, err := fileService.Download(ctx, req.Id)
	if err != nil {
		return nil, err
	}
	if data == nil {
		return nil, gerror.New("文件不存在")
	}

	r := g.RequestFromCtx(ctx)
	r.Response.Header().Set("Content-Type", "application/octet-stream")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, url.PathEscape(name)))
	r.Response.Write(data)
	return &api.FileDownloadRes{}, nil
}

func (c *ControllerV1) Page(ctx context.Context, req *api.FilePageReq) (res *api.FilePageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:file:page"); err != nil {
		return nil, err
	}
	result, err := fileService.Page(ctx, req.Keyword, req.Engine, req.DateRangeStart, req.DateRangeEnd, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.FilePageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.FileDetailReq) (res *api.FileDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:file:detail"); err != nil {
		return nil, err
	}
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
	if err := auth.MustPerm(ctx, "sys:file:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除文件")()
	err = fileService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.FileRemoveRes{}, nil
}

func (c *ControllerV1) RemoveAbsolute(ctx context.Context, req *api.FileRemoveAbsoluteReq) (res *api.FileRemoveAbsoluteRes, err error) {
	if err := auth.MustPerm(ctx, "sys:file:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "物理删除文件")()
	err = fileService.RemoveAbsolute(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.FileRemoveAbsoluteRes{}, nil
}

func (c *ControllerV1) Export(ctx context.Context, req *api.FileExportReq) (res *api.FileExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:file:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出文件数据")()
	buffer, err := fileService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("文件数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.FileExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.FileTemplateReq) (res *api.FileTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:file:template"); err != nil {
		return nil, err
	}
	buffer, err := fileService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("文件导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.FileTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.FileImportReq) (res *api.FileImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:file:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入文件数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := fileService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.FileImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

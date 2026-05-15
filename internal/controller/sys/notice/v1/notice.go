package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/notice/v1"
	"hei-goframe/internal/service/auth"
	"hei-goframe/internal/service/sys/log"
	noticeService "hei-goframe/internal/service/sys/notice"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.NoticePageReq) (res *api.NoticePageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:notice:page"); err != nil {
		return nil, err
	}
	result, err := noticeService.Page(ctx, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.NoticePageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.NoticeCreateReq) (res *api.NoticeCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:notice:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加通知")()
	err = noticeService.Create(ctx, req.Title, req.Category, req.Type, req.Summary, req.Content, req.Cover, req.Level, req.Position, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.NoticeCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.NoticeModifyReq) (res *api.NoticeModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:notice:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑通知")()
	err = noticeService.Modify(ctx, req.Id, req.Title, req.Category, req.Type, req.Summary, req.Content, req.Cover, req.Level, req.Position, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.NoticeModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.NoticeRemoveReq) (res *api.NoticeRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:notice:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除通知")()
	err = noticeService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.NoticeRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.NoticeDetailReq) (res *api.NoticeDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:notice:detail"); err != nil {
		return nil, err
	}
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

func (c *ControllerV1) Export(ctx context.Context, req *api.NoticeExportReq) (res *api.NoticeExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:notice:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出通知数据")()
	buffer, err := noticeService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("通知数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.NoticeExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.NoticeTemplateReq) (res *api.NoticeTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:notice:template"); err != nil {
		return nil, err
	}
	buffer, err := noticeService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("通知导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.NoticeTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.NoticeImportReq) (res *api.NoticeImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:notice:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入通知数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := noticeService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.NoticeImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

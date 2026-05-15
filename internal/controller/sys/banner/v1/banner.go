package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/banner/v1"
	"hei-goframe/internal/service/auth"
	bannerService "hei-goframe/internal/service/sys/banner"
	"hei-goframe/internal/service/sys/log"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.BannerPageReq) (res *api.BannerPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:banner:page"); err != nil {
		return nil, err
	}
	result, err := bannerService.Page(ctx, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.BannerPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.BannerCreateReq) (res *api.BannerCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:banner:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加Banner")()
	err = bannerService.Create(ctx, req.Title, req.Image, req.Category, req.Type, req.Position, req.Url, req.LinkType, req.Summary, req.Description, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.BannerCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.BannerModifyReq) (res *api.BannerModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:banner:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑Banner")()
	err = bannerService.Modify(ctx, req.Id, req.Title, req.Image, req.Category, req.Type, req.Position, req.Url, req.LinkType, req.Summary, req.Description, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.BannerModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.BannerRemoveReq) (res *api.BannerRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:banner:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除Banner")()
	err = bannerService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.BannerRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.BannerDetailReq) (res *api.BannerDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:banner:detail"); err != nil {
		return nil, err
	}
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

func (c *ControllerV1) Export(ctx context.Context, req *api.BannerExportReq) (res *api.BannerExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:banner:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出Banner数据")()
	buffer, err := bannerService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("Banner数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.BannerExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.BannerTemplateReq) (res *api.BannerTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:banner:template"); err != nil {
		return nil, err
	}
	buffer, err := bannerService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("Banner导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.BannerTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.BannerImportReq) (res *api.BannerImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:banner:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入Banner数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := bannerService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.BannerImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

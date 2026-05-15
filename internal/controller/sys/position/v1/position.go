package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/position/v1"
	"hei-goframe/internal/service/auth"
	"hei-goframe/internal/service/sys/log"
	positionService "hei-goframe/internal/service/sys/position"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.PositionPageReq) (res *api.PositionPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:position:page"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "查看职位列表")()
	result, err := positionService.Page(ctx, req.Keyword, req.Status, req.GroupId, req.OrgId, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.PositionPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.PositionCreateReq) (res *api.PositionCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:position:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加职位")()
	err = positionService.Create(ctx, req.Code, req.Name, req.Category, req.OrgId, req.GroupId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.PositionCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.PositionModifyReq) (res *api.PositionModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:position:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑职位")()
	err = positionService.Modify(ctx, req.Id, req.Code, req.Name, req.Category, req.OrgId, req.GroupId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.PositionModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.PositionRemoveReq) (res *api.PositionRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:position:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除职位")()
	err = positionService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.PositionRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.PositionDetailReq) (res *api.PositionDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:position:detail"); err != nil {
		return nil, err
	}
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

func (c *ControllerV1) Export(ctx context.Context, req *api.PositionExportReq) (res *api.PositionExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:position:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出职位数据")()
	buffer, err := positionService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("职位数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.PositionExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.PositionTemplateReq) (res *api.PositionTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:position:template"); err != nil {
		return nil, err
	}
	buffer, err := positionService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("职位导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.PositionTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.PositionImportReq) (res *api.PositionImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:position:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入职位数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := positionService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.PositionImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

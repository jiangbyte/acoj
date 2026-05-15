package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/config/v1"
	"hei-goframe/internal/service/auth"
	configService "hei-goframe/internal/service/sys/config"
	"hei-goframe/internal/service/sys/log"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.ConfigPageReq) (res *api.ConfigPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:page"); err != nil {
		return nil, err
	}
	result, err := configService.Page(ctx, req.Keyword, req.Category, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.ConfigPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.ConfigCreateReq) (res *api.ConfigCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加配置")()
	err = configService.Create(ctx, req.ConfigKey, req.ConfigValue, req.Category, req.Remark, req.SortCode, req.ExtJson)
	if err != nil {
		return nil, err
	}
	return &api.ConfigCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.ConfigModifyReq) (res *api.ConfigModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑配置")()
	err = configService.Modify(ctx, req.Id, req.ConfigKey, req.ConfigValue, req.Category, req.Remark, req.SortCode, req.ExtJson)
	if err != nil {
		return nil, err
	}
	return &api.ConfigModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.ConfigRemoveReq) (res *api.ConfigRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除配置")()
	err = configService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.ConfigRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.ConfigDetailReq) (res *api.ConfigDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:detail"); err != nil {
		return nil, err
	}
	data, err := configService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.ConfigDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) ListByCategory(ctx context.Context, req *api.ConfigListByCategoryReq) (res *api.ConfigListByCategoryRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:list"); err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	list, err := configService.ListByCategory(ctx, req.Category)
	if err != nil {
		return nil, err
	}
	r.Response.WriteJson(utility.SuccessWithCtx(ctx, list))
	return
}

func (c *ControllerV1) BatchEdit(ctx context.Context, req *api.ConfigBatchEditReq) (res *api.ConfigBatchEditRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:edit"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "批量编辑配置")()
	items := make([]g.Map, len(req.Configs))
	for i, item := range req.Configs {
		items[i] = g.Map{
			"id":           item.Id,
			"config_key":   item.ConfigKey,
			"config_value": item.ConfigValue,
			"sort_code":    item.SortCode,
			"remark":       item.Remark,
			"ext_json":     item.ExtJson,
		}
	}
	err = configService.BatchEdit(ctx, items)
	if err != nil {
		return nil, err
	}
	return &api.ConfigBatchEditRes{}, nil
}

func (c *ControllerV1) CategoryEdit(ctx context.Context, req *api.ConfigCategoryEditReq) (res *api.ConfigCategoryEditRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:edit"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "按分类批量编辑配置")()
	catItems := make([]g.Map, len(req.Configs))
	for i, item := range req.Configs {
		catItems[i] = g.Map{
			"config_key":   item.ConfigKey,
			"config_value": item.ConfigValue,
		}
	}
	err = configService.CategoryEdit(ctx, req.Category, catItems)
	if err != nil {
		return nil, err
	}
	return &api.ConfigCategoryEditRes{}, nil
}

func (c *ControllerV1) Export(ctx context.Context, req *api.ConfigExportReq) (res *api.ConfigExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出配置数据")()
	buffer, err := configService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("配置数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.ConfigExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.ConfigTemplateReq) (res *api.ConfigTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:template"); err != nil {
		return nil, err
	}
	buffer, err := configService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("配置导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.ConfigTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.ConfigImportReq) (res *api.ConfigImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:config:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入配置数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := configService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.ConfigImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

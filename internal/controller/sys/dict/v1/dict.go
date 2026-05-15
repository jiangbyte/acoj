package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/dict/v1"
	"hei-goframe/internal/service/auth"
	dictService "hei-goframe/internal/service/sys/dict"
	"hei-goframe/internal/service/sys/log"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.DictPageReq) (res *api.DictPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:page"); err != nil {
		return nil, err
	}
	result, err := dictService.Page(ctx, req.Keyword, req.ParentId, req.Category, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.DictPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.DictCreateReq) (res *api.DictCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加字典")()
	err = dictService.Create(ctx, req.Code, req.Label, req.Value, req.Color, req.Category, req.ParentId, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.DictCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.DictModifyReq) (res *api.DictModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑字典")()
	err = dictService.Modify(ctx, req.Id, req.Code, req.Label, req.Value, req.Color, req.Category, req.ParentId, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.DictModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.DictRemoveReq) (res *api.DictRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除字典")()
	err = dictService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.DictRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.DictDetailReq) (res *api.DictDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:detail"); err != nil {
		return nil, err
	}
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

func (c *ControllerV1) List(ctx context.Context, req *api.DictListReq) (res *api.DictListRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:list"); err != nil {
		return nil, err
	}
	list, err := dictService.List(ctx, req.ParentId, req.Category)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	r.Response.WriteJson(utility.Success(list))
	return
}

func (c *ControllerV1) Tree(ctx context.Context, req *api.DictTreeReq) (res *api.DictTreeRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:tree"); err != nil {
		return nil, err
	}
	treeList, err := dictService.Tree(ctx, req.Category, req.Status)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	r.Response.WriteJson(utility.Success(treeList))
	return
}

func (c *ControllerV1) GetLabel(ctx context.Context, req *api.DictGetLabelReq) (res *api.DictGetLabelRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:get-label"); err != nil {
		return nil, err
	}
	label, err := dictService.GetLabel(ctx, req.TypeCode, req.Value)
	if err != nil {
		return nil, err
	}
	return &api.DictGetLabelRes{
		TypeCode: req.TypeCode,
		Value:    req.Value,
		Label:    label,
	}, nil
}

func (c *ControllerV1) GetChildren(ctx context.Context, req *api.DictGetChildrenReq) (res *api.DictGetChildrenRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:get-children"); err != nil {
		return nil, err
	}
	list, err := dictService.GetChildren(ctx, req.TypeCode)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	r.Response.WriteJson(utility.Success(list))
	return
}

func (c *ControllerV1) Export(ctx context.Context, req *api.DictExportReq) (res *api.DictExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出字典数据")()
	buffer, err := dictService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("字典数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.DictExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.DictTemplateReq) (res *api.DictTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:template"); err != nil {
		return nil, err
	}
	buffer, err := dictService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("字典导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.DictTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.DictImportReq) (res *api.DictImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:dict:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入字典数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := dictService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.DictImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

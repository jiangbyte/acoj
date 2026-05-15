package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/resource/v1"
	"hei-goframe/internal/service/auth"
	"hei-goframe/internal/service/sys/log"
	resourceService "hei-goframe/internal/service/sys/resource"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Tree(ctx context.Context, req *api.ResourceTreeReq) (res *api.ResourceTreeRes, err error) {
	if err := auth.MustPerm(ctx, "sys:resource:tree"); err != nil {
		return nil, err
	}
	treeData, err := resourceService.Tree(ctx)
	if err != nil {
		return nil, err
	}
	nodes := make([]*api.ResourceTreeNode, 0)
	for _, item := range treeData {
		nodes = append(nodes, mapToTreeNode(item))
	}
	result := api.ResourceTreeRes(nodes)
	return &result, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.ResourceDetailReq) (res *api.ResourceDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:resource:detail"); err != nil {
		return nil, err
	}
	data, err := resourceService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.ResourceDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.ResourceCreateReq) (res *api.ResourceCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:resource:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加资源")()
	err = resourceService.Create(ctx,
		req.Code, req.Name, req.Category, req.Type, req.Description,
		req.ParentId, req.RoutePath, req.ComponentPath, req.RedirectPath,
		req.Icon, req.Color, req.IsVisible, req.IsCache, req.IsAffix, req.IsBreadcrumb,
		req.ExternalUrl, req.Extra, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.ResourceCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.ResourceModifyReq) (res *api.ResourceModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:resource:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑资源")()
	err = resourceService.Modify(ctx, req.Id,
		req.Code, req.Name, req.Category, req.Type, req.Description,
		req.ParentId, req.RoutePath, req.ComponentPath, req.RedirectPath,
		req.Icon, req.Color, req.IsVisible, req.IsCache, req.IsAffix, req.IsBreadcrumb,
		req.ExternalUrl, req.Extra, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.ResourceModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.ResourceRemoveReq) (res *api.ResourceRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:resource:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除资源")()
	err = resourceService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.ResourceRemoveRes{}, nil
}

func (c *ControllerV1) Export(ctx context.Context, req *api.ResourceExportReq) (res *api.ResourceExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:resource:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出资源数据")()
	buffer, err := resourceService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("资源数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.ResourceExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.ResourceTemplateReq) (res *api.ResourceTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:resource:template"); err != nil {
		return nil, err
	}
	buffer, err := resourceService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("资源导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.ResourceTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.ResourceImportReq) (res *api.ResourceImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:resource:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入资源数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := resourceService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.ResourceImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

func mapToTreeNode(m g.Map) *api.ResourceTreeNode {
	node := &api.ResourceTreeNode{
		Id:            m["id"].(string),
		Code:          m["code"].(string),
		Name:          m["name"].(string),
		Category:      m["category"].(string),
		Type:          m["type"].(string),
		Description:   m["description"].(string),
		ParentId:      m["parent_id"].(string),
		RoutePath:     m["route_path"].(string),
		ComponentPath: m["component_path"].(string),
		RedirectPath:  m["redirect_path"].(string),
		Icon:          m["icon"].(string),
		Color:         m["color"].(string),
		IsVisible:     m["is_visible"].(string),
		IsCache:       m["is_cache"].(string),
		IsAffix:       m["is_affix"].(string),
		IsBreadcrumb:  m["is_breadcrumb"].(string),
		ExternalUrl:   m["external_url"].(string),
		Extra:         m["extra"].(string),
		Status:        m["status"].(string),
		SortCode:      m["sort_code"].(int),
	}
	if children, ok := m["children"].([]g.Map); ok {
		for _, child := range children {
			node.Children = append(node.Children, mapToTreeNode(child))
		}
	}
	return node
}

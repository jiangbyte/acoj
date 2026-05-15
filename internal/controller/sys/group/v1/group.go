package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/group/v1"
	"hei-goframe/internal/service/auth"
	groupService "hei-goframe/internal/service/sys/group"
	"hei-goframe/internal/service/sys/log"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.GroupPageReq) (res *api.GroupPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:page"); err != nil {
		return nil, err
	}
	result, err := groupService.Page(ctx, req.Keyword, req.Status, req.ParentId, req.OrgId, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.GroupPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.GroupCreateReq) (res *api.GroupCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加用户组")()
	err = groupService.Create(ctx, req.Code, req.Name, req.Category, req.ParentId, req.OrgId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.GroupCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.GroupModifyReq) (res *api.GroupModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑用户组")()
	err = groupService.Modify(ctx, req.Id, req.Code, req.Name, req.Category, req.ParentId, req.OrgId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.GroupModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.GroupRemoveReq) (res *api.GroupRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除用户组")()
	err = groupService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.GroupRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.GroupDetailReq) (res *api.GroupDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:detail"); err != nil {
		return nil, err
	}
	data, err := groupService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.GroupDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) Tree(ctx context.Context, req *api.GroupTreeReq) (res *api.GroupTreeRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:tree"); err != nil {
		return nil, err
	}
	treeList, err := groupService.Tree(ctx, req.OrgId, req.Keyword)
	if err != nil {
		return nil, err
	}
	nodes := make([]*api.GroupTreeNode, 0)
	for _, item := range treeList {
		nodes = append(nodes, groupMapToNode(item))
	}
	result := api.GroupTreeRes(nodes)
	return &result, nil
}

func (c *ControllerV1) UnionTree(ctx context.Context, req *api.GroupUnionTreeReq) (res *api.GroupUnionTreeRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:tree"); err != nil {
		return nil, err
	}
	treeList, err := groupService.UnionTree(ctx)
	if err != nil {
		return nil, err
	}
	nodes := make([]*api.UnionGroupTreeNode, 0)
	for _, item := range treeList {
		nodes = append(nodes, unionMapToNode(item))
	}
	result := api.GroupUnionTreeRes(nodes)
	return &result, nil
}

func (c *ControllerV1) Export(ctx context.Context, req *api.GroupExportReq) (res *api.GroupExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出用户组数据")()
	buffer, err := groupService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("用户组数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.GroupExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.GroupTemplateReq) (res *api.GroupTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:template"); err != nil {
		return nil, err
	}
	buffer, err := groupService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("用户组导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.GroupTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.GroupImportReq) (res *api.GroupImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:group:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入用户组数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := groupService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.GroupImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

func groupMapToNode(m g.Map) *api.GroupTreeNode {
	node := &api.GroupTreeNode{
		Id:          m["id"].(string),
		Code:        m["code"].(string),
		Name:        m["name"].(string),
		Category:    m["category"].(string),
		ParentId:    m["parent_id"].(string),
		OrgId:       m["org_id"].(string),
		Description: m["description"].(string),
		Status:      m["status"].(string),
		SortCode:    m["sort_code"].(int),
	}
	if children, ok := m["children"].([]g.Map); ok {
		for _, child := range children {
			node.Children = append(node.Children, groupMapToNode(child))
		}
	}
	return node
}

func unionMapToNode(m g.Map) *api.UnionGroupTreeNode {
	node := &api.UnionGroupTreeNode{
		Id:          m["id"].(string),
		Code:        m["code"].(string),
		Name:        m["name"].(string),
		Category:    m["category"].(string),
		ParentId:    m["parent_id"].(string),
		OrgId:       m["org_id"].(string),
		Type:        m["type"].(string),
		Description: m["description"].(string),
		Status:      m["status"].(string),
		SortCode:    m["sort_code"].(int),
	}
	if children, ok := m["children"].([]g.Map); ok {
		for _, child := range children {
			node.Children = append(node.Children, unionMapToNode(child))
		}
	}
	return node
}

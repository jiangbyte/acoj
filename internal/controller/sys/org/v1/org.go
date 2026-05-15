package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/org/v1"
	"hei-goframe/internal/service/auth"
	"hei-goframe/internal/service/sys/log"
	orgService "hei-goframe/internal/service/sys/org"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.OrgPageReq) (res *api.OrgPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:page"); err != nil {
		return nil, err
	}
	result, err := orgService.Page(ctx, req.Keyword, req.Status, req.ParentId, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.OrgPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.OrgCreateReq) (res *api.OrgCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加组织")()
	err = orgService.Create(ctx, req.Code, req.Name, req.Category, req.ParentId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.OrgCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.OrgModifyReq) (res *api.OrgModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑组织")()
	err = orgService.Modify(ctx, req.Id, req.Code, req.Name, req.Category, req.ParentId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.OrgModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.OrgRemoveReq) (res *api.OrgRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除组织")()
	err = orgService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.OrgRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.OrgDetailReq) (res *api.OrgDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:detail"); err != nil {
		return nil, err
	}
	data, err := orgService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.OrgDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) Tree(ctx context.Context, req *api.OrgTreeReq) (res *api.OrgTreeRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:tree"); err != nil {
		return nil, err
	}
	treeList, err := orgService.Tree(ctx, req.Category)
	if err != nil {
		return nil, err
	}
	nodes := make([]*api.OrgTreeNode, 0)
	for _, item := range treeList {
		nodes = append(nodes, orgMapToNode(item))
	}
	result := api.OrgTreeRes(nodes)
	return &result, nil
}

func (c *ControllerV1) Export(ctx context.Context, req *api.OrgExportReq) (res *api.OrgExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出组织数据")()
	buffer, err := orgService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("组织数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.OrgExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.OrgTemplateReq) (res *api.OrgTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:template"); err != nil {
		return nil, err
	}
	buffer, err := orgService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("组织导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.OrgTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.OrgImportReq) (res *api.OrgImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入组织数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := orgService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.OrgImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) GrantOrgRole(ctx context.Context, req *api.GrantOrgRoleReq) (res *api.GrantOrgRoleRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:grant-role"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "分配组织角色")()
	err = orgService.GrantOrgRole(ctx, req.OrgId, req.RoleIds, req.Scope, req.CustomScopeGroupIds, req.CustomScopeOrgIds)
	if err != nil {
		return nil, err
	}
	return &api.GrantOrgRoleRes{}, nil
}

func (c *ControllerV1) OwnRoles(ctx context.Context, req *api.OrgOwnRolesReq) (res *api.OrgOwnRolesRes, err error) {
	if err := auth.MustPerm(ctx, "sys:org:own-roles"); err != nil {
		return nil, err
	}
	roleIds, err := orgService.GetOrgRoleIds(ctx, req.OrgId)
	if err != nil {
		return nil, err
	}
	result := api.OrgOwnRolesRes(roleIds)
	return &result, nil
}

func orgMapToNode(m g.Map) *api.OrgTreeNode {
	node := &api.OrgTreeNode{
		Id:          m["id"].(string),
		Code:        m["code"].(string),
		Name:        m["name"].(string),
		Category:    m["category"].(string),
		ParentId:    m["parent_id"].(string),
		Description: m["description"].(string),
		Status:      m["status"].(string),
		SortCode:    m["sort_code"].(int),
	}
	if children, ok := m["children"].([]g.Map); ok {
		for _, child := range children {
			node.Children = append(node.Children, orgMapToNode(child))
		}
	}
	return node
}

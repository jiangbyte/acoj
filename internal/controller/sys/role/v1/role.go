package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/role/v1"
	"hei-goframe/internal/service/auth"
	"hei-goframe/internal/service/sys/log"
	roleService "hei-goframe/internal/service/sys/role"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.RolePageReq) (res *api.RolePageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:page"); err != nil {
		return nil, err
	}
	result, err := roleService.Page(ctx, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.RolePageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.RoleCreateReq) (res *api.RoleCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:create"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "添加角色")()
	err = roleService.Create(ctx, req.Code, req.Name, req.Category, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.RoleCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.RoleModifyReq) (res *api.RoleModifyRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:modify"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "编辑角色")()
	err = roleService.Modify(ctx, req.Id, req.Code, req.Name, req.Category, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.RoleModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.RoleRemoveReq) (res *api.RoleRemoveRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除角色")()
	err = roleService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.RoleRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.RoleDetailReq) (res *api.RoleDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:detail"); err != nil {
		return nil, err
	}
	data, err := roleService.Detail(ctx, req.Id)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.RoleDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) GrantPermission(ctx context.Context, req *api.GrantPermissionReq) (res *api.GrantPermissionRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:grant-permission"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "分配角色权限")()
	perms := make([]map[string]interface{}, len(req.Permissions))
	for i, p := range req.Permissions {
		perms[i] = map[string]interface{}{
			"permission_code":        p.PermissionCode,
			"scope":                  p.Scope,
			"custom_scope_group_ids": p.CustomScopeGroupIds,
			"custom_scope_org_ids":   p.CustomScopeOrgIds,
		}
	}
	err = roleService.GrantPermissions(ctx, req.RoleId, perms)
	if err != nil {
		return nil, err
	}
	return &api.GrantPermissionRes{}, nil
}

func (c *ControllerV1) GrantResource(ctx context.Context, req *api.GrantResourceReq) (res *api.GrantResourceRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:grant-resource"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 3000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "分配角色资源")()
	perms := make([]map[string]interface{}, len(req.Permissions))
	for i, p := range req.Permissions {
		perms[i] = map[string]interface{}{
			"permission_code":        p.PermissionCode,
			"scope":                  p.Scope,
			"custom_scope_group_ids": p.CustomScopeGroupIds,
			"custom_scope_org_ids":   p.CustomScopeOrgIds,
		}
	}
	err = roleService.GrantResources(ctx, req.RoleId, req.ResourceIds, perms)
	if err != nil {
		return nil, err
	}
	return &api.GrantResourceRes{}, nil
}

func (c *ControllerV1) OwnPermission(ctx context.Context, req *api.OwnPermissionReq) (res *api.OwnPermissionRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:own-permission"); err != nil {
		return nil, err
	}
	codes, err := roleService.GetPermissionCodes(ctx, req.RoleId)
	if err != nil {
		return nil, err
	}
	result := api.OwnPermissionRes(codes)
	return &result, nil
}

func (c *ControllerV1) OwnPermissionDetail(ctx context.Context, req *api.OwnPermissionDetailReq) (res *api.OwnPermissionDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:own-permission-detail"); err != nil {
		return nil, err
	}
	list, err := roleService.GetPermissionDetails(ctx, req.RoleId)
	if err != nil {
		return nil, err
	}
	items := make([]api.PermissionDetailItem, 0)
	for _, item := range list {
		items = append(items, api.PermissionDetailItem{
			PermissionCode:      item["permission_code"].(string),
			Scope:               item["scope"].(string),
			CustomScopeGroupIds: item["custom_scope_group_ids"].(string),
			CustomScopeOrgIds:   item["custom_scope_org_ids"].(string),
		})
	}
	result := api.OwnPermissionDetailRes(items)
	return &result, nil
}

func (c *ControllerV1) OwnResource(ctx context.Context, req *api.OwnResourceReq) (res *api.OwnResourceRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:own-resource"); err != nil {
		return nil, err
	}
	ids, err := roleService.GetResourceIds(ctx, req.RoleId)
	if err != nil {
		return nil, err
	}
	result := api.OwnResourceRes(ids)
	return &result, nil
}

func (c *ControllerV1) Export(ctx context.Context, req *api.RoleExportReq) (res *api.RoleExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出角色数据")()
	buffer, err := roleService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("角色数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.RoleExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.RoleTemplateReq) (res *api.RoleTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:template"); err != nil {
		return nil, err
	}
	buffer, err := roleService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("角色导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.RoleTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.RoleImportReq) (res *api.RoleImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:role:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入角色数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := roleService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.RoleImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

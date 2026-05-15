package v1

import (
	"context"

	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/role/v1"
	roleService "hei-goframe/internal/service/sys/role"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.RolePageReq) (res *api.RolePageRes, err error) {
	result, err := roleService.Page(ctx, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.RolePageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.RoleCreateReq) (res *api.RoleCreateRes, err error) {
	err = roleService.Create(ctx, req.Code, req.Name, req.Category, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.RoleCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.RoleModifyReq) (res *api.RoleModifyRes, err error) {
	err = roleService.Modify(ctx, req.Id, req.Code, req.Name, req.Category, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.RoleModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.RoleRemoveReq) (res *api.RoleRemoveRes, err error) {
	err = roleService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.RoleRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.RoleDetailReq) (res *api.RoleDetailRes, err error) {
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
	err = roleService.GrantResources(ctx, req.RoleId, req.ResourceIds)
	if err != nil {
		return nil, err
	}
	return &api.GrantResourceRes{}, nil
}

func (c *ControllerV1) OwnPermission(ctx context.Context, req *api.OwnPermissionReq) (res *api.OwnPermissionRes, err error) {
	codes, err := roleService.GetPermissionCodes(ctx, req.RoleId)
	if err != nil {
		return nil, err
	}
	return &api.OwnPermissionRes{Codes: codes}, nil
}

func (c *ControllerV1) OwnPermissionDetail(ctx context.Context, req *api.OwnPermissionDetailReq) (res *api.OwnPermissionDetailRes, err error) {
	list, err := roleService.GetPermissionDetails(ctx, req.RoleId)
	if err != nil {
		return nil, err
	}
	res = &api.OwnPermissionDetailRes{List: make([]api.PermissionDetailItem, 0)}
	for _, item := range list {
		res.List = append(res.List, api.PermissionDetailItem{
			PermissionCode:      item["permission_code"].(string),
			Scope:               item["scope"].(string),
			CustomScopeGroupIds: item["custom_scope_group_ids"].(string),
			CustomScopeOrgIds:   item["custom_scope_org_ids"].(string),
		})
	}
	return res, nil
}

func (c *ControllerV1) OwnResource(ctx context.Context, req *api.OwnResourceReq) (res *api.OwnResourceRes, err error) {
	ids, err := roleService.GetResourceIds(ctx, req.RoleId)
	if err != nil {
		return nil, err
	}
	return &api.OwnResourceRes{ResourceIds: ids}, nil
}

package v1

import (
	"context"

	api "hei-goframe/api/sys/permission/v1"
	"hei-goframe/internal/service/auth"
	"hei-goframe/internal/service/sys/log"
	permService "hei-goframe/internal/service/sys/permission"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.PermissionPageReq) (res *api.PermissionPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:permission:page"); err != nil {
		return nil, err
	}
	result, err := permService.Page(ctx, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.PermissionPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Scan(ctx context.Context, req *api.PermissionScanReq) (res *api.PermissionScanRes, err error) {
	if err := auth.MustPerm(ctx, "sys:permission:scan"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "触发权限扫描")()
	err = permService.Scan(ctx)
	if err != nil {
		return nil, err
	}
	return &api.PermissionScanRes{}, nil
}

func (c *ControllerV1) Modules(ctx context.Context, req *api.PermissionModulesReq) (res *api.PermissionModulesRes, err error) {
	if err := auth.MustPerm(ctx, "sys:permission:modules"); err != nil {
		return nil, err
	}
	modules, err := permService.GetModules(ctx)
	if err != nil {
		return nil, err
	}
	result := api.PermissionModulesRes(modules)
	return &result, nil
}

func (c *ControllerV1) ListByModule(ctx context.Context, req *api.PermissionListByModuleReq) (res *api.PermissionListByModuleRes, err error) {
	if err := auth.MustPerm(ctx, "sys:permission:by-module"); err != nil {
		return nil, err
	}
	list, err := permService.GetListByModule(ctx, req.Module)
	if err != nil {
		return nil, err
	}
	items := make([]*api.PermissionItem, 0)
	for _, item := range list {
		items = append(items, &api.PermissionItem{
			Code:     item["code"].(string),
			Module:   item["module"].(string),
			Category: item["category"].(string),
			Name:     item["name"].(string),
		})
	}
	result := api.PermissionListByModuleRes(items)
	return &result, nil
}

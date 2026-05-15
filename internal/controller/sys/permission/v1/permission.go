package v1

import (
	"context"

	api "hei-goframe/api/sys/permission/v1"
	permService "hei-goframe/internal/service/sys/permission"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.PermissionPageReq) (res *api.PermissionPageRes, err error) {
	result, err := permService.Page(ctx, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.PermissionPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Scan(ctx context.Context, req *api.PermissionScanReq) (res *api.PermissionScanRes, err error) {
	err = permService.Scan(ctx)
	if err != nil {
		return nil, err
	}
	return &api.PermissionScanRes{}, nil
}

func (c *ControllerV1) Modules(ctx context.Context, req *api.PermissionModulesReq) (res *api.PermissionModulesRes, err error) {
	modules, err := permService.GetModules(ctx)
	if err != nil {
		return nil, err
	}
	return &api.PermissionModulesRes{Modules: modules}, nil
}

func (c *ControllerV1) ListByModule(ctx context.Context, req *api.PermissionListByModuleReq) (res *api.PermissionListByModuleRes, err error) {
	list, err := permService.GetListByModule(ctx, req.Module)
	if err != nil {
		return nil, err
	}
	res = &api.PermissionListByModuleRes{List: make([]*api.PermissionItem, 0)}
	for _, item := range list {
		res.List = append(res.List, &api.PermissionItem{
			Code:     item["code"].(string),
			Module:   item["module"].(string),
			Category: item["category"].(string),
			Name:     item["name"].(string),
		})
	}
	return res, nil
}

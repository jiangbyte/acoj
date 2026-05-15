package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/resource/v1"
	resourceService "hei-goframe/internal/service/sys/resource"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Tree(ctx context.Context, req *api.ResourceTreeReq) (res *api.ResourceTreeRes, err error) {
	treeData, err := resourceService.Tree(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.ResourceTreeRes{List: make([]*api.ResourceTreeNode, 0)}
	for _, item := range treeData {
		node := mapToTreeNode(item)
		res.List = append(res.List, node)
	}
	return res, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.ResourceDetailReq) (res *api.ResourceDetailRes, err error) {
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
	err = resourceService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.ResourceRemoveRes{}, nil
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

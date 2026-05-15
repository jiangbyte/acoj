package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/group/v1"
	groupService "hei-goframe/internal/service/sys/group"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.GroupPageReq) (res *api.GroupPageRes, err error) {
	result, err := groupService.Page(ctx, req.Keyword, req.Status, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.GroupPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.GroupCreateReq) (res *api.GroupCreateRes, err error) {
	err = groupService.Create(ctx, req.Code, req.Name, req.Category, req.ParentId, req.OrgId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.GroupCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.GroupModifyReq) (res *api.GroupModifyRes, err error) {
	err = groupService.Modify(ctx, req.Id, req.Code, req.Name, req.Category, req.ParentId, req.OrgId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.GroupModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.GroupRemoveReq) (res *api.GroupRemoveRes, err error) {
	err = groupService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.GroupRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.GroupDetailReq) (res *api.GroupDetailRes, err error) {
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
	treeList, err := groupService.Tree(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.GroupTreeRes{List: make([]*api.GroupTreeNode, 0)}
	for _, item := range treeList {
		res.List = append(res.List, groupMapToNode(item))
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

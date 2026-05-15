package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/org/v1"
	orgService "hei-goframe/internal/service/sys/org"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.OrgPageReq) (res *api.OrgPageRes, err error) {
	result, err := orgService.Page(ctx, req.Keyword, req.Status, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.OrgPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.OrgCreateReq) (res *api.OrgCreateRes, err error) {
	err = orgService.Create(ctx, req.Code, req.Name, req.Category, req.ParentId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.OrgCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.OrgModifyReq) (res *api.OrgModifyRes, err error) {
	err = orgService.Modify(ctx, req.Id, req.Code, req.Name, req.Category, req.ParentId, req.Description, req.Status, req.SortCode)
	if err != nil {
		return nil, err
	}
	return &api.OrgModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.OrgRemoveReq) (res *api.OrgRemoveRes, err error) {
	err = orgService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.OrgRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.OrgDetailReq) (res *api.OrgDetailRes, err error) {
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
	treeList, err := orgService.Tree(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.OrgTreeRes{List: make([]*api.OrgTreeNode, 0)}
	for _, item := range treeList {
		res.List = append(res.List, orgMapToNode(item))
	}
	return res, nil
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

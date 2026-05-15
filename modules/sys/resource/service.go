package resource

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/relroleresource"
	"hei-gin/ent/sysresource"
)

type PageParam struct {
	Page    int    `form:"page" json:"page"`
	Size    int    `form:"size" json:"size"`
	Keyword string `form:"keyword" json:"keyword"`
	Type    string `form:"type" json:"type"`
	Status  string `form:"status" json:"status"`
}

type ResourceVO struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Code        string `json:"code"`
	ParentID    string `json:"parent_id"`
	Hierarchy   string `json:"hierarchy"`
	Type        string `json:"type"`
	Category    string `json:"category"`
	Icon        string `json:"icon"`
	Path        string `json:"path"`
	Component   string `json:"component"`
	Permission  string `json:"permission"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Visible     bool   `json:"visible"`
	KeepAlive   bool   `json:"keep_alive"`
	IsFrame     bool   `json:"is_frame"`
	IsCache     bool   `json:"is_cache"`
	IsAffix     bool   `json:"is_affix"`
	Description string `json:"description"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type ResourceCreateReq struct {
	Name        string `json:"name" binding:"required"`
	Code        string `json:"code" binding:"required"`
	ParentID    string `json:"parent_id"`
	Hierarchy   string `json:"hierarchy"`
	Type        string `json:"type"`
	Category    string `json:"category"`
	Icon        string `json:"icon"`
	Path        string `json:"path"`
	Component   string `json:"component"`
	Permission  string `json:"permission"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Visible     *bool  `json:"visible"`
	KeepAlive   *bool  `json:"keep_alive"`
	IsFrame     *bool  `json:"is_frame"`
	IsCache     *bool  `json:"is_cache"`
	IsAffix     *bool  `json:"is_affix"`
	Description string `json:"description"`
}

type ResourceModifyReq struct {
	ID          string `json:"id" binding:"required"`
	Name        string `json:"name"`
	Code        string `json:"code"`
	ParentID    string `json:"parent_id"`
	Hierarchy   string `json:"hierarchy"`
	Type        string `json:"type"`
	Category    string `json:"category"`
	Icon        string `json:"icon"`
	Path        string `json:"path"`
	Component   string `json:"component"`
	Permission  string `json:"permission"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Visible     *bool  `json:"visible"`
	KeepAlive   *bool  `json:"keep_alive"`
	IsFrame     *bool  `json:"is_frame"`
	IsCache     *bool  `json:"is_cache"`
	IsAffix     *bool  `json:"is_affix"`
	Description string `json:"description"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

type TreeSelectVO struct {
	ID       string          `json:"id"`
	Name     string          `json:"name"`
	ParentID string          `json:"parent_id"`
	Children []*TreeSelectVO `json:"children"`
}

type BootstrapMenuVO struct {
	ID        string             `json:"id"`
	Name      string             `json:"name"`
	Icon      string             `json:"icon"`
	Path      string             `json:"path"`
	Component string             `json:"component"`
	Type      string             `json:"type"`
	Children  []*BootstrapMenuVO `json:"children"`
}

var ResourceExportFieldNames = map[string]string{
	"name":       "资源名称",
	"code":       "资源编码",
	"type":       "资源类型",
	"category":   "资源类别",
	"icon":       "图标",
	"path":       "路由路径",
	"component":  "组件路径",
	"sort_code":  "排序",
	"status":     "状态",
	"created_at": "创建时间",
}

var ResourceExportFields = []string{"name", "code", "type", "category", "icon", "path", "component", "sort_code", "status", "created_at"}

func toVO(r *ent.SysResource) ResourceVO {
	vo := ResourceVO{
		ID:          r.ID,
		Name:        r.Name,
		Code:        r.Code,
		ParentID:    r.ParentID,
		Hierarchy:   r.Hierarchy,
		Type:        r.Type,
		Category:    r.Category,
		Icon:        r.Icon,
		Path:        r.Path,
		Component:   r.Component,
		Permission:  r.Permission,
		SortCode:    r.SortCode,
		Status:      r.Status,
		Visible:     r.Visible,
		KeepAlive:   r.KeepAlive,
		IsFrame:     r.IsFrame,
		IsCache:     r.IsCache,
		IsAffix:     r.IsAffix,
		Description: r.Description,
		CreatedAt:   r.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   r.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   r.CreatedBy,
		UpdatedBy:   r.UpdatedBy,
	}
	return vo
}

func Page(page, size int, keyword, resType, status string) (int, []*ent.SysResource, error) {
	ctx := context.Background()
	q := db.Client.SysResource.Query()

	if keyword != "" {
		q = q.Where(
			sysresource.Or(
				sysresource.NameContains(keyword),
				sysresource.CodeContains(keyword),
			),
		)
	}
	if resType != "" {
		q = q.Where(sysresource.TypeEQ(resType))
	}
	if status != "" {
		q = q.Where(sysresource.StatusEQ(status))
	}

	total, err := q.Count(ctx)
	if err != nil {
		return 0, nil, err
	}

	if size <= 0 {
		size = 10
	}
	if page <= 0 {
		page = 1
	}

	items, err := q.
		Order(ent.Desc(sysresource.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Create(req *ResourceCreateReq, loginID string) (*ent.SysResource, error) {
	ctx := context.Background()
	now := time.Now()
	q := db.Client.SysResource.Create().
		SetID(utils.NextID()).
		SetName(req.Name).
		SetCode(req.Code).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)

	if req.ParentID != "" {
		q.SetParentID(req.ParentID)
	}
	if req.Hierarchy != "" {
		q.SetHierarchy(req.Hierarchy)
	}
	if req.Type != "" {
		q.SetType(req.Type)
	} else {
		q.SetType("MENU")
	}
	if req.Category != "" {
		q.SetCategory(req.Category)
	} else {
		q.SetCategory("BACKEND_MENU")
	}
	if req.Icon != "" {
		q.SetIcon(req.Icon)
	}
	if req.Path != "" {
		q.SetPath(req.Path)
	}
	if req.Component != "" {
		q.SetComponent(req.Component)
	}
	if req.Permission != "" {
		q.SetPermission(req.Permission)
	}
	if req.SortCode > 0 {
		q.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		q.SetStatus(req.Status)
	} else {
		q.SetStatus("ENABLED")
	}
	if req.Visible != nil {
		q.SetVisible(*req.Visible)
	} else {
		q.SetVisible(true)
	}
	if req.KeepAlive != nil {
		q.SetKeepAlive(*req.KeepAlive)
	}
	if req.IsFrame != nil {
		q.SetIsFrame(*req.IsFrame)
	}
	if req.IsCache != nil {
		q.SetIsCache(*req.IsCache)
	}
	if req.IsAffix != nil {
		q.SetIsAffix(*req.IsAffix)
	}
	if req.Description != "" {
		q.SetDescription(req.Description)
	}

	return q.Save(ctx)
}

func Modify(req *ResourceModifyReq, loginID string) (*ent.SysResource, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysResource.UpdateOneID(req.ID)

	if req.Name != "" {
		u.SetName(req.Name)
	}
	if req.Code != "" {
		u.SetCode(req.Code)
	}
	if req.ParentID != "" {
		u.SetParentID(req.ParentID)
	}
	if req.Hierarchy != "" {
		u.SetHierarchy(req.Hierarchy)
	}
	if req.Type != "" {
		u.SetType(req.Type)
	}
	if req.Category != "" {
		u.SetCategory(req.Category)
	}
	if req.Icon != "" {
		u.SetIcon(req.Icon)
	}
	if req.Path != "" {
		u.SetPath(req.Path)
	}
	if req.Component != "" {
		u.SetComponent(req.Component)
	}
	if req.Permission != "" {
		u.SetPermission(req.Permission)
	}
	if req.SortCode > 0 {
		u.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	if req.Visible != nil {
		u.SetVisible(*req.Visible)
	}
	if req.KeepAlive != nil {
		u.SetKeepAlive(*req.KeepAlive)
	}
	if req.IsFrame != nil {
		u.SetIsFrame(*req.IsFrame)
	}
	if req.IsCache != nil {
		u.SetIsCache(*req.IsCache)
	}
	if req.IsAffix != nil {
		u.SetIsAffix(*req.IsAffix)
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()

	// Clean up rel_role_resource
	_, err := db.Client.RelRoleResource.Delete().Where(relroleresource.ResourceIDIn(ids...)).Exec(ctx)
	if err != nil {
		return err
	}

	// Delete resources
	_, err = db.Client.SysResource.Delete().Where(sysresource.IDIn(ids...)).Exec(ctx)
	return err
}

func Detail(id string) (*ent.SysResource, error) {
	ctx := context.Background()
	return db.Client.SysResource.Get(ctx, id)
}

func QueryAll() ([]*ent.SysResource, error) {
	ctx := context.Background()
	return db.Client.SysResource.Query().Order(ent.Desc(sysresource.FieldCreatedAt)).All(ctx)
}

// TreeSelect returns resources as a flat-to-nested tree.
func TreeSelect() ([]*TreeSelectVO, error) {
	ctx := context.Background()
	items, err := db.Client.SysResource.Query().Order(ent.Asc(sysresource.FieldSortCode)).All(ctx)
	if err != nil {
		return nil, err
	}
	return buildResourceTree(items), nil
}

func buildResourceTree(items []*ent.SysResource) []*TreeSelectVO {
	childrenMap := make(map[string][]*ent.SysResource)
	var roots []*ent.SysResource
	for _, item := range items {
		if item.ParentID == "" {
			roots = append(roots, item)
		} else {
			childrenMap[item.ParentID] = append(childrenMap[item.ParentID], item)
		}
	}
	var tree []*TreeSelectVO
	for _, root := range roots {
		tree = append(tree, buildResourceTreeNode(root, childrenMap))
	}
	return tree
}

func buildResourceTreeNode(item *ent.SysResource, childrenMap map[string][]*ent.SysResource) *TreeSelectVO {
	node := &TreeSelectVO{
		ID:       item.ID,
		Name:     item.Name,
		ParentID: item.ParentID,
	}
	for _, child := range childrenMap[item.ID] {
		node.Children = append(node.Children, buildResourceTreeNode(child, childrenMap))
	}
	if node.Children == nil {
		node.Children = []*TreeSelectVO{}
	}
	return node
}

// BuildBootstrapMenus builds a menu tree for the frontend, filtering out BUTTON type.
func BuildBootstrapMenus() ([]*BootstrapMenuVO, error) {
	ctx := context.Background()
	items, err := db.Client.SysResource.Query().
		Where(
			sysresource.TypeNotIn("BUTTON"),
		).
		Order(ent.Asc(sysresource.FieldSortCode)).
		All(ctx)
	if err != nil {
		return nil, err
	}
	return buildBootstrapMenuTree(items), nil
}

func buildBootstrapMenuTree(items []*ent.SysResource) []*BootstrapMenuVO {
	childrenMap := make(map[string][]*ent.SysResource)
	var roots []*ent.SysResource
	for _, item := range items {
		if item.ParentID == "" {
			roots = append(roots, item)
		} else {
			childrenMap[item.ParentID] = append(childrenMap[item.ParentID], item)
		}
	}
	var tree []*BootstrapMenuVO
	for _, root := range roots {
		tree = append(tree, buildBootstrapMenuNode(root, childrenMap))
	}
	return tree
}

func buildBootstrapMenuNode(item *ent.SysResource, childrenMap map[string][]*ent.SysResource) *BootstrapMenuVO {
	node := &BootstrapMenuVO{
		ID:        item.ID,
		Name:      item.Name,
		Icon:      item.Icon,
		Path:      item.Path,
		Component: item.Component,
		Type:      item.Type,
	}
	for _, child := range childrenMap[item.ID] {
		node.Children = append(node.Children, buildBootstrapMenuNode(child, childrenMap))
	}
	if node.Children == nil {
		node.Children = []*BootstrapMenuVO{}
	}
	return node
}

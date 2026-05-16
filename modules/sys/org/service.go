package org

import (
	"context"
	"errors"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/relorgrole"
	"hei-gin/ent/gen/sysorg"
	"hei-gin/ent/gen/sysuser"
)

type PageParam struct {
	Page    int    `form:"page" json:"page"`
	Size    int    `form:"size" json:"size"`
	Keyword string `form:"keyword" json:"keyword"`
	Status  string `form:"status" json:"status"`
}

type OrgVO struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Code        string `json:"code"`
	ParentID    string `json:"parent_id"`
	Hierarchy   string `json:"hierarchy"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Description string `json:"description"`
	Leader      string `json:"leader"`
	Phone       string `json:"phone"`
	Email       string `json:"email"`
	Address     string `json:"address"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type OrgCreateReq struct {
	Name        string `json:"name" binding:"required"`
	Code        string `json:"code" binding:"required"`
	ParentID    string `json:"parent_id"`
	Hierarchy   string `json:"hierarchy"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Description string `json:"description"`
	Leader      string `json:"leader"`
	Phone       string `json:"phone"`
	Email       string `json:"email"`
	Address     string `json:"address"`
}

type OrgModifyReq struct {
	ID          string `json:"id" binding:"required"`
	Name        string `json:"name"`
	Code        string `json:"code"`
	ParentID    string `json:"parent_id"`
	Hierarchy   string `json:"hierarchy"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Description string `json:"description"`
	Leader      string `json:"leader"`
	Phone       string `json:"phone"`
	Email       string `json:"email"`
	Address     string `json:"address"`
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
	Code     string          `json:"code"`
	ParentID string          `json:"parent_id"`
	Children []*TreeSelectVO `json:"children"`
}

type OrgGrantRoleReq struct {
	OrgID   string   `json:"org_id" binding:"required"`
	RoleIDs []string `json:"role_ids" binding:"required"`
}

func toVO(o *ent.SysOrg) OrgVO {
	vo := OrgVO{
		ID:          o.ID,
		Name:        o.Name,
		Code:        o.Code,
		ParentID:    o.ParentID,
		Hierarchy:   o.Hierarchy,
		SortCode:    o.SortCode,
		Status:      o.Status,
		Description: o.Description,
		Leader:      o.Leader,
		Phone:       o.Phone,
		Email:       o.Email,
		Address:     o.Address,
		CreatedAt:   o.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   o.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   o.CreatedBy,
		UpdatedBy:   o.UpdatedBy,
	}
	return vo
}

func Page(page, size int, keyword, status string) (int, []*ent.SysOrg, error) {
	ctx := context.Background()
	q := db.Client.SysOrg.Query()

	if keyword != "" {
		q = q.Where(
			sysorg.Or(
				sysorg.NameContains(keyword),
				sysorg.CodeContains(keyword),
			),
		)
	}
	if status != "" {
		q = q.Where(sysorg.StatusEQ(status))
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
		Order(ent.Desc(sysorg.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Create(req *OrgCreateReq, loginID string) (*ent.SysOrg, error) {
	ctx := context.Background()
	now := time.Now()
	q := db.Client.SysOrg.Create().
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
	if req.SortCode > 0 {
		q.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		q.SetStatus(req.Status)
	} else {
		q.SetStatus("ENABLED")
	}
	if req.Description != "" {
		q.SetDescription(req.Description)
	}
	if req.Leader != "" {
		q.SetLeader(req.Leader)
	}
	if req.Phone != "" {
		q.SetPhone(req.Phone)
	}
	if req.Email != "" {
		q.SetEmail(req.Email)
	}
	if req.Address != "" {
		q.SetAddress(req.Address)
	}

	return q.Save(ctx)
}

func Modify(req *OrgModifyReq, loginID string) (*ent.SysOrg, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysOrg.UpdateOneID(req.ID)

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
	if req.SortCode > 0 {
		u.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}
	if req.Leader != "" {
		u.SetLeader(req.Leader)
	}
	if req.Phone != "" {
		u.SetPhone(req.Phone)
	}
	if req.Email != "" {
		u.SetEmail(req.Email)
	}
	if req.Address != "" {
		u.SetAddress(req.Address)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()

	// Recursively collect all descendant org IDs
	allIDs := make([]string, 0)
	allIDs = append(allIDs, ids...)
	currentIDs := ids
	for len(currentIDs) > 0 {
		children, err := db.Client.SysOrg.Query().
			Where(sysorg.ParentIDIn(currentIDs...)).
			Select(sysorg.FieldID).
			All(ctx)
		if err != nil {
			return err
		}
		currentIDs = nil
		for _, child := range children {
			allIDs = append(allIDs, child.ID)
			currentIDs = append(currentIDs, child.ID)
		}
	}

	// Check sys_user for references
	userCount, err := db.Client.SysUser.Query().Where(sysuser.OrgIDIn(allIDs...)).Count(ctx)
	if err != nil {
		return err
	}
	if userCount > 0 {
		return errors.New("存在关联数据，无法删除")
	}

	// Delete all descendant orgs
	_, err = db.Client.SysOrg.Delete().Where(sysorg.IDIn(allIDs...)).Exec(ctx)
	return err
}

func Detail(id string) (*ent.SysOrg, error) {
	ctx := context.Background()
	return db.Client.SysOrg.Get(ctx, id)
}

func QueryAll() ([]*ent.SysOrg, error) {
	ctx := context.Background()
	return db.Client.SysOrg.Query().Order(ent.Desc(sysorg.FieldCreatedAt)).All(ctx)
}

func TreeSelect() ([]*TreeSelectVO, error) {
	ctx := context.Background()
	items, err := db.Client.SysOrg.Query().Order(ent.Asc(sysorg.FieldSortCode)).All(ctx)
	if err != nil {
		return nil, err
	}
	return buildOrgTree(items), nil
}

func buildOrgTree(items []*ent.SysOrg) []*TreeSelectVO {
	childrenMap := make(map[string][]*ent.SysOrg)
	var roots []*ent.SysOrg
	for _, item := range items {
		if item.ParentID == "" {
			roots = append(roots, item)
		} else {
			childrenMap[item.ParentID] = append(childrenMap[item.ParentID], item)
		}
	}
	var tree []*TreeSelectVO
	for _, root := range roots {
		tree = append(tree, buildOrgTreeNode(root, childrenMap))
	}
	return tree
}

func buildOrgTreeNode(item *ent.SysOrg, childrenMap map[string][]*ent.SysOrg) *TreeSelectVO {
	node := &TreeSelectVO{
		ID:       item.ID,
		Name:     item.Name,
		Code:     item.Code,
		ParentID: item.ParentID,
	}
	for _, child := range childrenMap[item.ID] {
		node.Children = append(node.Children, buildOrgTreeNode(child, childrenMap))
	}
	if node.Children == nil {
		node.Children = []*TreeSelectVO{}
	}
	return node
}

func OwnRoles(orgID string) ([]string, error) {
	ctx := context.Background()
	rels, err := db.Client.RelOrgRole.Query().
		Where(relorgrole.OrgIDEQ(orgID)).
		Select(relorgrole.FieldRoleID).
		All(ctx)
	if err != nil {
		return nil, err
	}
	roleIDs := make([]string, len(rels))
	for i, r := range rels {
		roleIDs[i] = r.RoleID
	}
	return roleIDs, nil
}

func GrantRole(orgID string, roleIDs []string) error {
	ctx := context.Background()

	// Use ent client transaction
	tx, err := db.Client.Tx(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.RelOrgRole.Delete().Where(relorgrole.OrgIDEQ(orgID)).Exec(ctx)
	if err != nil {
		return err
	}

	for _, roleID := range roleIDs {
		_, err = tx.RelOrgRole.Create().
			SetID(utils.NextID()).
			SetOrgID(orgID).
			SetRoleID(roleID).
			Save(ctx)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

package group

import (
	"context"
	"sort"
	"strings"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/sysgroup"
)

type PageParam struct {
	Page    int    `form:"page" json:"page"`
	Size    int    `form:"size" json:"size"`
	Keyword string `form:"keyword" json:"keyword"`
	Status  string `form:"status" json:"status"`
}

type GroupVO struct {
	ID          string    `json:"id"`
	Name        string    `json:"name"`
	Code        string    `json:"code"`
	ParentID    string    `json:"parent_id"`
	Hierarchy   string    `json:"hierarchy"`
	SortCode    int       `json:"sort_code"`
	Status      string    `json:"status"`
	Description string    `json:"description"`
	CreatedAt   string    `json:"created_at"`
	CreatedBy   string    `json:"created_by"`
	UpdatedAt   string    `json:"updated_at"`
	UpdatedBy   string    `json:"updated_by"`
	Children    []GroupVO `json:"children,omitempty"`
}

type GroupCreateReq struct {
	Name        string `json:"name" binding:"required"`
	Code        string `json:"code"`
	ParentID    string `json:"parent_id"`
	Hierarchy   string `json:"hierarchy"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Description string `json:"description"`
}

type GroupModifyReq struct {
	ID          string `json:"id" binding:"required"`
	Name        string `json:"name"`
	Code        string `json:"code"`
	ParentID    string `json:"parent_id"`
	Hierarchy   string `json:"hierarchy"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Description string `json:"description"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

func toVO(g *ent.SysGroup) GroupVO {
	vo := GroupVO{
		ID:          g.ID,
		Name:        g.Name,
		Code:        g.Code,
		ParentID:    g.ParentID,
		Hierarchy:   g.Hierarchy,
		SortCode:    g.SortCode,
		Status:      g.Status,
		Description: g.Description,
		CreatedAt:   g.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   g.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   g.CreatedBy,
		UpdatedBy:   g.UpdatedBy,
	}
	return vo
}

func Page(page, size int, keyword, status string) (int, []*ent.SysGroup, error) {
	ctx := context.Background()
	q := db.Client.SysGroup.Query()

	if keyword != "" {
		q = q.Where(sysgroup.Or(
			sysgroup.NameContains(keyword),
			sysgroup.CodeContains(keyword),
		))
	}
	if status != "" {
		q = q.Where(sysgroup.Status(status))
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
		Order(ent.Desc(sysgroup.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Create(req *GroupCreateReq, loginID string) (*ent.SysGroup, error) {
	ctx := context.Background()
	now := time.Now()
	q := db.Client.SysGroup.Create().
		SetID(utils.NextID()).
		SetName(req.Name).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)
	if req.Code != "" {
		q.SetCode(req.Code)
	}
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
	}
	if req.Description != "" {
		q.SetDescription(req.Description)
	}
	return q.Save(ctx)
}

func Modify(req *GroupModifyReq, loginID string) (*ent.SysGroup, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysGroup.UpdateOneID(req.ID)

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

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()

	// Nullify group_id in related tables
	placeholders := make([]string, len(ids))
	args := make([]interface{}, len(ids))
	for i, id := range ids {
		placeholders[i] = "?"
		args[i] = id
	}
	inClause := strings.Join(placeholders, ",")

	_, _ = db.RawDB.ExecContext(ctx,
		"UPDATE sys_user SET group_id = NULL WHERE group_id IN ("+inClause+")", args...)
	_, _ = db.RawDB.ExecContext(ctx,
		"UPDATE sys_position SET group_id = NULL WHERE group_id IN ("+inClause+")", args...)

	// Delete groups
	_, err := db.Client.SysGroup.Delete().Where(sysgroup.IDIn(ids...)).Exec(ctx)
	return err
}

func Detail(id string) (*ent.SysGroup, error) {
	ctx := context.Background()
	return db.Client.SysGroup.Get(ctx, id)
}

func QueryAll() ([]*ent.SysGroup, error) {
	ctx := context.Background()
	return db.Client.SysGroup.Query().Order(ent.Desc(sysgroup.FieldCreatedAt)).All(ctx)
}

func TreeSelect() ([]GroupVO, error) {
	ctx := context.Background()
	all, err := db.Client.SysGroup.Query().Order(ent.Asc(sysgroup.FieldSortCode)).All(ctx)
	if err != nil {
		return nil, err
	}

	voMap := make(map[string]GroupVO)
	for _, g := range all {
		voMap[g.ID] = toVO(g)
	}

	var roots []GroupVO
	for _, g := range all {
		vo := voMap[g.ID]
		if g.ParentID == "" {
			roots = append(roots, vo)
		} else {
			if parent, ok := voMap[g.ParentID]; ok {
				parent.Children = append(parent.Children, vo)
				voMap[g.ParentID] = parent
			}
		}
	}

	return roots, nil
}

type TreeGroup struct {
	ID       string       `json:"id"`
	Name     string       `json:"name"`
	ParentID string       `json:"parent_id"`
	SortCode int          `json:"sort_code"`
	Type     string       `json:"_type,omitempty"`
	Children []*TreeGroup `json:"children,omitempty"`
}

func Tree(orgID, keyword string) ([]*TreeGroup, error) {
	ctx := context.Background()

	query := "SELECT id, name, COALESCE(parent_id,'') as parent_id, sort_code FROM sys_group"
	var conditions []string
	var args []interface{}

	if orgID != "" {
		conditions = append(conditions, "org_id = ?")
		args = append(args, orgID)
	}
	if keyword != "" {
		conditions = append(conditions, "name LIKE ?")
		args = append(args, "%"+keyword+"%")
	}

	if len(conditions) > 0 {
		query += " WHERE " + strings.Join(conditions, " AND ")
	}
	query += " ORDER BY sort_code ASC"

	rows, err := db.RawDB.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	type rawGroup struct {
		ID, Name, ParentID string
		SortCode           int
	}
	var allGroups []rawGroup
	for rows.Next() {
		var g rawGroup
		if err := rows.Scan(&g.ID, &g.Name, &g.ParentID, &g.SortCode); err != nil {
			return nil, err
		}
		allGroups = append(allGroups, g)
	}

	nodeMap := make(map[string]*TreeGroup)
	for _, g := range allGroups {
		nodeMap[g.ID] = &TreeGroup{
			ID:       g.ID,
			Name:     g.Name,
			ParentID: g.ParentID,
			SortCode: g.SortCode,
		}
	}

	var roots []*TreeGroup
	for _, g := range allGroups {
		node := nodeMap[g.ID]
		if g.ParentID == "" || nodeMap[g.ParentID] == nil {
			roots = append(roots, node)
		} else {
			parent := nodeMap[g.ParentID]
			parent.Children = append(parent.Children, node)
		}
	}

	sortTree(roots)
	return roots, nil
}

func UnionTree() ([]*TreeGroup, error) {
	ctx := context.Background()

	// Query all orgs
	orgRows, err := db.RawDB.QueryContext(ctx,
		"SELECT id, name, COALESCE(parent_id,'') as parent_id, sort_code FROM sys_org ORDER BY sort_code ASC")
	if err != nil {
		return nil, err
	}

	type rawOrg struct {
		ID, Name, ParentID string
		SortCode           int
	}
	var orgs []rawOrg
	for orgRows.Next() {
		var o rawOrg
		if err := orgRows.Scan(&o.ID, &o.Name, &o.ParentID, &o.SortCode); err != nil {
			orgRows.Close()
			return nil, err
		}
		orgs = append(orgs, o)
	}
	orgRows.Close()

	// Query all groups
	groupRows, err := db.RawDB.QueryContext(ctx,
		"SELECT id, name, COALESCE(parent_id,'') as parent_id, sort_code, COALESCE(org_id,'') as org_id FROM sys_group ORDER BY sort_code ASC")
	if err != nil {
		return nil, err
	}

	type rawGroupExt struct {
		ID, Name, ParentID, OrgID string
		SortCode                  int
	}
	var groups []rawGroupExt
	for groupRows.Next() {
		var g rawGroupExt
		if err := groupRows.Scan(&g.ID, &g.Name, &g.ParentID, &g.SortCode, &g.OrgID); err != nil {
			groupRows.Close()
			return nil, err
		}
		groups = append(groups, g)
	}
	groupRows.Close()

	// Build org nodes
	orgMap := make(map[string]*TreeGroup)
	for _, o := range orgs {
		orgMap[o.ID] = &TreeGroup{
			ID:       o.ID,
			Name:     o.Name,
			ParentID: o.ParentID,
			SortCode: o.SortCode,
			Type:     "org",
		}
	}

	// Build group nodes
	groupMap := make(map[string]*TreeGroup)
	for _, g := range groups {
		groupMap[g.ID] = &TreeGroup{
			ID:       g.ID,
			Name:     g.Name,
			ParentID: g.ParentID,
			SortCode: g.SortCode,
			Type:     "group",
		}
	}

	// Build group parent-child relationships
	for _, g := range groups {
		node := groupMap[g.ID]
		if g.ParentID != "" {
			if parent, ok := groupMap[g.ParentID]; ok {
				parent.Children = append(parent.Children, node)
			}
		}
	}

	// Orphan groups (no parent group) -- group by org_id
	orphanGroups := make(map[string][]*TreeGroup)
	for _, g := range groups {
		node := groupMap[g.ID]
		if g.ParentID == "" || groupMap[g.ParentID] == nil {
			orphanGroups[g.OrgID] = append(orphanGroups[g.OrgID], node)
		}
	}

	// Attach orphan groups to their org
	for oid, nodes := range orphanGroups {
		if org, ok := orgMap[oid]; ok {
			org.Children = append(nodes, org.Children...)
		}
	}

	// Build org parent-child tree
	var roots []*TreeGroup
	for _, o := range orgs {
		node := orgMap[o.ID]
		if o.ParentID != "" {
			if parent, ok := orgMap[o.ParentID]; ok {
				parent.Children = append(parent.Children, node)
			} else {
				roots = append(roots, node)
			}
		} else {
			roots = append(roots, node)
		}
	}

	sortTree(roots)
	return roots, nil
}

func sortTree(nodes []*TreeGroup) {
	sort.Slice(nodes, func(i, j int) bool {
		return nodes[i].SortCode < nodes[j].SortCode
	})
	for _, n := range nodes {
		if len(n.Children) > 0 {
			sortTree(n.Children)
		}
	}
}

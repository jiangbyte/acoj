package dict

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/predicate"
	"hei-gin/ent/gen/sysdict"
	"hei-gin/ent/gen/sysdictdata"
)

// ========================================================================
//  Helpers
// ========================================================================

func dictToVO(d *ent.SysDict) DictVO {
	return DictVO{
		ID:          d.ID,
		Code:        d.Code,
		Name:        d.Name,
		Category:    d.Category,
		Description: d.Description,
		SortCode:    d.SortCode,
		Status:      d.Status,
		CreatedAt:   d.CreatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   d.CreatedBy,
		UpdatedAt:   d.UpdatedAt.Format("2006-01-02 15:04:05"),
		UpdatedBy:   d.UpdatedBy,
	}
}

func dictToNode(d *ent.SysDict) *DictTreeNode {
	return &DictTreeNode{
		ID:          d.ID,
		Type:        "dict",
		SortCode:    d.SortCode,
		Status:      d.Status,
		CreatedAt:   d.CreatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   d.CreatedBy,
		UpdatedAt:   d.UpdatedAt.Format("2006-01-02 15:04:05"),
		UpdatedBy:   d.UpdatedBy,
		Children:    []*DictTreeNode{},
		Code:        d.Code,
		Name:        d.Name,
		Category:    d.Category,
		Description: d.Description,
	}
}

func dataToNode(d *ent.SysDictData) *DictTreeNode {
	return &DictTreeNode{
		ID:        d.ID,
		Type:      "data",
		SortCode:  d.SortCode,
		Status:    d.Status,
		CreatedAt: d.CreatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy: d.CreatedBy,
		UpdatedAt: d.UpdatedAt.Format("2006-01-02 15:04:05"),
		UpdatedBy: d.UpdatedBy,
		Children:  []*DictTreeNode{},
		DictID:    d.DictID,
		Label:     d.Label,
		Value:     d.Value,
		Color:     d.Color,
	}
}

// toTreeNode converts an ent model (SysDict or SysDictData) into a DictTreeNode.
// It is used by the Detail handler where the returned type is unknown at compile time.
func toTreeNode(v interface{}) *DictTreeNode {
	switch t := v.(type) {
	case *ent.SysDict:
		return dictToNode(t)
	case *ent.SysDictData:
		return dataToNode(t)
	}
	return nil
}

// ========================================================================
//  Page / List
// ========================================================================

func Page(page, size int, keyword, category string) (int, []*ent.SysDict, error) {
	ctx := context.Background()
	q := db.Client.SysDict.Query()

	var conds []predicate.SysDict
	if keyword != "" {
		conds = append(conds,
			sysdict.Or(
				sysdict.NameContains(keyword),
				sysdict.CodeContains(keyword),
			),
		)
	}
	if category != "" {
		conds = append(conds, sysdict.Category(category))
	}
	if len(conds) > 0 {
		q = q.Where(conds...)
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
		Order(ent.Desc(sysdict.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func List(keyword, category string) ([]*ent.SysDict, error) {
	ctx := context.Background()
	q := db.Client.SysDict.Query()

	var conds []predicate.SysDict
	if keyword != "" {
		conds = append(conds,
			sysdict.Or(
				sysdict.NameContains(keyword),
				sysdict.CodeContains(keyword),
			),
		)
	}
	if category != "" {
		conds = append(conds, sysdict.Category(category))
	}
	if len(conds) > 0 {
		q = q.Where(conds...)
	}

	return q.Order(ent.Desc(sysdict.FieldCreatedAt)).All(ctx)
}

// ========================================================================
//  Tree
// ========================================================================

// Tree loads all SysDict and SysDictData in two queries and assembles the
// combined tree in memory, avoiding N+1.
func Tree() ([]*DictTreeNode, error) {
	ctx := context.Background()

	dicts, err := db.Client.SysDict.Query().
		Order(ent.Asc(sysdict.FieldSortCode)).
		All(ctx)
	if err != nil {
		return nil, err
	}

	datas, err := db.Client.SysDictData.Query().
		Order(ent.Asc(sysdictdata.FieldSortCode)).
		All(ctx)
	if err != nil {
		return nil, err
	}

	// Group data entries by dict_id
	dataGroup := make(map[string][]*ent.SysDictData)
	for _, d := range datas {
		dataGroup[d.DictID] = append(dataGroup[d.DictID], d)
	}

	roots := make([]*DictTreeNode, 0, len(dicts))
	for _, d := range dicts {
		node := dictToNode(d)
		if children, ok := dataGroup[d.ID]; ok {
			for _, child := range children {
				node.Children = append(node.Children, dataToNode(child))
			}
		}
		roots = append(roots, node)
	}

	return roots, nil
}

// ========================================================================
//  Create
// ========================================================================

func Create(req *DictCreateReq, loginID string) (interface{}, error) {
	ctx := context.Background()
	now := time.Now()

	// parent_id is "0" or "" => create SysDict
	if req.ParentID == "" || req.ParentID == "0" {
		q := db.Client.SysDict.Create().
			SetID(utils.NextID()).
			SetName(req.Name).
			SetCode(req.Code).
			SetCreatedAt(now).
			SetCreatedBy(loginID).
			SetUpdatedAt(now).
			SetUpdatedBy(loginID)
		if req.Category != "" {
			q.SetCategory(req.Category)
		}
		if req.Description != "" {
			q.SetDescription(req.Description)
		}
		if req.SortCode > 0 {
			q.SetSortCode(req.SortCode)
		}
		if req.Status != "" {
			q.SetStatus(req.Status)
		}
		return q.Save(ctx)
	}

	// parent_id is set => create SysDictData
	q := db.Client.SysDictData.Create().
		SetID(utils.NextID()).
		SetDictID(req.ParentID).
		SetLabel(req.Label).
		SetValue(req.Value).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)
	if req.Color != "" {
		q.SetColor(req.Color)
	}
	if req.SortCode > 0 {
		q.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		q.SetStatus(req.Status)
	}
	return q.Save(ctx)
}

// ========================================================================
//  Modify
// ========================================================================

func Modify(req *DictModifyReq, loginID string) (interface{}, error) {
	ctx := context.Background()
	now := time.Now()

	// Try SysDict first
	dict, err := db.Client.SysDict.Get(ctx, req.ID)
	if err == nil {
		// It is a SysDict
		u := db.Client.SysDict.UpdateOneID(req.ID)
		if req.Name != "" {
			u.SetName(req.Name)
		}
		if req.Code != "" {
			u.SetCode(req.Code)
		}
		if req.Category != "" {
			u.SetCategory(req.Category)
		}
		if req.Description != "" {
			u.SetDescription(req.Description)
		}
		if req.SortCode > 0 {
			u.SetSortCode(req.SortCode)
		}
		if req.Status != "" {
			u.SetStatus(req.Status)
		}
		dict, err = u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
		if err != nil {
			return nil, err
		}
		return dict, nil
	}

	// Try SysDictData
	data, err := db.Client.SysDictData.Get(ctx, req.ID)
	if err != nil {
		return nil, err
	}

	u := db.Client.SysDictData.UpdateOneID(req.ID)
	if req.DictID != "" {
		u.SetDictID(req.DictID)
	}
	if req.Label != "" {
		u.SetLabel(req.Label)
	}
	if req.Value != "" {
		u.SetValue(req.Value)
	}
	if req.Color != "" {
		u.SetColor(req.Color)
	}
	if req.SortCode > 0 {
		u.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	data, err = u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
	if err != nil {
		return nil, err
	}
	return data, nil
}

// ========================================================================
//  Remove (cascade)
// ========================================================================

func Remove(ids []string) error {
	ctx := context.Background()

	// 1. Find SysDict entries matching requested IDs
	dicts, err := db.Client.SysDict.Query().
		Where(sysdict.IDIn(ids...)).
		All(ctx)
	if err != nil {
		return err
	}
	dictIDs := make([]string, 0, len(dicts))
	for _, d := range dicts {
		dictIDs = append(dictIDs, d.ID)
	}

	// 2. Find SysDictData entries matching requested IDs
	directData, err := db.Client.SysDictData.Query().
		Where(sysdictdata.IDIn(ids...)).
		All(ctx)
	if err != nil {
		return err
	}
	dataIDs := make([]string, 0, len(directData))
	for _, d := range directData {
		dataIDs = append(dataIDs, d.ID)
	}

	// 3. Find SysDictData entries linked to the SysDict entries being removed
	if len(dictIDs) > 0 {
		linkedData, err := db.Client.SysDictData.Query().
			Where(sysdictdata.DictIDIn(dictIDs...)).
			All(ctx)
		if err != nil {
			return err
		}
		for _, d := range linkedData {
			dataIDs = append(dataIDs, d.ID)
		}
	}

	// 4. Batch delete SysDict entries
	if len(dictIDs) > 0 {
		_, err = db.Client.SysDict.Delete().
			Where(sysdict.IDIn(dictIDs...)).
			Exec(ctx)
		if err != nil {
			return err
		}
	}

	// 5. Batch delete SysDictData entries
	if len(dataIDs) > 0 {
		_, err = db.Client.SysDictData.Delete().
			Where(sysdictdata.IDIn(dataIDs...)).
			Exec(ctx)
		if err != nil {
			return err
		}
	}

	return nil
}

// ========================================================================
//  Detail
// ========================================================================

func Detail(id string) (interface{}, error) {
	ctx := context.Background()

	dict, err := db.Client.SysDict.Get(ctx, id)
	if err == nil {
		return dict, nil
	}

	data, err := db.Client.SysDictData.Get(ctx, id)
	if err != nil {
		return nil, err
	}
	return data, nil
}

// ========================================================================
//  GetLabel
// ========================================================================

func GetLabel(typeCode, value string) (string, error) {
	ctx := context.Background()

	dict, err := db.Client.SysDict.Query().
		Where(sysdict.CodeEQ(typeCode)).
		First(ctx)
	if err != nil {
		return "", err
	}

	data, err := db.Client.SysDictData.Query().
		Where(
			sysdictdata.DictID(dict.ID),
			sysdictdata.ValueEQ(value),
		).
		First(ctx)
	if err != nil {
		return "", err
	}

	return data.Label, nil
}

// ========================================================================
//  GetChildren
// ========================================================================

// GetChildren returns the dict type node (with code, name) and its data children.
func GetChildren(typeCode string) (*DictTreeNode, error) {
	ctx := context.Background()

	dict, err := db.Client.SysDict.Query().
		Where(sysdict.CodeEQ(typeCode)).
		First(ctx)
	if err != nil {
		return nil, err
	}

	datas, err := db.Client.SysDictData.Query().
		Where(sysdictdata.DictID(dict.ID)).
		Order(ent.Asc(sysdictdata.FieldSortCode)).
		All(ctx)
	if err != nil {
		return nil, err
	}

	node := dictToNode(dict)
	node.Children = make([]*DictTreeNode, 0, len(datas))
	for _, d := range datas {
		node.Children = append(node.Children, dataToNode(d))
	}

	return node, nil
}

// ========================================================================
//  Export / Import helpers
// ========================================================================

func QueryAll() ([]*ent.SysDict, error) {
	ctx := context.Background()
	return db.Client.SysDict.Query().Order(ent.Desc(sysdict.FieldCreatedAt)).All(ctx)
}

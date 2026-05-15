package group

import (
	"context"
	"errors"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func Page(ctx context.Context, keyword, status string, current, size int) (*utility.PageRes, error) {
	m := dao.SysGroup.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("name LIKE ? OR code LIKE ?", kw, kw)
	}
	if status != "" {
		m = m.Where("status", status)
	}

	count, err := m.Count()
	if err != nil {
		return nil, err
	}

	var list []g.Map
	if err := m.Page(current, size).Scan(&list); err != nil {
		return nil, err
	}

	return utility.NewPageRes(list, count, current, size), nil
}

func Create(ctx context.Context, code, name, category, parentId, orgId, description, status string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysGroup.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"code":        code,
		"name":        name,
		"category":    category,
		"parent_id":   parentId,
		"org_id":      orgId,
		"description": description,
		"status":      ifEmpty(status, consts.StatusEnabled),
		"sort_code":   sortCode,
		"created_by":  loginId,
	})
	return err
}

func Modify(ctx context.Context, id, code, name, category, parentId, orgId, description, status string, sortCode int) error {
	entity := findById(ctx, id)
	if entity == nil {
		return errors.New("数据不存在")
	}

	updateData := g.Map{}
	if code != "" {
		updateData["code"] = code
	}
	if name != "" {
		updateData["name"] = name
	}
	if category != "" {
		updateData["category"] = category
	}
	if parentId != "" {
		updateData["parent_id"] = parentId
	}
	if orgId != "" {
		updateData["org_id"] = orgId
	}
	if description != "" {
		updateData["description"] = description
	}
	if status != "" {
		updateData["status"] = status
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}

	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysGroup.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	// Check for child groups
	count, _ := dao.SysGroup.Ctx().Ctx(ctx).Where("parent_id in (?)", ids).Count()
	if count > 0 {
		return errors.New("用户组存在子级，无法删除")
	}

	_, err := dao.SysGroup.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysGroup.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	return g.Map{
		"id":          row["id"].String(),
		"code":        row["code"].String(),
		"name":        row["name"].String(),
		"category":    row["category"].String(),
		"parent_id":   row["parent_id"].String(),
		"org_id":      row["org_id"].String(),
		"description": row["description"].String(),
		"status":      row["status"].String(),
		"sort_code":   row["sort_code"].Int(),
		"created_at":  row["created_at"].String(),
		"created_by":  row["created_by"].String(),
		"updated_at":  row["updated_at"].String(),
		"updated_by":  row["updated_by"].String(),
	}, nil
}

func Tree(ctx context.Context) ([]g.Map, error) {
	rows, err := dao.SysGroup.Ctx().Ctx(ctx).OrderAsc("sort_code").All()
	if err != nil {
		return nil, err
	}

	groupMap := make(map[string]g.Map)
	for _, r := range rows {
		groupMap[r["id"].String()] = g.Map{
			"id":          r["id"].String(),
			"code":        r["code"].String(),
			"name":        r["name"].String(),
			"category":    r["category"].String(),
			"parent_id":   r["parent_id"].String(),
			"org_id":      r["org_id"].String(),
			"description": r["description"].String(),
			"status":      r["status"].String(),
			"sort_code":   r["sort_code"].Int(),
			"children":    []g.Map{},
		}
	}

	var tree []g.Map
	for _, r := range rows {
		node := groupMap[r["id"].String()]
		pid := r["parent_id"].String()
		if pid != "" && groupMap[pid] != nil {
			children, _ := groupMap[pid]["children"].([]g.Map)
			children = append(children, node)
			groupMap[pid]["children"] = children
		} else {
			tree = append(tree, node)
		}
	}
	return tree, nil
}

func findById(ctx context.Context, id string) g.Map {
	row, err := dao.SysGroup.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil
	}
	return row.Map()
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}

func ifEmpty(s, def string) string {
	if s == "" {
		return def
	}
	return s
}

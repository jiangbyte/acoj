package dict

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func Page(ctx context.Context, keyword, status string, current, size int) (*utility.PageRes, error) {
	m := dao.SysDict.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("code LIKE ? OR label LIKE ?", kw, kw)
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

func Create(ctx context.Context, code, label, value, color, category, parentId, status string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysDict.Ctx().Ctx(ctx).Insert(g.Map{
		"id":         utility.GenerateID(),
		"code":       code,
		"label":      label,
		"value":      value,
		"color":      color,
		"category":   category,
		"parent_id":  parentId,
		"status":     ifEmpty(status, "ENABLED"),
		"sort_code":  sortCode,
		"created_by": loginId,
	})
	return err
}

func Modify(ctx context.Context, id, code, label, value, color, category, parentId, status string, sortCode int) error {
	updateData := g.Map{}
	if code != "" {
		updateData["code"] = code
	}
	if label != "" {
		updateData["label"] = label
	}
	if value != "" {
		updateData["value"] = value
	}
	if color != "" {
		updateData["color"] = color
	}
	if category != "" {
		updateData["category"] = category
	}
	if parentId != "" {
		updateData["parent_id"] = parentId
	}
	if status != "" {
		updateData["status"] = status
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}
	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysDict.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysDict.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysDict.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	return g.Map{
		"id":         row["id"].String(),
		"code":       row["code"].String(),
		"label":      row["label"].String(),
		"value":      row["value"].String(),
		"color":      row["color"].String(),
		"category":   row["category"].String(),
		"parent_id":  row["parent_id"].String(),
		"status":     row["status"].String(),
		"sort_code":  row["sort_code"].Int(),
		"created_at": row["created_at"].String(),
		"created_by": row["created_by"].String(),
		"updated_at": row["updated_at"].String(),
		"updated_by": row["updated_by"].String(),
	}, nil
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

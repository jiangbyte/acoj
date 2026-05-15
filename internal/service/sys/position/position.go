package position

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
	m := dao.SysPosition.Ctx().Ctx(ctx).OrderAsc("sort_code")
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

func Create(ctx context.Context, code, name, category, orgId, groupId, description, status string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysPosition.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"code":        code,
		"name":        name,
		"category":    category,
		"org_id":      orgId,
		"group_id":    groupId,
		"description": description,
		"status":      ifEmpty(status, consts.StatusEnabled),
		"sort_code":   sortCode,
		"created_by":  loginId,
	})
	return err
}

func Modify(ctx context.Context, id, code, name, category, orgId, groupId, description, status string, sortCode int) error {
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
	if orgId != "" {
		updateData["org_id"] = orgId
	}
	if groupId != "" {
		updateData["group_id"] = groupId
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
		_, err := dao.SysPosition.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysPosition.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysPosition.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	return g.Map{
		"id":          row["id"].String(),
		"code":        row["code"].String(),
		"name":        row["name"].String(),
		"category":    row["category"].String(),
		"org_id":      row["org_id"].String(),
		"group_id":    row["group_id"].String(),
		"description": row["description"].String(),
		"status":      row["status"].String(),
		"sort_code":   row["sort_code"].Int(),
		"created_at":  row["created_at"].String(),
		"created_by":  row["created_by"].String(),
		"updated_at":  row["updated_at"].String(),
		"updated_by":  row["updated_by"].String(),
	}, nil
}

func findById(ctx context.Context, id string) g.Map {
	row, err := dao.SysPosition.Ctx().Ctx(ctx).WherePri(id).One()
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

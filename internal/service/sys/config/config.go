package config

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func Page(ctx context.Context, keyword string, current, size int) (*utility.PageRes, error) {
	m := dao.SysConfig.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("config_key LIKE ? OR remark LIKE ?", kw, kw)
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

func Create(ctx context.Context, configKey, configValue, category, remark string, sortCode int, extra string) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysConfig.Ctx().Ctx(ctx).Insert(g.Map{
		"id":           utility.GenerateID(),
		"config_key":   configKey,
		"config_value": configValue,
		"category":     category,
		"remark":       remark,
		"sort_code":    sortCode,
		"extra":        extra,
		"created_by":   loginId,
	})
	return err
}

func Modify(ctx context.Context, id, configKey, configValue, category, remark string, sortCode int, extra string) error {
	updateData := g.Map{}
	if configKey != "" {
		updateData["config_key"] = configKey
	}
	if configValue != "" {
		updateData["config_value"] = configValue
	}
	if category != "" {
		updateData["category"] = category
	}
	if remark != "" {
		updateData["remark"] = remark
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}
	if extra != "" {
		updateData["extra"] = extra
	}
	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysConfig.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysConfig.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysConfig.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	return g.Map{
		"id":           row["id"].String(),
		"config_key":   row["config_key"].String(),
		"config_value": row["config_value"].String(),
		"category":     row["category"].String(),
		"remark":       row["remark"].String(),
		"sort_code":    row["sort_code"].Int(),
		"extra":        row["extra"].String(),
		"created_at":   row["created_at"].String(),
		"created_by":   row["created_by"].String(),
		"updated_at":   row["updated_at"].String(),
		"updated_by":   row["updated_by"].String(),
	}, nil
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}

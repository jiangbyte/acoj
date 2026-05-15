package notice

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func Page(ctx context.Context, current, size int) (*utility.PageRes, error) {
	m := dao.SysNotice.Ctx().Ctx(ctx).OrderAsc("sort_code")
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

func Create(ctx context.Context, title, category, noticeType, summary, content, cover, level, position, status string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysNotice.Ctx().Ctx(ctx).Insert(g.Map{
		"id":         utility.GenerateID(),
		"title":      title,
		"category":   category,
		"type":       noticeType,
		"summary":    summary,
		"content":    content,
		"cover":      cover,
		"level":      level,
		"position":   position,
		"status":     ifEmpty(status, "ENABLED"),
		"sort_code":  sortCode,
		"created_by": loginId,
	})
	return err
}

func Modify(ctx context.Context, id, title, category, noticeType, summary, content, cover, level, position, status string, sortCode int) error {
	updateData := g.Map{}
	if title != "" {
		updateData["title"] = title
	}
	if category != "" {
		updateData["category"] = category
	}
	if noticeType != "" {
		updateData["type"] = noticeType
	}
	if summary != "" {
		updateData["summary"] = summary
	}
	if content != "" {
		updateData["content"] = content
	}
	if cover != "" {
		updateData["cover"] = cover
	}
	if level != "" {
		updateData["level"] = level
	}
	if position != "" {
		updateData["position"] = position
	}
	if status != "" {
		updateData["status"] = status
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}
	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysNotice.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysNotice.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysNotice.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	return g.Map{
		"id":         row["id"].String(),
		"title":      row["title"].String(),
		"category":   row["category"].String(),
		"type":       row["type"].String(),
		"summary":    row["summary"].String(),
		"content":    row["content"].String(),
		"cover":      row["cover"].String(),
		"level":      row["level"].String(),
		"view_count": row["view_count"].Int(),
		"is_top":     row["is_top"].String(),
		"position":   row["position"].String(),
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

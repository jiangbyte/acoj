package banner

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func Page(ctx context.Context, current, size int) (*utility.PageRes, error) {
	m := dao.SysBanner.Ctx().Ctx(ctx).OrderAsc("sort_code")
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

func Create(ctx context.Context, title, image, category, bannerType, position, url, linkType, summary, description string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysBanner.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"title":       title,
		"image":       image,
		"category":    category,
		"type":        bannerType,
		"position":    position,
		"url":         url,
		"link_type":   linkType,
		"summary":     summary,
		"description": description,
		"sort_code":   sortCode,
		"created_by":  loginId,
	})
	return err
}

func Modify(ctx context.Context, id, title, image, category, bannerType, position, url, linkType, summary, description string, sortCode int) error {
	updateData := g.Map{}
	if title != "" {
		updateData["title"] = title
	}
	if image != "" {
		updateData["image"] = image
	}
	if category != "" {
		updateData["category"] = category
	}
	if bannerType != "" {
		updateData["type"] = bannerType
	}
	if position != "" {
		updateData["position"] = position
	}
	if url != "" {
		updateData["url"] = url
	}
	if linkType != "" {
		updateData["link_type"] = linkType
	}
	if summary != "" {
		updateData["summary"] = summary
	}
	if description != "" {
		updateData["description"] = description
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}
	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysBanner.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysBanner.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysBanner.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	return g.Map{
		"id":          row["id"].String(),
		"title":       row["title"].String(),
		"image":       row["image"].String(),
		"category":    row["category"].String(),
		"type":        row["type"].String(),
		"position":    row["position"].String(),
		"url":         row["url"].String(),
		"link_type":   row["link_type"].String(),
		"summary":     row["summary"].String(),
		"description": row["description"].String(),
		"sort_code":   row["sort_code"].Int(),
		"view_count":  row["view_count"].Int(),
		"click_count": row["click_count"].Int(),
		"created_at":  row["created_at"].String(),
		"created_by":  row["created_by"].String(),
		"updated_at":  row["updated_at"].String(),
		"updated_by":  row["updated_by"].String(),
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

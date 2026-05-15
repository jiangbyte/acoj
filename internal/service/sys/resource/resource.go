package resource

import (
	"context"

	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func Tree(ctx context.Context) ([]g.Map, error) {
	rows, err := dao.SysResource.Ctx().Ctx(ctx).OrderAsc("sort_code").All()
	if err != nil {
		return nil, err
	}
	return buildTree(rows), nil
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysResource.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	return g.Map{
		"id":             row["id"].String(),
		"code":           row["code"].String(),
		"name":           row["name"].String(),
		"category":       row["category"].String(),
		"type":           row["type"].String(),
		"description":    row["description"].String(),
		"parent_id":      row["parent_id"].String(),
		"route_path":     row["route_path"].String(),
		"component_path": row["component_path"].String(),
		"redirect_path":  row["redirect_path"].String(),
		"icon":           row["icon"].String(),
		"color":          row["color"].String(),
		"is_visible":     row["is_visible"].String(),
		"is_cache":       row["is_cache"].String(),
		"is_affix":       row["is_affix"].String(),
		"is_breadcrumb":  row["is_breadcrumb"].String(),
		"external_url":   row["external_url"].String(),
		"extra":          row["extra"].String(),
		"status":         row["status"].String(),
		"sort_code":      row["sort_code"].Int(),
		"created_at":     row["created_at"].String(),
		"created_by":     row["created_by"].String(),
		"updated_at":     row["updated_at"].String(),
		"updated_by":     row["updated_by"].String(),
	}, nil
}

func Create(ctx context.Context, code, name, category, resourceType, description, parentId, routePath, componentPath, redirectPath, icon, color, isVisible, isCache, isAffix, isBreadcrumb, externalUrl, extra, status string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysResource.Ctx().Ctx(ctx).Insert(g.Map{
		"id":             utility.GenerateID(),
		"code":           code,
		"name":           name,
		"category":       category,
		"type":           resourceType,
		"description":    description,
		"parent_id":      parentId,
		"route_path":     routePath,
		"component_path": componentPath,
		"redirect_path":  redirectPath,
		"icon":           icon,
		"color":          color,
		"is_visible":     ifEmpty(isVisible, "YES"),
		"is_cache":       ifEmpty(isCache, "NO"),
		"is_affix":       ifEmpty(isAffix, "NO"),
		"is_breadcrumb":  ifEmpty(isBreadcrumb, "YES"),
		"external_url":   externalUrl,
		"extra":          extra,
		"status":         ifEmpty(status, "ENABLED"),
		"sort_code":      sortCode,
		"created_by":     loginId,
	})
	return err
}

func Modify(ctx context.Context, id, code, name, category, resourceType, description, parentId, routePath, componentPath, redirectPath, icon, color, isVisible, isCache, isAffix, isBreadcrumb, externalUrl, extra, status string, sortCode int) error {
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
	if resourceType != "" {
		updateData["type"] = resourceType
	}
	if description != "" {
		updateData["description"] = description
	}
	if parentId != "" {
		updateData["parent_id"] = parentId
	}
	if routePath != "" {
		updateData["route_path"] = routePath
	}
	if componentPath != "" {
		updateData["component_path"] = componentPath
	}
	if redirectPath != "" {
		updateData["redirect_path"] = redirectPath
	}
	if icon != "" {
		updateData["icon"] = icon
	}
	if color != "" {
		updateData["color"] = color
	}
	if isVisible != "" {
		updateData["is_visible"] = isVisible
	}
	if isCache != "" {
		updateData["is_cache"] = isCache
	}
	if isAffix != "" {
		updateData["is_affix"] = isAffix
	}
	if isBreadcrumb != "" {
		updateData["is_breadcrumb"] = isBreadcrumb
	}
	if externalUrl != "" {
		updateData["external_url"] = externalUrl
	}
	if extra != "" {
		updateData["extra"] = extra
	}
	if status != "" {
		updateData["status"] = status
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}
	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysResource.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	// Delete children first, then parent resources
	dao.SysResource.Ctx().Ctx(ctx).Where("parent_id in (?)", ids).Delete()
	dao.RelRoleResource.Ctx().Ctx(ctx).Where("resource_id in (?)", ids).Delete()
	_, err := dao.SysResource.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func buildTree(rows []gdb.Record) []g.Map {
	resourceMap := make(map[string]g.Map)
	for _, r := range rows {
		resourceMap[r["id"].String()] = g.Map{
			"id":             r["id"].String(),
			"code":           r["code"].String(),
			"name":           r["name"].String(),
			"category":       r["category"].String(),
			"type":           r["type"].String(),
			"description":    r["description"].String(),
			"parent_id":      r["parent_id"].String(),
			"route_path":     r["route_path"].String(),
			"component_path": r["component_path"].String(),
			"redirect_path":  r["redirect_path"].String(),
			"icon":           r["icon"].String(),
			"color":          r["color"].String(),
			"is_visible":     r["is_visible"].String(),
			"is_cache":       r["is_cache"].String(),
			"is_affix":       r["is_affix"].String(),
			"is_breadcrumb":  r["is_breadcrumb"].String(),
			"external_url":   r["external_url"].String(),
			"extra":          r["extra"].String(),
			"status":         r["status"].String(),
			"sort_code":      r["sort_code"].Int(),
			"children":       []g.Map{},
		}
	}
	var tree []g.Map
	for _, r := range rows {
		node := resourceMap[r["id"].String()]
		pid := r["parent_id"].String()
		if pid != "" && resourceMap[pid] != nil {
			children, _ := resourceMap[pid]["children"].([]g.Map)
			children = append(children, node)
			resourceMap[pid]["children"] = children
		} else {
			tree = append(tree, node)
		}
	}
	return tree
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

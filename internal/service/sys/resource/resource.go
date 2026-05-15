package resource

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"strings"

	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func init() {
	auth.RegisterPermission("sys:resource:page", "sys/resource", "BACKEND", "资源查询")
	auth.RegisterPermission("sys:resource:create", "sys/resource", "BACKEND", "资源新增")
	auth.RegisterPermission("sys:resource:modify", "sys/resource", "BACKEND", "资源修改")
	auth.RegisterPermission("sys:resource:remove", "sys/resource", "BACKEND", "资源删除")
	auth.RegisterPermission("sys:resource:detail", "sys/resource", "BACKEND", "资源详情")
	auth.RegisterPermission("sys:resource:export", "sys/resource", "BACKEND", "资源导出")
	auth.RegisterPermission("sys:resource:template", "sys/resource", "BACKEND", "资源导入模板")
	auth.RegisterPermission("sys:resource:import", "sys/resource", "BACKEND", "资源导入")
	auth.RegisterPermission("sys:resource:tree", "sys/resource", "BACKEND", "资源树查询")
}

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
	result := g.Map{
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
	}
	enrichCreatorUpdater(ctx, result)
	return result, nil
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
	// Read old entity to detect extra.permission_code changes
	oldRow, err := dao.SysResource.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil {
		return err
	}
	if oldRow == nil {
		return fmt.Errorf("数据不存在")
	}

	// Check for circular parent reference
	if parentId != "" && parentId != oldRow["parent_id"].String() {
		allRecords, err := dao.SysResource.Ctx().Ctx(ctx).All()
		if err == nil {
			parentMap := make(map[string]string)
			for _, r := range allRecords {
				parentMap[r["id"].String()] = r["parent_id"].String()
			}
			current := parentId
			for current != "" {
				if current == id {
					return fmt.Errorf("父级不能选择自身或子节点")
				}
				current = parentMap[current]
			}
		}
	}

	oldExtraStr := oldRow["extra"].String()

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
		if err != nil {
			return err
		}
	}

	// Sync RelRolePermission if extra.permission_code changed
	newExtraStr := oldExtraStr
	if extra != "" {
		newExtraStr = extra
	}
	oldCode := extractPermissionCode(oldExtraStr)
	newCode := extractPermissionCode(newExtraStr)
	if oldCode != newCode {
		roleRows, _ := dao.RelRoleResource.Ctx().Ctx(ctx).
			Where("resource_id", id).Fields("role_id").All()
		var roleIds []string
		for _, r := range roleRows {
			roleIds = append(roleIds, r["role_id"].String())
		}
		if len(roleIds) > 0 {
			if oldCode != "" {
				dao.RelRolePermission.Ctx().Ctx(ctx).
					Where("role_id in (?)", roleIds).
					Where("permission_code", oldCode).
					Delete()
			}
			if newCode != "" {
				existingRows, _ := dao.RelRolePermission.Ctx().Ctx(ctx).
					Where("role_id in (?)", roleIds).
					Where("permission_code", newCode).
					Fields("role_id").All()
				existingMap := make(map[string]bool)
				for _, r := range existingRows {
					existingMap[r["role_id"].String()] = true
				}
				for _, rid := range roleIds {
					if !existingMap[rid] {
						dao.RelRolePermission.Ctx().Ctx(ctx).Insert(g.Map{
							"id":              utility.GenerateID(),
							"role_id":         rid,
							"permission_code": newCode,
							"scope":           consts.PermissionScopeAll,
						})
					}
				}
			}
		}
	}
	return nil
}

// extractPermissionCode parses a JSON extra string and returns the permission_code value.
func extractPermissionCode(extra string) string {
	if extra == "" {
		return ""
	}
	var m map[string]interface{}
	if err := json.Unmarshal([]byte(extra), &m); err != nil {
		return ""
	}
	if code, ok := m["permission_code"].(string); ok {
		return code
	}
	return ""
}

func Remove(ctx context.Context, ids []string) error {
	// Collect all descendant IDs recursively (DFS)
	allIds := collectDescendantIds(ctx, ids)

	// Delete role-resource relations for all collected IDs
	dao.RelRoleResource.Ctx().Ctx(ctx).Where("resource_id in (?)", allIds).Delete()
	// Delete all resources (requested + descendants)
	_, err := dao.SysResource.Ctx().Ctx(ctx).WherePri(allIds).Delete()
	return err
}

// collectDescendantIds recursively collects all descendant resource IDs.
// It loads all resources, builds a children map, and performs DFS to find every descendant.
func collectDescendantIds(ctx context.Context, ids []string) []string {
	allRecords, err := dao.SysResource.Ctx().Ctx(ctx).All()
	if err != nil {
		return ids
	}
	childrenMap := make(map[string][]string)
	for _, r := range allRecords {
		pid := r["parent_id"].String()
		childrenMap[pid] = append(childrenMap[pid], r["id"].String())
	}
	allIds := make(map[string]struct{})
	for _, id := range ids {
		allIds[id] = struct{}{}
	}
	stack := make([]string, len(ids))
	copy(stack, ids)
	for len(stack) > 0 {
		parentId := stack[len(stack)-1]
		stack = stack[:len(stack)-1]
		for _, childId := range childrenMap[parentId] {
			if _, ok := allIds[childId]; !ok {
				allIds[childId] = struct{}{}
				stack = append(stack, childId)
			}
		}
	}
	result := make([]string, 0, len(allIds))
	for id := range allIds {
		result = append(result, id)
	}
	return result
}

func Export(ctx context.Context, exportType string, selectedIds []string, current, size int) (*bytes.Buffer, error) {
	var records []g.Map
	switch exportType {
	case "current":
		pageSize := size
		if pageSize <= 0 {
			pageSize = 10
		}
		pageCurrent := current
		if pageCurrent <= 0 {
			pageCurrent = 1
		}
		m := dao.SysResource.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysResource.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default:
		m := dao.SysResource.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}
	return utility.CreateExcelFromData(data, "资源数据")
}

func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"code", "name", "category", "type", "description", "parent_id", "route_path", "component_path", "redirect_path", "icon", "color", "is_visible", "is_cache", "is_affix", "is_breadcrumb", "external_url", "extra", "status", "sort_code"}
	return utility.CreateExcelTemplate(headers, "资源数据")
}

func Import(ctx context.Context, file ghttp.UploadFile) (g.Map, error) {
	f, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}
	defer f.Close()

	if file.Size > 5*1024*1024 {
		return nil, fmt.Errorf("文件大小不能超过5MB")
	}
	if !strings.HasSuffix(strings.ToLower(file.Filename), ".xlsx") {
		return nil, fmt.Errorf("仅支持.xlsx格式文件")
	}

	content, err := io.ReadAll(f)
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}

	rows, err := utility.ParseExcelFromBytes(content, true)
	if err != nil {
		return nil, err
	}

	if len(rows) == 0 {
		return nil, fmt.Errorf("导入数据不能为空")
	}

	imported := 0
	for _, row := range rows {
		id := utility.GenerateID()
		_, err := dao.SysResource.Ctx().Ctx(ctx).Insert(g.Map{
			"id":             id,
			"code":           row["code"],
			"name":           row["name"],
			"category":       row["category"],
			"type":           row["type"],
			"description":    row["description"],
			"parent_id":      row["parent_id"],
			"route_path":     row["route_path"],
			"component_path": row["component_path"],
			"redirect_path":  row["redirect_path"],
			"icon":           row["icon"],
			"color":          row["color"],
			"is_visible":     row["is_visible"],
			"is_cache":       row["is_cache"],
			"is_affix":       row["is_affix"],
			"is_breadcrumb":  row["is_breadcrumb"],
			"external_url":   row["external_url"],
			"extra":          row["extra"],
			"status":         row["status"],
			"sort_code":      row["sort_code"],
			"created_by":     getLoginId(ctx),
		})
		if err == nil {
			imported++
		}
	}

	return g.Map{
		"total":   imported,
		"message": fmt.Sprintf("成功导入%d条数据", imported),
	}, nil
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

func cleanMapForExport(m g.Map) map[string]interface{} {
	result := make(map[string]interface{}, len(m))
	for k, v := range m {
		if v == nil {
			result[k] = ""
		} else {
			result[k] = v
		}
	}
	delete(result, "id")
	return result
}

func enrichCreatorUpdater(ctx context.Context, item g.Map) {
	if id, ok := item["created_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["created_name"] = row["nickname"].String()
		}
	}
	if id, ok := item["updated_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["updated_name"] = row["nickname"].String()
		}
	}
}

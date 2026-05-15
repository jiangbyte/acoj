package org

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"
	"strings"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func init() {
	auth.RegisterPermission("sys:org:page", "sys/org", "BACKEND", "组织查询")
	auth.RegisterPermission("sys:org:create", "sys/org", "BACKEND", "组织新增")
	auth.RegisterPermission("sys:org:modify", "sys/org", "BACKEND", "组织修改")
	auth.RegisterPermission("sys:org:remove", "sys/org", "BACKEND", "组织删除")
	auth.RegisterPermission("sys:org:detail", "sys/org", "BACKEND", "组织详情")
	auth.RegisterPermission("sys:org:export", "sys/org", "BACKEND", "组织导出")
	auth.RegisterPermission("sys:org:template", "sys/org", "BACKEND", "组织导入模板")
	auth.RegisterPermission("sys:org:import", "sys/org", "BACKEND", "组织导入")
	auth.RegisterPermission("sys:org:grant-role", "sys/org", "BACKEND", "组织分配角色")
	auth.RegisterPermission("sys:org:own-roles", "sys/org", "BACKEND", "组织角色ID列表")
	auth.RegisterPermission("sys:org:tree", "sys/org", "BACKEND", "组织树")
}

func Page(ctx context.Context, keyword, status, parentId string, current, size int) (*utility.PageRes, error) {
	m := dao.SysOrg.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if parentId != "" {
		m = m.Where("(parent_id = ? OR id = ?)", parentId, parentId)
	}
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

	all, err := m.Page(current, size).All()
	if err != nil {
		return nil, err
	}
	list := all.List()

	batchEnrichNames(ctx, list)
	return utility.NewPageRes(list, count, current, size), nil
}

func Create(ctx context.Context, code, name, category, parentId, description, status string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysOrg.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"code":        code,
		"name":        name,
		"category":    category,
		"parent_id":   parentId,
		"description": description,
		"status":      ifEmpty(status, consts.StatusEnabled),
		"sort_code":   sortCode,
		"created_by":  loginId,
	})
	return err
}

func Modify(ctx context.Context, id, code, name, category, parentId, description, status string, sortCode int) error {
	entity := findById(ctx, id)
	if entity == nil {
		return errors.New("数据不存在")
	}

	if parentId != "" && parentId != entity["parent_id"].(string) {
		if err := checkCircularParent(ctx, id, parentId); err != nil {
			return err
		}
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
	if description != "" {
		updateData["description"] = description
	}
	if status != "" {
		updateData["status"] = status
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}
	if parentId != "" {
		updateData["parent_id"] = parentId
	}

	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysOrg.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	allIds := collectDescendantIds(ctx, ids)

	// Check for linked users
	count, err := dao.SysUser.Ctx().Ctx(ctx).Where("org_id in (?)", allIds).Count()
	if err != nil {
		return err
	}
	if count > 0 {
		return errors.New("组织存在关联用户，无法删除")
	}

	// Check for linked groups
	count, err = dao.SysGroup.Ctx().Ctx(ctx).Where("org_id in (?)", allIds).Count()
	if err != nil {
		return err
	}
	if count > 0 {
		return errors.New("组织下存在用户组，无法删除")
	}

	// Delete org-role relations
	_, err = dao.RelOrgRole.Ctx().Ctx(ctx).Where("org_id in (?)", allIds).Delete()
	if err != nil {
		return err
	}

	// Nullify position.org_id references
	_, err = dao.SysPosition.Ctx().Ctx(ctx).Where("org_id in (?)", allIds).Update(g.Map{"org_id": nil})
	if err != nil {
		return err
	}

	// Delete all descendants + original ids
	_, err = dao.SysOrg.Ctx().Ctx(ctx).WherePri(allIds).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysOrg.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	result := g.Map{
		"id":          row["id"].String(),
		"code":        row["code"].String(),
		"name":        row["name"].String(),
		"category":    row["category"].String(),
		"parent_id":   row["parent_id"].String(),
		"description": row["description"].String(),
		"status":      row["status"].String(),
		"sort_code":   row["sort_code"].Int(),
		"created_at":  row["created_at"].String(),
		"created_by":  row["created_by"].String(),
		"updated_at":  row["updated_at"].String(),
		"updated_by":  row["updated_by"].String(),
	}
	enrichCreatorUpdater(ctx, result)
	return result, nil
}

func Tree(ctx context.Context, category string) ([]g.Map, error) {
	m := dao.SysOrg.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if category != "" {
		m = m.Where("category", category)
	}
	rows, err := m.All()
	if err != nil {
		return nil, err
	}

	orgMap := make(map[string]g.Map)
	for _, r := range rows {
		orgMap[r["id"].String()] = g.Map{
			"id":          r["id"].String(),
			"code":        r["code"].String(),
			"name":        r["name"].String(),
			"category":    r["category"].String(),
			"parent_id":   r["parent_id"].String(),
			"description": r["description"].String(),
			"status":      r["status"].String(),
			"sort_code":   r["sort_code"].Int(),
			"children":    []g.Map{},
		}
	}

	var tree []g.Map
	for _, r := range rows {
		node := orgMap[r["id"].String()]
		pid := r["parent_id"].String()
		if pid != "" && orgMap[pid] != nil {
			children, _ := orgMap[pid]["children"].([]g.Map)
			children = append(children, node)
			orgMap[pid]["children"] = children
		} else {
			tree = append(tree, node)
		}
	}
	return tree, nil
}

func GrantOrgRole(ctx context.Context, orgId string, roleIds []string, scope, customScopeGroupIds, customScopeOrgIds string) error {
	loginId := getLoginId(ctx)
	dao.RelOrgRole.Ctx().Ctx(ctx).Where("org_id", orgId).Delete()
	for _, rid := range roleIds {
		data := g.Map{
			"id":         utility.GenerateID(),
			"org_id":     orgId,
			"role_id":    rid,
			"created_by": loginId,
		}
		if scope != "" {
			data["scope"] = scope
		}
		if customScopeGroupIds != "" {
			data["custom_scope_group_ids"] = customScopeGroupIds
		}
		if customScopeOrgIds != "" {
			data["custom_scope_org_ids"] = customScopeOrgIds
		}
		dao.RelOrgRole.Ctx().Ctx(ctx).Insert(data)
	}
	return nil
}

func GetOrgRoleIds(ctx context.Context, orgId string) ([]string, error) {
	rows, err := dao.RelOrgRole.Ctx().Ctx(ctx).
		Where("org_id", orgId).Fields("role_id").All()
	if err != nil {
		return nil, err
	}
	var ids []string
	for _, r := range rows {
		ids = append(ids, r["role_id"].String())
	}
	return ids, nil
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
		m := dao.SysOrg.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysOrg.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default:
		m := dao.SysOrg.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}
	return utility.CreateExcelFromData(data, "组织数据")
}

func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"code", "name", "category", "parent_id", "description", "status", "sort_code"}
	return utility.CreateExcelTemplate(headers, "组织数据")
}

func Import(ctx context.Context, file ghttp.UploadFile) (g.Map, error) {
	f, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}
	defer f.Close()

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
		_, err := dao.SysOrg.Ctx().Ctx(ctx).Insert(g.Map{
			"id":          id,
			"code":        row["code"],
			"name":        row["name"],
			"category":    row["category"],
			"parent_id":   row["parent_id"],
			"description": row["description"],
			"status":      row["status"],
			"sort_code":   row["sort_code"],
			"created_by":  getLoginId(ctx),
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

func findById(ctx context.Context, id string) g.Map {
	row, err := dao.SysOrg.Ctx().Ctx(ctx).WherePri(id).One()
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

func batchEnrichNames(ctx context.Context, list []g.Map) {
	for _, item := range list {
		enrichCreatorUpdater(ctx, item)
	}
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

func checkCircularParent(ctx context.Context, entityId, newParentId string) error {
	if newParentId == "" {
		return nil
	}
	all, err := dao.SysOrg.Ctx().Ctx(ctx).All()
	if err != nil {
		return err
	}
	parentMap := make(map[string]string)
	for _, r := range all {
		parentMap[r["id"].String()] = r["parent_id"].String()
	}
	current := newParentId
	for current != "" {
		if current == entityId {
			return errors.New("父级不能选择自身或子节点")
		}
		current = parentMap[current]
	}
	return nil
}

func collectDescendantIds(ctx context.Context, ids []string) []string {
	all, err := dao.SysOrg.Ctx().Ctx(ctx).All()
	if err != nil {
		return ids
	}
	childrenMap := make(map[string][]string)
	for _, r := range all {
		pid := r["parent_id"].String()
		if pid == "" {
			pid = "0"
		}
		childrenMap[pid] = append(childrenMap[pid], r["id"].String())
	}

	allIds := make(map[string]bool)
	for _, id := range ids {
		allIds[id] = true
	}

	stack := make([]string, len(ids))
	copy(stack, ids)

	for len(stack) > 0 {
		parentId := stack[len(stack)-1]
		stack = stack[:len(stack)-1]
		for _, childId := range childrenMap[parentId] {
			if !allIds[childId] {
				allIds[childId] = true
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

func containsSubstring(s, substr string) bool {
	return strings.Contains(strings.ToLower(s), strings.ToLower(substr))
}

package role

import (
	"bytes"
	"context"
	"encoding/json"
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
	auth.RegisterPermission("sys:role:page", "sys/role", "BACKEND", "角色查询")
	auth.RegisterPermission("sys:role:create", "sys/role", "BACKEND", "角色新增")
	auth.RegisterPermission("sys:role:modify", "sys/role", "BACKEND", "角色修改")
	auth.RegisterPermission("sys:role:remove", "sys/role", "BACKEND", "角色删除")
	auth.RegisterPermission("sys:role:detail", "sys/role", "BACKEND", "角色详情")
	auth.RegisterPermission("sys:role:export", "sys/role", "BACKEND", "角色导出")
	auth.RegisterPermission("sys:role:template", "sys/role", "BACKEND", "角色导入模板")
	auth.RegisterPermission("sys:role:import", "sys/role", "BACKEND", "角色导入")
	auth.RegisterPermission("sys:role:grant-permission", "sys/role", "BACKEND", "角色分配权限")
	auth.RegisterPermission("sys:role:grant-resource", "sys/role", "BACKEND", "角色分配资源")
	auth.RegisterPermission("sys:role:own-permission", "sys/role", "BACKEND", "角色权限编码列表")
	auth.RegisterPermission("sys:role:own-permission-detail", "sys/role", "BACKEND", "角色权限详情")
	auth.RegisterPermission("sys:role:own-resource", "sys/role", "BACKEND", "角色资源ID列表")
}

func Page(ctx context.Context, current, size int) (*utility.PageRes, error) {
	m := dao.SysRole.Ctx().Ctx(ctx).OrderAsc("sort_code")
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

func Create(ctx context.Context, code, name, category, description, status string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysRole.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"code":        code,
		"name":        name,
		"category":    ifEmpty(category, consts.PermissionCategoryBackend),
		"description": description,
		"status":      ifEmpty(status, consts.StatusEnabled),
		"sort_code":   sortCode,
		"created_by":  loginId,
	})
	return err
}

func Modify(ctx context.Context, id, code, name, category, description, status string, sortCode int) error {
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
	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysRole.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	// Check for linked users
	count, _ := dao.RelUserRole.Ctx().Ctx(ctx).Where("role_id in (?)", ids).Count()
	if count > 0 {
		return errors.New("角色存在关联用户，无法删除")
	}
	count, _ = dao.RelOrgRole.Ctx().Ctx(ctx).Where("role_id in (?)", ids).Count()
	if count > 0 {
		return errors.New("角色已被组织使用，无法删除")
	}

	dao.RelRolePermission.Ctx().Ctx(ctx).Where("role_id in (?)", ids).Delete()
	dao.RelRoleResource.Ctx().Ctx(ctx).Where("role_id in (?)", ids).Delete()
	dao.RelUserRole.Ctx().Ctx(ctx).Where("role_id in (?)", ids).Delete()
	dao.RelOrgRole.Ctx().Ctx(ctx).Where("role_id in (?)", ids).Delete()

	_, err := dao.SysRole.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysRole.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	result := g.Map{
		"id":          row["id"].String(),
		"code":        row["code"].String(),
		"name":        row["name"].String(),
		"category":    row["category"].String(),
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

func GrantPermissions(ctx context.Context, roleId string, permissions []map[string]interface{}) error {
	loginId := getLoginId(ctx)
	dao.RelRolePermission.Ctx().Ctx(ctx).Where("role_id", roleId).Delete()
	for _, p := range permissions {
		code, _ := p["permission_code"].(string)
		scope, _ := p["scope"].(string)
		if scope == "" {
			scope = consts.PermissionScopeAll
		}
		cgIds, _ := p["custom_scope_group_ids"].(string)
		coIds, _ := p["custom_scope_org_ids"].(string)
		dao.RelRolePermission.Ctx().Ctx(ctx).Insert(g.Map{
			"id":                     utility.GenerateID(),
			"role_id":                roleId,
			"permission_code":        code,
			"scope":                  scope,
			"custom_scope_group_ids": cgIds,
			"custom_scope_org_ids":   coIds,
			"created_by":             loginId,
		})
	}
	return nil
}

func GrantResources(ctx context.Context, roleId string, resourceIds []string, permissions []map[string]interface{}) error {
	loginId := getLoginId(ctx)
	dao.RelRoleResource.Ctx().Ctx(ctx).Where("role_id", roleId).Delete()

	// Deduplicate resource_ids like Python does
	seen := make(map[string]bool)
	var uniqueIds []string
	for _, rid := range resourceIds {
		if !seen[rid] {
			seen[rid] = true
			uniqueIds = append(uniqueIds, rid)
		}
	}

	for _, rid := range uniqueIds {
		dao.RelRoleResource.Ctx().Ctx(ctx).Insert(g.Map{
			"id":          utility.GenerateID(),
			"role_id":     roleId,
			"resource_id": rid,
			"created_by":  loginId,
		})
	}

	// Auto-grant permissions linked via resource.extra -> permission_code
	if len(uniqueIds) > 0 {
		rows, err := dao.SysResource.Ctx().Ctx(ctx).
			WherePri(uniqueIds).
			Where("extra != '' AND extra IS NOT NULL").
			All()
		if err == nil {
			// Build permission_code -> scope override map from provided permissions
			scopeMap := make(map[string]g.Map)
			for _, p := range permissions {
				code, _ := p["permission_code"].(string)
				if code != "" {
					scopeMap[code] = p
				}
			}

			var newPerms []g.Map
			seen := make(map[string]bool)
			for _, r := range rows {
				extraStr := r["extra"].String()
				pcode := extractPermissionCode(extraStr)
				if pcode == "" || seen[pcode] {
					continue
				}
				seen[pcode] = true
				scope := consts.PermissionScopeAll
				var cgIds, coIds string
				if override, ok := scopeMap[pcode]; ok {
					if s, ok2 := override["scope"].(string); ok2 && s != "" {
						scope = s
					}
					if s, ok2 := override["custom_scope_group_ids"].(string); ok2 {
						cgIds = s
					}
					if s, ok2 := override["custom_scope_org_ids"].(string); ok2 {
						coIds = s
					}
				}
				newPerms = append(newPerms, g.Map{
					"permission_code":        pcode,
					"scope":                  scope,
					"custom_scope_group_ids": cgIds,
					"custom_scope_org_ids":   coIds,
				})
			}

			for _, perm := range newPerms {
				addMissingPermission(ctx, roleId, perm)
			}
		}
	}

	return nil
}

// addMissingPermission inserts a permission for a role only if it does not already exist.
func addMissingPermission(ctx context.Context, roleId string, perm g.Map) {
	code, _ := perm["permission_code"].(string)
	if code == "" {
		return
	}
	count, err := dao.RelRolePermission.Ctx().Ctx(ctx).
		Where("role_id", roleId).
		Where("permission_code", code).
		Count()
	if err != nil || count > 0 {
		return
	}
	scope, _ := perm["scope"].(string)
	if scope == "" {
		scope = consts.PermissionScopeAll
	}
	cgIds, _ := perm["custom_scope_group_ids"].(string)
	coIds, _ := perm["custom_scope_org_ids"].(string)
	dao.RelRolePermission.Ctx().Ctx(ctx).Insert(g.Map{
		"id":                     utility.GenerateID(),
		"role_id":                roleId,
		"permission_code":        code,
		"scope":                  scope,
		"custom_scope_group_ids": cgIds,
		"custom_scope_org_ids":   coIds,
		"created_by":             getLoginId(ctx),
	})
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

func GetPermissionCodes(ctx context.Context, roleId string) ([]string, error) {
	rows, err := dao.RelRolePermission.Ctx().Ctx(ctx).
		Where("role_id", roleId).Fields("permission_code").All()
	if err != nil {
		return nil, err
	}
	var codes []string
	for _, r := range rows {
		codes = append(codes, r["permission_code"].String())
	}
	return codes, nil
}

func GetPermissionDetails(ctx context.Context, roleId string) ([]g.Map, error) {
	rows, err := dao.RelRolePermission.Ctx().Ctx(ctx).
		Where("role_id", roleId).
		Fields("permission_code", "scope", "custom_scope_group_ids", "custom_scope_org_ids").All()
	if err != nil {
		return nil, err
	}
	var result []g.Map
	for _, r := range rows {
		scope := r["scope"].String()
		if scope == "" {
			scope = consts.PermissionScopeAll
		}
		result = append(result, g.Map{
			"permission_code":        r["permission_code"].String(),
			"scope":                  scope,
			"custom_scope_group_ids": r["custom_scope_group_ids"].String(),
			"custom_scope_org_ids":   r["custom_scope_org_ids"].String(),
		})
	}
	return result, nil
}

func GetResourceIds(ctx context.Context, roleId string) ([]string, error) {
	rows, err := dao.RelRoleResource.Ctx().Ctx(ctx).
		Where("role_id", roleId).Fields("resource_id").All()
	if err != nil {
		return nil, err
	}
	var ids []string
	for _, r := range rows {
		ids = append(ids, r["resource_id"].String())
	}
	return ids, nil
}

// Export exports role data as an Excel file.
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
		m := dao.SysRole.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysRole.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default: // "all"
		m := dao.SysRole.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}

	return utility.CreateExcelFromData(data, "角色数据")
}

// DownloadTemplate downloads an import template Excel file.
func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"code", "name", "category", "description", "status", "sort_code"}
	return utility.CreateExcelTemplate(headers, "角色数据")
}

// Import imports role data from an uploaded Excel file.
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
		_, err := dao.SysRole.Ctx().Ctx(ctx).Insert(g.Map{
			"id":          id,
			"code":        row["code"],
			"name":        row["name"],
			"category":    row["category"],
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

// cleanMapForExport removes nil values and converts types for Excel export.
func cleanMapForExport(m g.Map) map[string]interface{} {
	result := make(map[string]interface{}, len(m))
	for k, v := range m {
		if v == nil {
			result[k] = ""
		} else {
			result[k] = v
		}
	}
	// Remove sensitive/internal fields
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

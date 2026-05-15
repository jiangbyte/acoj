package role

import (
	"context"
	"errors"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func Page(ctx context.Context, current, size int) (*utility.PageRes, error) {
	m := dao.SysRole.Ctx().Ctx(ctx).OrderAsc("sort_code")
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
	return g.Map{
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
	}, nil
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

func GrantResources(ctx context.Context, roleId string, resourceIds []string) error {
	loginId := getLoginId(ctx)
	dao.RelRoleResource.Ctx().Ctx(ctx).Where("role_id", roleId).Delete()
	for _, rid := range resourceIds {
		dao.RelRoleResource.Ctx().Ctx(ctx).Insert(g.Map{
			"id":          utility.GenerateID(),
			"role_id":     roleId,
			"resource_id": rid,
			"created_by":  loginId,
		})
	}
	return nil
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

package auth

import (
	"context"
	"encoding/json"

	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
)

// PermissionInterface 运行时权限查询接口，请求时从 DB 查询
type PermissionInterface struct{}

func NewPermissionInterface() *PermissionInterface {
	return &PermissionInterface{}
}

func (p *PermissionInterface) getRoleIds(ctx context.Context, loginId string) []string {
	roleIds := make(map[string]bool)

	rows, _ := dao.RelUserRole.Ctx().Ctx(ctx).Where("user_id", loginId).Fields("role_id").All()
	for _, row := range rows {
		roleIds[row["role_id"].String()] = true
	}

	user, err := dao.SysUser.Ctx().Ctx(ctx).Where("id", loginId).One()
	if err == nil && user != nil {
		orgId := user["org_id"].String()
		if orgId != "" {
			rows, _ := dao.RelOrgRole.Ctx().Ctx(ctx).Where("org_id", orgId).Fields("role_id").All()
			for _, row := range rows {
				roleIds[row["role_id"].String()] = true
			}
		}
	}

	result := make([]string, 0, len(roleIds))
	for id := range roleIds {
		result = append(result, id)
	}
	return result
}

func (p *PermissionInterface) isGroupScope(scope string) bool {
	return scope == consts.PermissionScopeGroup ||
		scope == consts.PermissionScopeGroupAndBelow ||
		scope == consts.PermissionScopeCustomGroup
}

func (p *PermissionInterface) isOrgScope(scope string) bool {
	return scope == consts.PermissionScopeOrg ||
		scope == consts.PermissionScopeOrgAndBelow ||
		scope == consts.PermissionScopeCustomOrg
}

func (p *PermissionInterface) mergeDimension(cur map[string]interface{}, scopeKey, idsKey, newScope string, newIds []string) {
	curScope, _ := cur[scopeKey].(string)
	if curScope != "" {
		if curScope == consts.PermissionScopeSelf {
			return
		}
		if newScope == consts.PermissionScopeSelf {
			cur[scopeKey] = consts.PermissionScopeSelf
			cur[idsKey] = []string{}
			return
		}
		restricted := mostRestrictiveScope(curScope, newScope)
		cur[scopeKey] = restricted
		if restricted == consts.PermissionScopeCustomGroup || restricted == consts.PermissionScopeCustomOrg {
			existing, _ := cur[idsKey].([]string)
			merged := make(map[string]bool)
			for _, id := range existing {
				merged[id] = true
			}
			for _, id := range newIds {
				merged[id] = true
			}
			result := make([]string, 0, len(merged))
			for id := range merged {
				result = append(result, id)
			}
			cur[idsKey] = result
		}
	} else {
		cur[scopeKey] = newScope
		cur[idsKey] = newIds
	}
}

func (p *PermissionInterface) mergeScope(result map[string]map[string]interface{}, priority string, rows []gdb.Record) {
	for _, row := range rows {
		code := row["permission_code"].String()
		scope := row["scope"].String()
		if scope == "" {
			scope = consts.PermissionScopeAll
		}

		var cgIds, coIds []string
		if cgRaw := row["custom_scope_group_ids"].String(); cgRaw != "" {
			json.Unmarshal(gconv.Bytes(cgRaw), &cgIds)
		}
		if coRaw := row["custom_scope_org_ids"].String(); coRaw != "" {
			json.Unmarshal(gconv.Bytes(coRaw), &coIds)
		}

		isGroup := p.isGroupScope(scope)
		isOrg := p.isOrgScope(scope)
		isAll := scope == consts.PermissionScopeAll
		isSelf := scope == consts.PermissionScopeSelf

		if cur, ok := result[code]; !ok {
			entry := map[string]interface{}{
				"group_scope":      nil,
				"org_scope":        nil,
				"custom_group_ids": []string{},
				"custom_org_ids":   []string{},
				"priority":         priority,
			}
			if isAll || isSelf || isGroup {
				entry["group_scope"] = scope
			}
			if isAll || isSelf || isOrg {
				entry["org_scope"] = scope
			}
			entry["custom_group_ids"] = cgIds
			entry["custom_org_ids"] = coIds
			result[code] = entry
		} else {
			curPriority, _ := cur["priority"].(string)
			if comparePriority(priority, curPriority) < 0 {
				cur["group_scope"] = nil
				cur["org_scope"] = nil
				if isAll || isSelf || isGroup {
					cur["group_scope"] = scope
				}
				if isAll || isSelf || isOrg {
					cur["org_scope"] = scope
				}
				cur["custom_group_ids"] = cgIds
				cur["custom_org_ids"] = coIds
				cur["priority"] = priority
			} else if priority == curPriority {
				if isGroup || isAll || isSelf {
					p.mergeDimension(cur, "group_scope", "custom_group_ids", scope, cgIds)
				}
				if isOrg || isAll || isSelf {
					p.mergeDimension(cur, "org_scope", "custom_org_ids", scope, coIds)
				}
			}
		}
	}
}

func (p *PermissionInterface) getAllPermissionsFromRedis(ctx context.Context) []string {
	data, err := g.Redis().Get(ctx, consts.PermissionCacheKey)
	if err != nil || data.IsNil() {
		return nil
	}
	var tree map[string]map[string]interface{}
	if err := json.Unmarshal(data.Bytes(), &tree); err != nil {
		return nil
	}
	var codes []string
	for _, perms := range tree {
		for code := range perms {
			codes = append(codes, code)
		}
	}
	return codes
}

// GetPermissionList 返回用户的权限码列表，SUPER_ADMIN 返回所有权限
func (p *PermissionInterface) GetPermissionList(ctx context.Context, loginId, loginType string) ([]string, error) {
	roleList, _ := p.GetRoleList(ctx, loginId, loginType)
	for _, role := range roleList {
		if role == consts.SuperAdminCode {
			return p.getAllPermissionsFromRedis(ctx), nil
		}
	}

	roleIds := p.getRoleIds(ctx, loginId)
	permSet := make(map[string]bool)

	if loginType == consts.LoginTypeBusiness || loginType == consts.LoginTypeConsumer {
		if len(roleIds) > 0 {
			rows, _ := dao.RelRolePermission.Ctx().Ctx(ctx).
				Where("role_id in (?)", roleIds).Fields("permission_code").All()
			for _, row := range rows {
				permSet[row["permission_code"].String()] = true
			}
		}

		rows, _ := dao.RelUserPermission.Ctx().Ctx(ctx).
			Where("user_id", loginId).Fields("permission_code").All()
		for _, row := range rows {
			permSet[row["permission_code"].String()] = true
		}
	}

	result := make([]string, 0, len(permSet))
	for code := range permSet {
		result = append(result, code)
	}
	return result, nil
}

// GetRoleList 返回用户的角色编码列表
func (p *PermissionInterface) GetRoleList(ctx context.Context, loginId, loginType string) ([]string, error) {
	rows, err := dao.SysRole.Ctx().Ctx(ctx).
		InnerJoin("rel_user_role", "sys_role.id = rel_user_role.role_id").
		Where("rel_user_role.user_id", loginId).
		Fields("sys_role.code").All()
	if err != nil {
		return nil, err
	}
	codes := make([]string, 0, len(rows))
	for _, row := range rows {
		codes = append(codes, row["code"].String())
	}
	return codes, nil
}

// GetPermissionScopeMap 返回用户权限的数据范围映射
func (p *PermissionInterface) GetPermissionScopeMap(ctx context.Context, loginId, loginType string) (map[string]map[string]interface{}, error) {
	permScope := make(map[string]map[string]interface{})

	roleList, _ := p.GetRoleList(ctx, loginId, loginType)
	for _, role := range roleList {
		if role == consts.SuperAdminCode {
			allCodes := p.getAllPermissionsFromRedis(ctx)
			result := make(map[string]map[string]interface{})
			for _, code := range allCodes {
				result[code] = map[string]interface{}{
					"group_scope":      consts.PermissionScopeAll,
					"org_scope":        consts.PermissionScopeAll,
					"custom_group_ids": []string{},
					"custom_org_ids":   []string{},
				}
			}
			return result, nil
		}
	}

	// Path P1: User -> Role -> Permission
	rows, _ := dao.RelUserRole.Ctx().Ctx(ctx).Where("user_id", loginId).Fields("role_id").All()
	var roleIds []string
	for _, row := range rows {
		roleIds = append(roleIds, row["role_id"].String())
	}
	if len(roleIds) > 0 {
		permRows, _ := dao.RelRolePermission.Ctx().Ctx(ctx).
			Where("role_id in (?)", roleIds).
			Fields("permission_code", "scope", "custom_scope_group_ids", "custom_scope_org_ids").All()
		p.mergeScope(permScope, consts.PermissionPathUserRole, permRows)
	}

	// Path P0: User -> Direct Permission
	directRows, _ := dao.RelUserPermission.Ctx().Ctx(ctx).
		Where("user_id", loginId).
		Fields("permission_code", "scope", "custom_scope_group_ids", "custom_scope_org_ids").All()
	p.mergeScope(permScope, consts.PermissionPathDirect, directRows)

	// Path P2: User -> Org -> Role -> Permission
	if loginType == consts.LoginTypeBusiness {
		user, _ := dao.SysUser.Ctx().Ctx(ctx).Where("id", loginId).One()
		if user != nil && user["org_id"].String() != "" {
			orgId := user["org_id"].String()
			permRows, _ := dao.RelRolePermission.Ctx().Ctx(ctx).
				LeftJoin("rel_org_role", "rel_org_role.role_id = rel_role_permission.role_id").
				Where("rel_org_role.org_id", orgId).
				Fields(
					"rel_role_permission.permission_code",
					"IFNULL(rel_org_role.scope, rel_role_permission.scope) as scope",
					"IFNULL(rel_org_role.custom_scope_group_ids, rel_role_permission.custom_scope_group_ids) as custom_scope_group_ids",
					"IFNULL(rel_org_role.custom_scope_org_ids, rel_role_permission.custom_scope_org_ids) as custom_scope_org_ids",
				).All()
			p.mergeScope(permScope, consts.PermissionPathOrgRole, permRows)
		}
	}

	result := make(map[string]map[string]interface{})
	for k, v := range permScope {
		result[k] = map[string]interface{}{
			"group_scope":      v["group_scope"],
			"org_scope":        v["org_scope"],
			"custom_group_ids": v["custom_group_ids"],
			"custom_org_ids":   v["custom_org_ids"],
		}
	}
	return result, nil
}

func comparePriority(a, b string) int {
	if a < b {
		return -1
	}
	if a > b {
		return 1
	}
	return 0
}

// mostRestrictiveScope 返回两个 scope 中更严格的
func mostRestrictiveScope(a, b string) string {
	order := []string{
		consts.PermissionScopeAll,
		consts.PermissionScopeOrg,
		consts.PermissionScopeOrgAndBelow,
		consts.PermissionScopeGroup,
		consts.PermissionScopeGroupAndBelow,
		consts.PermissionScopeCustomOrg,
		consts.PermissionScopeCustomGroup,
		consts.PermissionScopeSelf,
	}
	idx := make(map[string]int)
	for i, s := range order {
		idx[s] = i
	}
	ia, oka := idx[a]
	ib, okb := idx[b]
	if !oka {
		return b
	}
	if !okb {
		return a
	}
	if ia > ib {
		return a
	}
	return b
}

package auth

import (
	"context"
	"encoding/json"
	"log"
	"strings"

	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/core/enums"
	"hei-gin/ent/gen/relrolepermission"
	"hei-gin/ent/gen/reluserpermission"
	"hei-gin/ent/gen/reluserrole"
	"hei-gin/ent/gen/sysrole"
)

// PermissionPathEnum constants for scope resolution priority.
const (
	PermissionPathDIRECT   = "P0" // User → Direct Permission (highest priority)
	PermissionPathUserRole = "P1" // User → Role → Permission
)

// PermissionScopeItem represents scope info for a single permission.
type PermissionScopeItem struct {
	GroupScope     string   `json:"group_scope"`
	OrgScope       string   `json:"org_scope"`
	CustomGroupIDs []string `json:"custom_group_ids"`
	CustomOrgIDs   []string `json:"custom_org_ids"`
}

// PermissionInterfaceImpl provides runtime permission loading from the database.
// It resolves 2 permission paths:
//   - P0 (DIRECT): User → RelUserPermission (highest priority)
//   - P1 (USER_ROLE): User → Role → RelRolePermission
type PermissionInterfaceImpl struct{}

var PermissionInterface = &PermissionInterfaceImpl{}

func (p *PermissionInterfaceImpl) GetPermissionList(loginID, loginType string) []string {
	if db.Client == nil {
		return nil
	}

	// Check SUPER_ADMIN
	roleList := p.GetRoleList(loginID, loginType)
	for _, role := range roleList {
		if role == constants.SuperAdminCode {
			return p.getAllPermissionsFromCache()
		}
	}

	seen := map[string]bool{}
	var result []string

	// P0: Direct permissions (highest priority)
	direct := p.getDirectPermissions(loginID)
	for _, perm := range direct {
		if !seen[perm] {
			seen[perm] = true
			result = append(result, perm)
		}
	}

	// P1: Role-based permissions (lower priority than P0)
	rolePerms := p.getRolePermissions(loginID)
	for _, perm := range rolePerms {
		if !seen[perm] {
			seen[perm] = true
			result = append(result, perm)
		}
	}

	return result
}

func (p *PermissionInterfaceImpl) GetRoleList(loginID, loginType string) []string {
	if db.Client == nil {
		return nil
	}

	ctx := context.Background()
	roleRels, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserIDEQ(loginID)).
		Select(reluserrole.FieldRoleID).
		All(ctx)
	if err != nil {
		log.Printf("[Permission] query roles error: %v", err)
		return nil
	}

	roleIDs := make([]string, len(roleRels))
	for i, r := range roleRels {
		roleIDs[i] = r.RoleID
	}
	if len(roleIDs) == 0 {
		return nil
	}

	roles, err := db.Client.SysRole.Query().
		Where(sysrole.IDIn(roleIDs...)).
		Select(sysrole.FieldCode).
		All(ctx)
	if err != nil {
		log.Printf("[Permission] query roles error: %v", err)
		return nil
	}

	codes := make([]string, len(roles))
	for i, r := range roles {
		codes[i] = r.Code
	}
	return codes
}

// GetPermissionScopeMap returns a map of permission_code → scope info.
// SUPER_ADMIN gets ALL scope on every permission in the cache.
// For normal users, it merges P0 (direct) and P1 (role-based) with PriorityMerge.
func (p *PermissionInterfaceImpl) GetPermissionScopeMap(loginID, loginType string) map[string]PermissionScopeItem {
	if db.Client == nil {
		return nil
	}

	ctx := context.Background()

	// SUPER_ADMIN gets ALL scope on every known permission
	roleList := p.GetRoleList(loginID, loginType)
	for _, role := range roleList {
		if role == constants.SuperAdminCode {
			allCodes := p.getAllPermissionsFromCache()
			result := make(map[string]PermissionScopeItem, len(allCodes))
			for _, code := range allCodes {
				result[code] = PermissionScopeItem{
					GroupScope: string(enums.DataScopeAll),
					OrgScope:   string(enums.DataScopeAll),
				}
			}
			return result
		}
	}

	permScope := map[string]*scopeMergeEntry{}

	// P1: User → Role → Permission (rel_role_permission)
	roleIDs, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserIDEQ(loginID)).
		Select(reluserrole.FieldRoleID).
		All(ctx)
	if err == nil && len(roleIDs) > 0 {
		rids := make([]string, len(roleIDs))
		for i, r := range roleIDs {
			rids[i] = r.RoleID
		}

		rpRows, err := db.Client.RelRolePermission.Query().
			Where(relrolepermission.RoleIDIn(rids...)).
			All(ctx)
		if err == nil {
			for _, rp := range rpRows {
				entry := &scopeMergeEntry{
					Priority:       PermissionPathUserRole,
					GroupScope:     rp.Scope,
					OrgScope:       rp.Scope,
					CustomGroupIDs: parseJSONList(rp.CustomScopeGroupIds),
					CustomOrgIDs:   parseJSONList(rp.CustomScopeOrgIds),
				}

				if existing, exists := permScope[rp.PermissionCode]; exists {
					mergeEntry(existing, entry)
				} else {
					permScope[rp.PermissionCode] = entry
				}
			}
		}
	} else if err != nil {
		log.Printf("[Permission] query role_ids error: %v", err)
	}

	// P2: User → Direct Permission (rel_user_permission) — highest priority
	upRows, err := db.Client.RelUserPermission.Query().
		Where(reluserpermission.UserIDEQ(loginID)).
		All(ctx)
	if err == nil {
		for _, up := range upRows {
			entry := &scopeMergeEntry{
				Priority:       PermissionPathDIRECT,
				GroupScope:     up.Scope,
				OrgScope:       up.Scope,
				CustomGroupIDs: parseJSONList(up.CustomScopeGroupIds),
				CustomOrgIDs:   parseJSONList(up.CustomScopeOrgIds),
			}

			if existing, exists := permScope[up.PermissionCode]; exists {
				mergeEntry(existing, entry)
			} else {
				permScope[up.PermissionCode] = entry
			}
		}
	} else {
		log.Printf("[Permission] query user permissions error: %v", err)
	}

	result := make(map[string]PermissionScopeItem, len(permScope))
	for k, v := range permScope {
		result[k] = PermissionScopeItem{
			GroupScope:     v.GroupScope,
			OrgScope:       v.OrgScope,
			CustomGroupIDs: v.CustomGroupIDs,
			CustomOrgIDs:   v.CustomOrgIDs,
		}
	}
	return result
}

func mergeEntry(existing, new *scopeMergeEntry) {
	if new.Priority < existing.Priority {
		existing.Priority = new.Priority
		existing.GroupScope = new.GroupScope
		existing.OrgScope = new.OrgScope
		existing.CustomGroupIDs = new.CustomGroupIDs
		existing.CustomOrgIDs = new.CustomOrgIDs
	} else if new.Priority == existing.Priority {
		if isGroupScope(new.GroupScope) || new.GroupScope == string(enums.DataScopeAll) || new.GroupScope == string(enums.DataScopeSelf) {
			mergeDimension(&existing.GroupScope, &existing.CustomGroupIDs, new.GroupScope, new.CustomGroupIDs)
		}
		if isOrgScope(new.OrgScope) || new.OrgScope == string(enums.DataScopeAll) || new.OrgScope == string(enums.DataScopeSelf) {
			mergeDimension(&existing.OrgScope, &existing.CustomOrgIDs, new.OrgScope, new.CustomOrgIDs)
		}
	}
}

// scopeMergeEntry is an internal struct used during scope merging.
type scopeMergeEntry struct {
	GroupScope     string
	OrgScope       string
	CustomGroupIDs []string
	CustomOrgIDs   []string
	Priority       string
}

func isGroupScope(scope string) bool {
	return scope == string(enums.DataScopeGroup) ||
		scope == string(enums.DataScopeGroupAndBelow) ||
		scope == string(enums.DataScopeCustomGroup)
}

func isOrgScope(scope string) bool {
	return scope == string(enums.DataScopeOrg) ||
		scope == string(enums.DataScopeOrgAndBelow) ||
		scope == string(enums.DataScopeCustomOrg)
}

// mergeDimension merges scope dimension using most-restrictive rules.
// SELF always wins. Otherwise, the most restrictive scope is used.
// For CUSTOM_GROUP/CUSTOM_ORG, IDs are merged.
func mergeDimension(curScope *string, curIDs *[]string, newScope string, newIDs []string) {
	if *curScope == "" {
		*curScope = newScope
		*curIDs = newIDs
		return
	}

	if *curScope == string(enums.DataScopeSelf) {
		return
	}
	if newScope == string(enums.DataScopeSelf) {
		*curScope = string(enums.DataScopeSelf)
		*curIDs = nil
		return
	}

	restricted := mostRestrictiveScope(*curScope, newScope)
	*curScope = restricted

	if restricted == string(enums.DataScopeCustomGroup) || restricted == string(enums.DataScopeCustomOrg) {
		merged := make(map[string]struct{})
		for _, id := range *curIDs {
			merged[id] = struct{}{}
		}
		for _, id := range newIDs {
			merged[id] = struct{}{}
		}
		var ids []string
		for id := range merged {
			ids = append(ids, id)
		}
		*curIDs = ids
	}
}

// scopePriority defines the restrictive ordering for DataScope (lower = more restrictive).
var scopePriority = map[string]int{
	"SELF":            0,
	"CUSTOM_GROUP":    1,
	"CUSTOM_ORG":      2,
	"GROUP_AND_BELOW": 3,
	"GROUP":           4,
	"ORG_AND_BELOW":   5,
	"ORG":             6,
	"ALL":             7,
}

func mostRestrictiveScope(a, b string) string {
	pa := scopePriority[a]
	pb := scopePriority[b]
	if pa < pb {
		return a
	}
	return b
}

// getAllPermissionsFromCache reads all permission codes from Redis cache.
func (p *PermissionInterfaceImpl) getAllPermissionsFromCache() []string {
	if db.Redis == nil {
		return nil
	}
	ctx := context.Background()
	data, err := db.Redis.Get(ctx, constants.PermissionCacheKey).Result()
	if err != nil {
		log.Printf("[Permission] read cache error: %v", err)
		return nil
	}

	// Parse structured format: {module: {code: {code, module, category, name}, ...}, ...}
	var tree map[string]map[string]PermissionCacheItem
	if err := json.Unmarshal([]byte(data), &tree); err != nil {
		// Fallback to legacy format: {module: [values...]}
		var legacy map[string][]string
		if err2 := json.Unmarshal([]byte(data), &legacy); err2 != nil {
			return nil
		}
		var codes []string
		for _, items := range legacy {
			for _, item := range items {
				// Legacy format: "method:path:code" — extract code after last ':'
				if idx := strings.LastIndex(item, ":"); idx >= 0 && idx < len(item)-1 {
					code := item[idx+1:]
					codes = append(codes, code)
				}
			}
		}
		return codes
	}

	var codes []string
	for _, perms := range tree {
		for code := range perms {
			codes = append(codes, code)
		}
	}
	return codes
}

func parseJSONList(raw string) []string {
	raw = strings.TrimSpace(raw)
	if raw == "" || raw == "null" || raw == "[]" {
		return nil
	}
	var list []string
	if err := json.Unmarshal([]byte(raw), &list); err != nil {
		return nil
	}
	return list
}

func (p *PermissionInterfaceImpl) getDirectPermissions(loginID string) []string {
	ctx := context.Background()
	perms, err := db.Client.RelUserPermission.Query().
		Where(reluserpermission.UserIDEQ(loginID)).
		Select(reluserpermission.FieldPermissionCode).
		All(ctx)
	if err != nil {
		return nil
	}

	codes := make([]string, len(perms))
	for i, p := range perms {
		codes[i] = p.PermissionCode
	}
	return codes
}

func (p *PermissionInterfaceImpl) getRolePermissions(loginID string) []string {
	ctx := context.Background()

	// Get role IDs for the user
	roleIDs, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserIDEQ(loginID)).
		Select(reluserrole.FieldRoleID).
		All(ctx)
	if err != nil || len(roleIDs) == 0 {
		return nil
	}

	rids := make([]string, len(roleIDs))
	for i, r := range roleIDs {
		rids[i] = r.RoleID
	}

	// Get distinct permission codes via RelRolePermission
	perms, err := db.Client.RelRolePermission.Query().
		Where(relrolepermission.RoleIDIn(rids...)).
		Select(relrolepermission.FieldPermissionCode).
		All(ctx)
	if err != nil {
		return nil
	}

	codes := make([]string, len(perms))
	for i, p := range perms {
		codes[i] = p.PermissionCode
	}
	return codes
}

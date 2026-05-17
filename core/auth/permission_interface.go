package auth

import (
	"context"
	"encoding/json"
	"log"

	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/core/enums"
	"hei-gin/ent/gen/relrolepermission"
	"hei-gin/ent/gen/reluserpermission"
	"hei-gin/ent/gen/reluserrole"
	"hei-gin/ent/gen/sysrole"
)

// HeiPermissionInterface is the runtime permission loader interface.
// Implementations query the database at request time.
type HeiPermissionInterface interface {
	GetPermissionList(loginID string, loginType string) ([]string, error)
	GetRoleList(loginID string, loginType string) ([]string, error)
	GetPermissionScopeMap(loginID string, loginType string) (map[string]ScopeInfo, error)
}

// ScopeInfo represents the data scope information for a permission.
type ScopeInfo struct {
	GroupScope     string   `json:"group_scope"`
	OrgScope       string   `json:"org_scope"`
	CustomGroupIDs []string `json:"custom_group_ids"`
	CustomOrgIDs   []string `json:"custom_org_ids"`
}

// ScopeRow represents a raw scope row from the database.
type ScopeRow struct {
	PermissionCode string
	Scope          string
	CustomGroupIDs string
	CustomOrgIDs   string
}

// HeiPermissionInterfaceImpl is the default implementation of HeiPermissionInterface.
type HeiPermissionInterfaceImpl struct{}

// getRoleIDs loads the role IDs for a given login ID from the database.
func (p *HeiPermissionInterfaceImpl) getRoleIDs(ctx context.Context, loginID string) ([]string, error) {
	entities, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserID(loginID)).
		All(ctx)
	if err != nil {
		log.Printf("[Permission] Failed to query role IDs: %v", err)
		return nil, err
	}

	roleIDs := make([]string, 0, len(entities))
	for _, e := range entities {
		roleIDs = append(roleIDs, e.RoleID)
	}
	return roleIDs, nil
}

// isGroupScope checks if the given scope is a group-related scope type.
func (p *HeiPermissionInterfaceImpl) isGroupScope(scope string) bool {
	return scope == string(enums.DataScopeGroup) ||
		scope == string(enums.DataScopeGroupAndBelow) ||
		scope == string(enums.DataScopeCustomGroup)
}

// isOrgScope checks if the given scope is an org-related scope type.
func (p *HeiPermissionInterfaceImpl) isOrgScope(scope string) bool {
	return scope == string(enums.DataScopeOrg) ||
		scope == string(enums.DataScopeOrgAndBelow) ||
		scope == string(enums.DataScopeCustomOrg)
}

// mergeDimension merges a single dimension (group or org) into the current scope map.
// SELF has the highest priority and cannot be overridden.
func (p *HeiPermissionInterfaceImpl) mergeDimension(cur map[string]interface{}, scopeKey, idsKey, newScope string, newIDs []string) {
	if curScope, ok := cur[scopeKey].(string); ok && curScope != "" {
		if curScope == string(enums.DataScopeSelf) {
			return
		}
		if newScope == string(enums.DataScopeSelf) {
			cur[scopeKey] = string(enums.DataScopeSelf)
			cur[idsKey] = []string{}
			return
		}
		restricted := enums.MostRestrictive(curScope, newScope)
		cur[scopeKey] = restricted
		if restricted == string(enums.DataScopeCustomGroup) || restricted == string(enums.DataScopeCustomOrg) {
			merged := make(map[string]struct{})
			if existingIDs, ok := cur[idsKey].([]string); ok {
				for _, id := range existingIDs {
					merged[id] = struct{}{}
				}
			}
			for _, id := range newIDs {
				merged[id] = struct{}{}
			}
			result := make([]string, 0, len(merged))
			for id := range merged {
				result = append(result, id)
			}
			cur[idsKey] = result
		}
	} else {
		cur[scopeKey] = newScope
		if newIDs == nil {
			cur[idsKey] = []string{}
		} else {
			cur[idsKey] = newIDs
		}
	}
}

// mergeScope merges scope rows into the result map using two-path logic.
func (p *HeiPermissionInterfaceImpl) mergeScope(result map[string]map[string]interface{}, priority string, rows []ScopeRow) {
	for _, row := range rows {
		scope := row.Scope
		if scope == "" {
			scope = string(enums.DataScopeAll)
		}

		var cgids []string
		if row.CustomGroupIDs != "" {
			if err := json.Unmarshal([]byte(row.CustomGroupIDs), &cgids); err != nil {
				cgids = []string{}
			}
		}

		var cogids []string
		if row.CustomOrgIDs != "" {
			if err := json.Unmarshal([]byte(row.CustomOrgIDs), &cogids); err != nil {
				cogids = []string{}
			}
		}

		isGroup := p.isGroupScope(scope)
		isOrg := p.isOrgScope(scope)
		isAll := scope == string(enums.DataScopeAll)
		isSelf := scope == string(enums.DataScopeSelf)

		code := row.PermissionCode
		if existing, ok := result[code]; ok {
			existingPriority := existing["priority"].(string)
			if priority < existingPriority {
				// Higher priority (lower P value): overwrite
				var gScope interface{} = nil
				if isAll || isSelf || isGroup {
					gScope = scope
				}
				var oScope interface{} = nil
				if isAll || isSelf || isOrg {
					oScope = scope
				}
				existing["group_scope"] = gScope
				existing["org_scope"] = oScope
				existing["custom_group_ids"] = cgids
				existing["custom_org_ids"] = cogids
				existing["priority"] = priority
			} else if priority == existingPriority {
				// Same priority: merge dimensions
				if isGroup || isAll || isSelf {
					p.mergeDimension(existing, "group_scope", "custom_group_ids", scope, cgids)
				}
				if isOrg || isAll || isSelf {
					p.mergeDimension(existing, "org_scope", "custom_org_ids", scope, cogids)
				}
			}
		} else {
			var gScope interface{} = nil
			if isAll || isSelf || isGroup {
				gScope = scope
			}
			var oScope interface{} = nil
			if isAll || isSelf || isOrg {
				oScope = scope
			}
			result[code] = map[string]interface{}{
				"group_scope":      gScope,
				"org_scope":        oScope,
				"custom_group_ids": cgids,
				"custom_org_ids":   cogids,
				"priority":         priority,
			}
		}
	}
}

// getAllPermissionsFromRedis reads all permission codes from the Redis cache.
func (p *HeiPermissionInterfaceImpl) getAllPermissionsFromRedis(ctx context.Context) ([]string, error) {
	if db.Redis == nil {
		return []string{}, nil
	}

	data, err := db.Redis.Get(ctx, constants.PERMISSION_CACHE_KEY).Result()
	if err != nil {
		log.Printf("[Permission] Failed to read permission cache from Redis: %v", err)
		return []string{}, nil
	}
	if data == "" {
		return []string{}, nil
	}

	var tree map[string]map[string]interface{}
	if err := json.Unmarshal([]byte(data), &tree); err != nil {
		log.Printf("[Permission] Failed to unmarshal permission cache: %v", err)
		return []string{}, nil
	}

	var codes []string
	for _, modulePerms := range tree {
		for code := range modulePerms {
			codes = append(codes, code)
		}
	}
	return codes, nil
}

// GetPermissionList returns the user's permission codes, or ALL codes for SUPER_ADMIN.
func (p *HeiPermissionInterfaceImpl) GetPermissionList(loginID, loginType string) ([]string, error) {
	ctx := context.Background()

	roleList, err := p.GetRoleList(loginID, loginType)
	if err != nil {
		return nil, err
	}

	// SUPER_ADMIN returns all permissions in the system (from route annotations)
	for _, role := range roleList {
		if role == constants.SUPER_ADMIN_CODE {
			return p.getAllPermissionsFromRedis(ctx)
		}
	}

	permissionCodes := make(map[string]struct{})

	if loginType == string(enums.LoginTypeBusiness) || loginType == string(enums.LoginTypeConsumer) {
		// Path 1: User -> Role -> Permission
		roleIDs, err := p.getRoleIDs(ctx, loginID)
		if err != nil {
			return nil, err
		}
		if len(roleIDs) > 0 {
			entities, err := db.Client.RelRolePermission.Query().
				Where(relrolepermission.RoleIDIn(roleIDs...)).
				All(ctx)
			if err != nil {
				log.Printf("[Permission] Failed to query role permissions: %v", err)
			} else {
				for _, e := range entities {
					permissionCodes[e.PermissionCode] = struct{}{}
				}
			}
		}

		// Path 2: User -> Direct Permission
		entities, err := db.Client.RelUserPermission.Query().
			Where(reluserpermission.UserID(loginID)).
			All(ctx)
		if err != nil {
			log.Printf("[Permission] Failed to query user permissions: %v", err)
		} else {
			for _, e := range entities {
				permissionCodes[e.PermissionCode] = struct{}{}
			}
		}
	}

	result := make([]string, 0, len(permissionCodes))
	for code := range permissionCodes {
		result = append(result, code)
	}
	return result, nil
}

// GetRoleList returns the user's role codes by querying SysRole through RelUserRole.
func (p *HeiPermissionInterfaceImpl) GetRoleList(loginID, loginType string) ([]string, error) {
	ctx := context.Background()

	roleIDs, err := p.getRoleIDs(ctx, loginID)
	if err != nil {
		return nil, err
	}

	if len(roleIDs) == 0 {
		return []string{}, nil
	}

	roles, err := db.Client.SysRole.Query().
		Where(sysrole.IDIn(roleIDs...)).
		All(ctx)
	if err != nil {
		log.Printf("[Permission] Failed to query role list: %v", err)
		return nil, err
	}

	roleCodes := make([]string, 0, len(roles))
	for _, r := range roles {
		roleCodes = append(roleCodes, r.Code)
	}
	return roleCodes, nil
}

// GetPermissionScopeMap returns the permission scope map with two-path merge.
func (p *HeiPermissionInterfaceImpl) GetPermissionScopeMap(loginID, loginType string) (map[string]ScopeInfo, error) {
	ctx := context.Background()

	if loginType != string(enums.LoginTypeBusiness) && loginType != string(enums.LoginTypeConsumer) {
		return map[string]ScopeInfo{}, nil
	}

	roleList, err := p.GetRoleList(loginID, loginType)
	if err != nil {
		return nil, err
	}

	// SUPER_ADMIN gets ALL scope on every permission
	for _, role := range roleList {
		if role == constants.SUPER_ADMIN_CODE {
			allCodes, err := p.getAllPermissionsFromRedis(ctx)
			if err != nil {
				return nil, err
			}
			result := make(map[string]ScopeInfo, len(allCodes))
			for _, code := range allCodes {
				result[code] = ScopeInfo{
					GroupScope:     string(enums.DataScopeAll),
					OrgScope:       string(enums.DataScopeAll),
					CustomGroupIDs: []string{},
					CustomOrgIDs:   []string{},
				}
			}
			return result, nil
		}
	}

	permScope := make(map[string]map[string]interface{})

	// Path 1 (P1): User -> Role -> Permission
	roleIDs, err := p.getRoleIDs(ctx, loginID)
	if err != nil {
		log.Printf("[Permission] Failed to query user roles: %v", err)
	} else if len(roleIDs) > 0 {
		entities, err := db.Client.RelRolePermission.Query().
			Where(relrolepermission.RoleIDIn(roleIDs...)).
			All(ctx)
		if err != nil {
			log.Printf("[Permission] Failed to query role permission scopes: %v", err)
		} else {
			var scopeRows []ScopeRow
			for _, e := range entities {
				scopeRows = append(scopeRows, ScopeRow{
					PermissionCode: e.PermissionCode,
					Scope:          e.Scope,
					CustomGroupIDs: safeStrPtr(e.CustomScopeGroupIds),
					CustomOrgIDs:   safeStrPtr(e.CustomScopeOrgIds),
				})
			}
			p.mergeScope(permScope, string(enums.PermissionPathUserRole), scopeRows)
		}
	}

	// Path 2 (P0): User -> Direct Permission
	entities, err := db.Client.RelUserPermission.Query().
		Where(reluserpermission.UserID(loginID)).
		All(ctx)
	if err != nil {
		log.Printf("[Permission] Failed to query user permission scopes: %v", err)
	} else {
		var scopeRows []ScopeRow
		for _, e := range entities {
			scopeRows = append(scopeRows, ScopeRow{
				PermissionCode: e.PermissionCode,
				Scope:          e.Scope,
				CustomGroupIDs: safeStrPtr(e.CustomScopeGroupIds),
				CustomOrgIDs:   safeStrPtr(e.CustomScopeOrgIds),
			})
		}
		p.mergeScope(permScope, string(enums.PermissionPathDirect), scopeRows)
	}

	result := make(map[string]ScopeInfo, len(permScope))
	for k, v := range permScope {
		result[k] = ScopeInfo{
			GroupScope:     safeString(v["group_scope"]),
			OrgScope:       safeString(v["org_scope"]),
			CustomGroupIDs: safeStringSlice(v["custom_group_ids"]),
			CustomOrgIDs:   safeStringSlice(v["custom_org_ids"]),
		}
	}
	return result, nil
}

// safeString converts an interface{} to a string, returning "" for nil.
func safeString(v interface{}) string {
	if v == nil {
		return ""
	}
	if s, ok := v.(string); ok {
		return s
	}
	return ""
}

// safeStringSlice converts an interface{} to a []string, returning empty slice for nil.
func safeStringSlice(v interface{}) []string {
	if v == nil {
		return []string{}
	}
	if s, ok := v.([]string); ok {
		return s
	}
	return []string{}
}

// safeStrPtr converts a *string to string, returning "" for nil.
func safeStrPtr(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

package auth

import (
	"context"
	"encoding/json"
	"log"

	"gorm.io/gorm"

	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/core/enums"
	userModel "hei-gin/modules/sys/user"
	roleModel "hei-gin/modules/sys/role"
)

type HeiPermissionInterface interface {
	GetPermissionList(loginID string, loginType string) ([]string, error)
	GetRoleList(loginID string, loginType string) ([]string, error)
	GetPermissionScopeMap(loginID string, loginType string) (map[string]ScopeInfo, error)
}

type ScopeInfo struct {
	GroupScope     string   `json:"group_scope"`
	OrgScope       string   `json:"org_scope"`
	CustomGroupIDs []string `json:"custom_group_ids"`
	CustomOrgIDs   []string `json:"custom_org_ids"`
}

type ScopeRow struct {
	PermissionCode string
	Scope          string
	CustomGroupIDs string
	CustomOrgIDs   string
}

type HeiPermissionInterfaceImpl struct{}

func (p *HeiPermissionInterfaceImpl) getRoleIDs(ctx context.Context, loginID string) ([]string, error) {
	var entities []userModel.RelUserRole
	if err := db.DB.WithContext(ctx).Where("user_id = ?", loginID).Find(&entities).Error; err != nil {
		log.Printf("[Permission] Failed to query role IDs: %v", err)
		return nil, err
	}
	roleIDs := make([]string, 0, len(entities))
	for _, e := range entities {
		roleIDs = append(roleIDs, e.RoleID)
	}
	return roleIDs, nil
}

func (p *HeiPermissionInterfaceImpl) isGroupScope(scope string) bool {
	return scope == string(enums.DataScopeGroup) ||
		scope == string(enums.DataScopeGroupAndBelow) ||
		scope == string(enums.DataScopeCustomGroup)
}

func (p *HeiPermissionInterfaceImpl) isOrgScope(scope string) bool {
	return scope == string(enums.DataScopeOrg) ||
		scope == string(enums.DataScopeOrgAndBelow) ||
		scope == string(enums.DataScopeCustomOrg)
}

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
				if isGroup {
					p.mergeDimension(existing, "group_scope", "custom_group_ids", scope, cgids)
				}
				if isOrg {
					p.mergeDimension(existing, "org_scope", "custom_org_ids", scope, cogids)
				}
				existing["priority"] = priority
			}
		} else {
			entry := map[string]interface{}{
				"priority": priority,
			}
			if isAll || isSelf || isGroup {
				entry["group_scope"] = scope
				if isGroup {
					p.mergeDimension(entry, "group_scope", "custom_group_ids", scope, cgids)
				}
			}
			if isAll || isSelf || isOrg {
				entry["org_scope"] = scope
				if isOrg {
					p.mergeDimension(entry, "org_scope", "custom_org_ids", scope, cogids)
				}
			}
			result[code] = entry
		}
	}
}

// getAllPermissionsFromRedis retrieves all permission codes from the Redis cache.
func (p *HeiPermissionInterfaceImpl) getAllPermissionsFromRedis(ctx context.Context) ([]string, error) {
	data, err := db.Redis.Get(ctx, constants.PERMISSION_CACHE_KEY).Result()
	if err != nil {
		return nil, err
	}
	var tree map[string]interface{}
	if err := json.Unmarshal([]byte(data), &tree); err != nil {
		return nil, err
	}
	var codes []string
	for _, modulePerms := range tree {
		if perms, ok := modulePerms.(map[string]interface{}); ok {
			for code := range perms {
				codes = append(codes, code)
			}
		}
	}
	return codes, nil
}

// GetPermissionList returns the user's permission codes by querying both role-based and direct permissions.
func (p *HeiPermissionInterfaceImpl) GetPermissionList(loginID, loginType string) ([]string, error) {
	ctx := context.Background()

	roleList, err := p.GetRoleList(loginID, loginType)
	if err != nil {
		return nil, err
	}
	for _, role := range roleList {
		if role == constants.SUPER_ADMIN_CODE {
			return p.getAllPermissionsFromRedis(ctx)
		}
	}

	permissionCodes := make(map[string]struct{})

	if loginType == string(enums.LoginTypeBusiness) || loginType == string(enums.LoginTypeConsumer) {
		roleIDs, err := p.getRoleIDs(ctx, loginID)
		if err != nil {
			return nil, err
		}
		if len(roleIDs) > 0 {
			var entities []userModel.RelRolePermission
			if err := db.DB.WithContext(ctx).Where("role_id IN ?", roleIDs).Find(&entities).Error; err != nil {
				log.Printf("[Permission] Failed to query role permissions: %v", err)
			} else {
				for _, e := range entities {
					permissionCodes[e.PermissionCode] = struct{}{}
				}
			}
		}

		var entities []userModel.RelUserPermission
		if err := db.DB.WithContext(ctx).Where("user_id = ?", loginID).Find(&entities).Error; err != nil {
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

	var roles []roleModel.SysRole
	if err := db.DB.WithContext(ctx).Where("id IN ?", roleIDs).Find(&roles).Error; err != nil {
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

	roleIDs, err := p.getRoleIDs(ctx, loginID)
	if err != nil {
		log.Printf("[Permission] Failed to query user roles: %v", err)
	} else if len(roleIDs) > 0 {
		var entities []userModel.RelRolePermission
		if err := db.DB.WithContext(ctx).Where("role_id IN ?", roleIDs).Find(&entities).Error; err != nil {
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

	var entities []userModel.RelUserPermission
	if err := db.DB.WithContext(ctx).Where("user_id = ?", loginID).Find(&entities).Error; err != nil {
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

func safeString(v interface{}) string {
	if v == nil {
		return ""
	}
	if s, ok := v.(string); ok {
		return s
	}
	return ""
}

func safeStringSlice(v interface{}) []string {
	if v == nil {
		return []string{}
	}
	if s, ok := v.([]string); ok {
		return s
	}
	return []string{}
}

func safeStrPtr(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

// Ensure gorm is imported
var _ = &gorm.DB{}

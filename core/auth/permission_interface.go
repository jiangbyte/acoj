package auth

import (
	"context"
	"log"

	"hei-gin/core/db"
)

// PermissionInterfaceImpl provides runtime permission loading from the database.
// It resolves 2 permission paths:
//   - P0 (DIRECT): User → RelUserPermission (highest priority)
//   - P1 (USER_ROLE): User → Role → RelRolePermission
type PermissionInterfaceImpl struct{}

var PermissionInterface = &PermissionInterfaceImpl{}

func (p *PermissionInterfaceImpl) GetPermissionList(loginID, loginType string) []string {
	if db.RawDB == nil {
		return nil
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
	if db.RawDB == nil {
		return nil
	}

	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx,
		"SELECT r.code FROM rel_user_role ur JOIN sys_role r ON ur.role_id = r.id WHERE ur.user_id = ?",
		loginID,
	)
	if err != nil {
		log.Printf("[Permission] query roles error: %v", err)
		return nil
	}
	defer rows.Close()

	var roles []string
	for rows.Next() {
		var code string
		if err := rows.Scan(&code); err == nil {
			roles = append(roles, code)
		}
	}
	return roles
}

func (p *PermissionInterfaceImpl) getDirectPermissions(loginID string) []string {
	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx,
		"SELECT permission_code FROM rel_user_permission WHERE user_id = ?",
		loginID,
	)
	if err != nil {
		return nil
	}
	defer rows.Close()

	var perms []string
	for rows.Next() {
		var code string
		if err := rows.Scan(&code); err == nil {
			perms = append(perms, code)
		}
	}
	return perms
}

func (p *PermissionInterfaceImpl) getRolePermissions(loginID string) []string {
	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT DISTINCT rp.permission_code
		 FROM rel_user_role ur
		 JOIN rel_role_permission rp ON ur.role_id = rp.role_id
		 WHERE ur.user_id = ?`,
		loginID,
	)
	if err != nil {
		return nil
	}
	defer rows.Close()

	var perms []string
	for rows.Next() {
		var code string
		if err := rows.Scan(&code); err == nil {
			perms = append(perms, code)
		}
	}
	return perms
}

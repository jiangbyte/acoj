package auth

import (
	"context"

	"hei-goframe/internal/consts"
)

// PermissionTool 权限工具，包装 PermissionInterface 提供便捷方法
type PermissionTool struct{}

var PermTool = &PermissionTool{}

func (t *PermissionTool) getInterface() *PermissionInterface {
	return GetPermissionInterface()
}

func (t *PermissionTool) getLoginId(ctx context.Context, tokenStr string, loginType string) string {
	var authTool *AuthTool
	if loginType == consts.LoginTypeConsumer {
		authTool = ConsumerAuth
	} else {
		authTool = BusinessAuth
	}
	id, _ := authTool.GetLoginId(ctx, tokenStr)
	return id
}

func (t *PermissionTool) GetPermissionList(ctx context.Context, tokenStr, loginType string) ([]string, error) {
	pi := t.getInterface()
	if pi == nil {
		return nil, nil
	}
	loginId := t.getLoginId(ctx, tokenStr, loginType)
	if loginId == "" {
		return nil, nil
	}
	return pi.GetPermissionList(ctx, loginId, loginType)
}

func (t *PermissionTool) GetRoleList(ctx context.Context, tokenStr, loginType string) ([]string, error) {
	pi := t.getInterface()
	if pi == nil {
		return nil, nil
	}
	loginId := t.getLoginId(ctx, tokenStr, loginType)
	if loginId == "" {
		return nil, nil
	}
	return pi.GetRoleList(ctx, loginId, loginType)
}

func (t *PermissionTool) HasPermission(ctx context.Context, permission, tokenStr, loginType string) (bool, error) {
	perms, err := t.GetPermissionList(ctx, tokenStr, loginType)
	if err != nil || perms == nil {
		return false, err
	}
	matcher := &PermissionMatcher{}
	return matcher.HasPermission(permission, perms), nil
}

func (t *PermissionTool) HasPermissionAnd(ctx context.Context, required []string, tokenStr, loginType string) (bool, error) {
	perms, err := t.GetPermissionList(ctx, tokenStr, loginType)
	if err != nil || perms == nil {
		return false, err
	}
	matcher := &PermissionMatcher{}
	return matcher.HasPermissionAnd(required, perms), nil
}

func (t *PermissionTool) HasPermissionOr(ctx context.Context, required []string, tokenStr, loginType string) (bool, error) {
	perms, err := t.GetPermissionList(ctx, tokenStr, loginType)
	if err != nil || perms == nil {
		return false, err
	}
	matcher := &PermissionMatcher{}
	return matcher.HasPermissionOr(required, perms), nil
}

func (t *PermissionTool) HasRole(ctx context.Context, role, tokenStr, loginType string) (bool, error) {
	roles, err := t.GetRoleList(ctx, tokenStr, loginType)
	if err != nil || roles == nil {
		return false, err
	}
	for _, r := range roles {
		if r == role {
			return true, nil
		}
	}
	return false, nil
}

func (t *PermissionTool) GetPermissionListByLoginId(ctx context.Context, loginId, loginType string) ([]string, error) {
	pi := t.getInterface()
	if pi == nil {
		return nil, nil
	}
	return pi.GetPermissionList(ctx, loginId, loginType)
}

func (t *PermissionTool) GetRoleListByLoginId(ctx context.Context, loginId, loginType string) ([]string, error) {
	pi := t.getInterface()
	if pi == nil {
		return nil, nil
	}
	return pi.GetRoleList(ctx, loginId, loginType)
}

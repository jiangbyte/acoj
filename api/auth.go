package api

import "context"

// PermissionAPI defines the interface for permission queries.
// Context is passed through for tracing and timeout propagation.
type PermissionAPI interface {
	GetPermissionList(ctx context.Context, loginID string, loginType string) ([]string, error)
	GetRoleList(ctx context.Context, loginID string, loginType string) ([]string, error)
	GetPermissionScopeMap(ctx context.Context, loginID string, loginType string) (map[string]ScopeInfo, error)
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
	CustomGroupIDs *string
	CustomOrgIDs   *string
}

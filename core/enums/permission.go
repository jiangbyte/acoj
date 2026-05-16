package enums

// PermissionCategory represents the category of a permission.
type PermissionCategory string

const (
	PermissionCategoryBackend  PermissionCategory = "BACKEND"
	PermissionCategoryFrontend PermissionCategory = "FRONTEND"
)

func (e PermissionCategory) Desc() string {
	switch e {
	case PermissionCategoryBackend:
		return "后端权限"
	case PermissionCategoryFrontend:
		return "前端权限"
	default:
		return ""
	}
}

// PermissionScope represents the scope of a permission.
type PermissionScope string

const (
	PermissionScopeAll         PermissionScope = "ALL"
	PermissionScopeOrg         PermissionScope = "ORG"
	PermissionScopeOrgAndBelow PermissionScope = "ORG_AND_BELOW"
	PermissionScopeSelf        PermissionScope = "SELF"
)

func (e PermissionScope) Desc() string {
	switch e {
	case PermissionScopeAll:
		return "全部"
	case PermissionScopeOrg:
		return "本组织"
	case PermissionScopeOrgAndBelow:
		return "本组织及以下"
	case PermissionScopeSelf:
		return "本人"
	default:
		return ""
	}
}

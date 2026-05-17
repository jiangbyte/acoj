package role

// RoleVO is the view object for a role, used for create/modify requests and API responses.
type RoleVO struct {
	ID          string  `json:"id,omitempty"`
	Code        string  `json:"code"`
	Name        string  `json:"name"`
	Category    string  `json:"category"`
	Description *string `json:"description,omitempty"`
	Status      string  `json:"status,omitempty"`
	SortCode    int     `json:"sort_code"`
	Extra       *string `json:"extra,omitempty"`
	CreatedAt   string  `json:"created_at,omitempty"`
	CreatedBy   *string `json:"created_by,omitempty"`
	UpdatedAt   string  `json:"updated_at,omitempty"`
	UpdatedBy   *string `json:"updated_by,omitempty"`
}

// RolePageParam holds pagination parameters for the role page query.
type RolePageParam struct {
	Current int `json:"current" form:"current"`
	Size    int `json:"size" form:"size"`
}

// PermissionItem represents a permission to be granted to a role.
type PermissionItem struct {
	PermissionCode      string  `json:"permission_code"`
	Scope               string  `json:"scope"`
	CustomScopeGroupIds *string `json:"custom_scope_group_ids,omitempty"`
	CustomScopeOrgIds   *string `json:"custom_scope_org_ids,omitempty"`
}

// GrantPermissionParam holds the parameters for granting permissions to a role.
type GrantPermissionParam struct {
	RoleID      string           `json:"role_id"`
	Permissions []PermissionItem `json:"permissions"`
}

// ButtonPermissionScope represents a button permission with data scope.
type ButtonPermissionScope struct {
	PermissionCode      string  `json:"permission_code"`
	Scope               string  `json:"scope"`
	CustomScopeGroupIds *string `json:"custom_scope_group_ids,omitempty"`
	CustomScopeOrgIds   *string `json:"custom_scope_org_ids,omitempty"`
}

// GrantResourceParam holds the parameters for granting resources to a role.
type GrantResourceParam struct {
	RoleID      string                  `json:"role_id"`
	ResourceIDs []string                `json:"resource_ids"`
	Permissions []ButtonPermissionScope `json:"permissions"`
}

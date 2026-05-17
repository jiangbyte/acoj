package user

import "hei-gin/modules/sys/role"

type UserVO struct {
	ID           string   `json:"id,omitempty"`
	Username     *string  `json:"username,omitempty"`
	Nickname     *string  `json:"nickname,omitempty"`
	Avatar       *string  `json:"avatar,omitempty"`
	Motto        *string  `json:"motto,omitempty"`
	Gender       *string  `json:"gender,omitempty"`
	Birthday     string   `json:"birthday,omitempty"`
	Email        *string  `json:"email,omitempty"`
	Github       *string  `json:"github,omitempty"`
	Phone        *string  `json:"phone,omitempty"`
	OrgID        *string  `json:"org_id,omitempty"`
	PositionID   *string  `json:"position_id,omitempty"`
	GroupID      *string  `json:"group_id,omitempty"`
	OrgNames     []string `json:"org_names,omitempty"`
	GroupNames   []string `json:"group_names,omitempty"`
	PositionName *string  `json:"position_name,omitempty"`
	Status       string   `json:"status,omitempty"`
	LastLoginAt  string   `json:"last_login_at,omitempty"`
	LastLoginIP  *string  `json:"last_login_ip,omitempty"`
	LoginCount   int      `json:"login_count,omitempty"`
	CreatedAt    string   `json:"created_at,omitempty"`
	CreatedBy    *string  `json:"created_by,omitempty"`
	UpdatedAt    string   `json:"updated_at,omitempty"`
	UpdatedBy    *string  `json:"updated_by,omitempty"`
	RoleIDs      []string `json:"role_ids,omitempty"`
}

type UserPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword,omitempty" form:"keyword"`
	Status  string `json:"status,omitempty" form:"status"`
}

type GrantRoleParam struct {
	UserID  string   `json:"user_id"`
	RoleIDs []string `json:"role_ids"`
}

type GrantUserPermissionParam struct {
	UserID      string                `json:"user_id"`
	Permissions []role.PermissionItem `json:"permissions,omitempty"`
}

type UpdateProfileParam struct {
	Username *string `json:"username,omitempty"`
	Nickname *string `json:"nickname,omitempty"`
	Motto    *string `json:"motto,omitempty"`
	Gender   *string `json:"gender,omitempty"`
	Birthday string  `json:"birthday,omitempty"`
	Email    *string `json:"email,omitempty"`
	Github   *string `json:"github,omitempty"`
	Phone    *string `json:"phone,omitempty"`
}

type UpdateAvatarParam struct {
	Avatar string `json:"avatar"`
}

type UpdatePasswordParam struct {
	CurrentPassword string `json:"current_password"`
	NewPassword     string `json:"new_password"`
}

type PermissionDetail struct {
	PermissionCode      string  `json:"permission_code"`
	Scope               string  `json:"scope"`
	CustomScopeGroupIds *string `json:"custom_scope_group_ids,omitempty"`
	CustomScopeOrgIds   *string `json:"custom_scope_org_ids,omitempty"`
}

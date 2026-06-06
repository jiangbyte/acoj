package user

type (
	PermissionItem struct {
		PermissionCode      string  `json:"permission_code"`
		Scope               string  `json:"scope"`
		CustomScopeGroupIds *string `json:"custom_scope_group_ids"`
		CustomScopeOrgIds   *string `json:"custom_scope_org_ids"`
	}

	UpdateStatusParam struct {
		IDs    []string `json:"ids"`
		Status string   `json:"status"`
	}

	BatchImportParam struct {
		Users []BatchImportUser `json:"users"`
	}

	BatchImportUser struct {
		Username *string `json:"username"`
		Nickname *string `json:"nickname"`
		Phone    *string `json:"phone"`
		Email    *string `json:"email"`
		Gender   *string `json:"gender"`
		Password *string `json:"password"`
	}

	SysQuickAction struct {
		ID         string `gorm:"primaryKey;size:32" json:"id"`
		UserID     string `gorm:"size:32;uniqueIndex:idx_user_resource;not null" json:"user_id"`
		ResourceID string `gorm:"size:32;uniqueIndex:idx_user_resource;not null" json:"resource_id"`
		SortCode   int    `gorm:"default:0" json:"sort_code"`
	}
)

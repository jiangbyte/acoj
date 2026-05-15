package enums

type LoginType string

const (
	LoginTypeBusiness LoginType = "BUSINESS"
	LoginTypeConsumer LoginType = "CONSUMER"
)

type Status string

const (
	StatusYes      Status = "YES"
	StatusNo       Status = "NO"
	StatusActive   Status = "ACTIVE"
	StatusEnabled  Status = "ENABLED"
	StatusDisabled Status = "DISABLED"
)

type UserStatus string

const (
	UserStatusActive   UserStatus = "ACTIVE"
	UserStatusInactive UserStatus = "INACTIVE"
	UserStatusLocked   UserStatus = "LOCKED"
)

type ResourceType string

const (
	ResourceTypeDirectory    ResourceType = "DIRECTORY"
	ResourceTypeMenu         ResourceType = "MENU"
	ResourceTypeButton       ResourceType = "BUTTON"
	ResourceTypeInternalLink ResourceType = "INTERNAL_LINK"
	ResourceTypeExternalLink ResourceType = "EXTERNAL_LINK"
)

type ResourceCategory string

const (
	ResourceCategoryBackendMenu    ResourceCategory = "BACKEND_MENU"
	ResourceCategoryFrontendMenu   ResourceCategory = "FRONTEND_MENU"
	ResourceCategoryBackendButton  ResourceCategory = "BACKEND_BUTTON"
	ResourceCategoryFrontendButton ResourceCategory = "FRONTEND_BUTTON"
)

type DataScope string

const (
	DataScopeAll           DataScope = "ALL"
	DataScopeSelf          DataScope = "SELF"
	DataScopeOrg           DataScope = "ORG"
	DataScopeOrgAndBelow   DataScope = "ORG_AND_BELOW"
	DataScopeCustomOrg     DataScope = "CUSTOM_ORG"
	DataScopeGroup         DataScope = "GROUP"
	DataScopeGroupAndBelow DataScope = "GROUP_AND_BELOW"
	DataScopeCustomGroup   DataScope = "CUSTOM_GROUP"
)

type PermissionCategory string

const (
	PermissionCategoryBackend  PermissionCategory = "BACKEND"
	PermissionCategoryFrontend PermissionCategory = "FRONTEND"
)

type CheckMode string

const (
	CheckModeAND CheckMode = "AND"
	CheckModeOR  CheckMode = "OR"
)

type ExportType string

const (
	ExportTypeCurrent  ExportType = "current"
	ExportTypeSelected ExportType = "selected"
	ExportTypeAll      ExportType = "all"
)

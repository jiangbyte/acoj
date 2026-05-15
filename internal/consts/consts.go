package consts

const (
	// Page data field keys matching Python PageDataField
	PageDataRecords = "records"
	PageDataTotal   = "total"
	PageDataPage    = "page"
	PageDataSize    = "size"
	PageDataPages   = "pages"

	// Base system field names matching Python BASE_SYSTEM_FIELDS
	BaseFieldId        = "id"
	BaseFieldCreatedAt = "created_at"
	BaseFieldCreatedBy = "created_by"
	BaseFieldUpdatedAt = "updated_at"
	BaseFieldUpdatedBy = "updated_by"

	// Login types
	LoginTypeBusiness = "BUSINESS"
	LoginTypeConsumer = "CONSUMER"

	// Status
	StatusEnabled  = "ENABLED"
	StatusDisabled = "DISABLED"
	StatusActive   = "ACTIVE"
	StatusInactive = "INACTIVE"
	StatusLocked   = "LOCKED"

	// User status
	UserStatusActive   = "ACTIVE"
	UserStatusInactive = "INACTIVE"
	UserStatusLocked   = "LOCKED"

	// Permission category
	PermissionCategoryBackend  = "BACKEND"
	PermissionCategoryFrontend = "FRONTEND"

	// Permission scope
	PermissionScopeAll           = "ALL"
	PermissionScopeOrg           = "ORG"
	PermissionScopeOrgAndBelow   = "ORG_AND_BELOW"
	PermissionScopeSelf          = "SELF"
	PermissionScopeCustomOrg     = "CUSTOM_ORG"
	PermissionScopeGroup         = "GROUP"
	PermissionScopeGroupAndBelow = "GROUP_AND_BELOW"
	PermissionScopeCustomGroup   = "CUSTOM_GROUP"

	// Check mode
	CheckModeAnd = "AND"
	CheckModeOr  = "OR"

	// Permission path (priority: lower value = higher priority)
	PermissionPathDirect   = "P0"
	PermissionPathUserRole = "P1"
	PermissionPathOrgRole  = "P2"

	// Resource category
	ResourceCategoryBackendMenu    = "BACKEND_MENU"
	ResourceCategoryFrontendMenu   = "FRONTEND_MENU"
	ResourceCategoryBackendButton  = "BACKEND_BUTTON"
	ResourceCategoryFrontendButton = "FRONTEND_BUTTON"

	// Resource type
	ResourceTypeDirectory    = "DIRECTORY"
	ResourceTypeMenu         = "MENU"
	ResourceTypeButton       = "BUTTON"
	ResourceTypeInternalLink = "INTERNAL_LINK"
	ResourceTypeExternalLink = "EXTERNAL_LINK"

	// Export type
	ExportTypeCurrent  = "current"
	ExportTypeSelected = "selected"
	ExportTypeAll      = "all"

	// Super admin
	SuperAdminCode = "SUPER_ADMIN"

	// Redis cache keys
	PermissionCacheKey = "hei:permission:keys"
	DictCacheKey       = "hei:dict:tree"
	DictTreeCacheKey   = "hei:dict:fulltree"
	NoRepeatPrefix     = "norepeat:"

	// Auth token / session keys (BUSINESS)
	TokenPrefixBusiness   = "hei:auth:BUSINESS:token:"
	SessionPrefixBusiness = "hei:auth:BUSINESS:session:"
	DisableKeyBusiness    = "hei:auth:BUSINESS:disable:"

	// Auth token / session keys (CONSUMER)
	TokenPrefixConsumer   = "hei:auth:CONSUMER:token:"
	SessionPrefixConsumer = "hei:auth:CONSUMER:session:"
	DisableKeyConsumer    = "hei:auth:CONSUMER:disable:"

	// Captcha
	CaptchaBusinessCacheKey = "BUSINESS:captcha:"
	CaptchaConsumerCacheKey = "CONSUMER:captcha:"

	// Yes/No
	Yes = "YES"
	No  = "NO"

	// Gender
	GenderMale    = "MALE"
	GenderFemale  = "FEMALE"
	GenderUnknown = "UNKNOWN"
)

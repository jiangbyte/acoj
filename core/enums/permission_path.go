package enums

// PermissionPath represents the resolution path for a permission (lower P = higher priority).
type PermissionPath string

const (
	PermissionPathDIRECT   PermissionPath = "P0" // User → Direct Permission
	PermissionPathUserRole PermissionPath = "P1" // User → Role → Permission
)

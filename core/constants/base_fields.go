package constants

// BaseSystemFields are system-level entity fields that should be excluded
// from VO-to-entity mapping, matching fastapi's BASE_SYSTEM_FIELDS.
var BaseSystemFields = map[string]bool{
	"id":         true,
	"created_at": true,
	"created_by": true,
	"updated_at": true,
	"updated_by": true,
}

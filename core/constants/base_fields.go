package constants

// BASE_SYSTEM_FIELDS lists the system-managed base fields common to all entities.
var BASE_SYSTEM_FIELDS = map[string]bool{
	"id":         true,
	"created_at": true,
	"created_by": true,
	"updated_at": true,
	"updated_by": true,
}

package constants

import "hei-gin/core/enums"

// Base system fields that should be excluded from VO→entity mapping
var BaseSystemFields = map[string]bool{
	"id":         true,
	"created_at": true,
	"created_by": true,
	"updated_at": true,
	"updated_by": true,
}

// Redis cache keys
const (
	PermissionCacheKey = "hei:permission:keys"
)

// Auth token / session prefixes
var (
	TokenPrefixBusiness   = "hei:auth:" + string(enums.LoginTypeBusiness) + ":token:"
	SessionPrefixBusiness = "hei:auth:" + string(enums.LoginTypeBusiness) + ":session:"
	DisableKeyBusiness    = "hei:auth:" + string(enums.LoginTypeBusiness) + ":disable:"

	TokenPrefixConsumer   = "hei:auth:" + string(enums.LoginTypeConsumer) + ":token:"
	SessionPrefixConsumer = "hei:auth:" + string(enums.LoginTypeConsumer) + ":session:"
	DisableKeyConsumer    = "hei:auth:" + string(enums.LoginTypeConsumer) + ":disable:"
)

// Dict cache keys
const (
	DictCacheKey     = "hei:dict:tree"
	DictTreeCacheKey = "hei:dict:fulltree"
)

// Captcha cache key prefixes
var (
	CaptchaBusinessPrefix = string(enums.LoginTypeBusiness) + ":captcha:"
	CaptchaConsumerPrefix = string(enums.LoginTypeConsumer) + ":captcha:"
)

// NoRepeat key prefix
const (
	NoRepeatPrefix = "norepeat:"
)

// SUPER_ADMIN role code
const (
	SuperAdminCode = "SUPER_ADMIN"
)

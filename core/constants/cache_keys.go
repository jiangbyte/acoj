package constants

import "hei-gin/core/enums"

// Redis cache keys

// Permission
const PERMISSION_CACHE_KEY = "hei:permission:keys"

// Auth token / session
var (
	TOKEN_PREFIX_BUSINESS   = "hei:auth:" + string(enums.LoginTypeBusiness) + ":token:"
	SESSION_PREFIX_BUSINESS = "hei:auth:" + string(enums.LoginTypeBusiness) + ":session:"
	DISABLE_KEY_BUSINESS    = "hei:auth:" + string(enums.LoginTypeBusiness) + ":disable:"

	TOKEN_PREFIX_CONSUMER   = "hei:auth:" + string(enums.LoginTypeConsumer) + ":token:"
	SESSION_PREFIX_CONSUMER = "hei:auth:" + string(enums.LoginTypeConsumer) + ":session:"
	DISABLE_KEY_CONSUMER    = "hei:auth:" + string(enums.LoginTypeConsumer) + ":disable:"
)

// Dict
const (
	DICT_CACHE_KEY      = "hei:dict:tree"
	DICT_TREE_CACHE_KEY = "hei:dict:fulltree"
)

// Captcha
var (
	CAPTCHA_BUSINESS_CACHE_KEY = string(enums.LoginTypeBusiness) + ":captcha:"
	CAPTCHA_CONSUMER_CACHE_KEY = string(enums.LoginTypeConsumer) + ":captcha:"
)

// No-repeat
const NO_REPEAT_PREFIX = "norepeat:"

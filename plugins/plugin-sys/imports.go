// Package plugin_sys blank-imports all sub-packages to trigger their init() functions
// for model registration and route registration.
package plugin_sys

import (
	// Model registrations (migrate.go)
	_ "hei-gin/plugins/plugin-sys/user"
	_ "hei-gin/plugins/plugin-sys/role"
	_ "hei-gin/plugins/plugin-sys/org"
	_ "hei-gin/plugins/plugin-sys/group"
	_ "hei-gin/plugins/plugin-sys/position"
	_ "hei-gin/plugins/plugin-sys/dict"
	_ "hei-gin/plugins/plugin-sys/config"
	_ "hei-gin/plugins/plugin-sys/banner"
	_ "hei-gin/plugins/plugin-sys/home"
	_ "hei-gin/plugins/plugin-sys/log"
	_ "hei-gin/plugins/plugin-sys/notice"
	_ "hei-gin/plugins/plugin-sys/file"
	_ "hei-gin/plugins/plugin-sys/resource"

	// Route registrations (api/v1/register.go)
	_ "hei-gin/plugins/plugin-sys/user/api/v1"
	_ "hei-gin/plugins/plugin-sys/role/api/v1"
	_ "hei-gin/plugins/plugin-sys/org/api/v1"
	_ "hei-gin/plugins/plugin-sys/group/api/v1"
	_ "hei-gin/plugins/plugin-sys/position/api/v1"
	_ "hei-gin/plugins/plugin-sys/dict/api/v1"
	_ "hei-gin/plugins/plugin-sys/config/api/v1"
	_ "hei-gin/plugins/plugin-sys/banner/api/v1"
	_ "hei-gin/plugins/plugin-sys/home/api/v1"
	_ "hei-gin/plugins/plugin-sys/log/api/v1"
	_ "hei-gin/plugins/plugin-sys/notice/api/v1"
	_ "hei-gin/plugins/plugin-sys/file/api/v1"
	_ "hei-gin/plugins/plugin-sys/resource/api/v1"
	_ "hei-gin/plugins/plugin-sys/session/api/v1"
	_ "hei-gin/plugins/plugin-sys/permission/api/v1"
	_ "hei-gin/plugins/plugin-sys/analyze/api/v1"

	// Auth route registrations
	_ "hei-gin/plugins/plugin-sys/auth/username/api/v1"
	_ "hei-gin/plugins/plugin-sys/auth/captcha/api/v1"
	_ "hei-gin/plugins/plugin-sys/auth/sm2/api/v1"
)

package config

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysConfig{})
}

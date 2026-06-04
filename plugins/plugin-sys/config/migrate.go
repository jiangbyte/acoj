package config

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysConfig{})
}

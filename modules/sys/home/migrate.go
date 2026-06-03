package home

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysQuickAction{})
}

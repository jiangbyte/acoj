package home

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysQuickAction{})
}

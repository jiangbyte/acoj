package group

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysGroup{})
}

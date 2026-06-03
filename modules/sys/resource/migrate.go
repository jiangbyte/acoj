package resource

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysResource{})
	db.RegisterModel(&SysModule{})
}

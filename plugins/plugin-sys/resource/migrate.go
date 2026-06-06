package resource

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysResource{})
	db.RegisterModel(&SysModule{})
}

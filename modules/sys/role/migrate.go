package role

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysRole{})
}

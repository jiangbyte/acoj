package role

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysRole{})
}

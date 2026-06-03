package notice

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysNotice{})
}

package notice

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysNotice{})
}

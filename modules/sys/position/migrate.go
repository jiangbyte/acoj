package position

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysPosition{})
}

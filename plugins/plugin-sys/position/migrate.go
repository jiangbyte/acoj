package position

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysPosition{})
}

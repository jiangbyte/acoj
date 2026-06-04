package group

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysGroup{})
}

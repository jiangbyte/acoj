package log

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysLog{})
}

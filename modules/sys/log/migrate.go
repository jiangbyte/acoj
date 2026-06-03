package log

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysLog{})
}

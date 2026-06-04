package sys_message

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysMessage{})
}

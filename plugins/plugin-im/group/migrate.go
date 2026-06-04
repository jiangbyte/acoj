package group

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&GroupChat{})
	db.RegisterModel(&GroupMember{})
	db.RegisterModel(&GroupMessage{})
	db.RegisterModel(&GroupMessageRead{})
}

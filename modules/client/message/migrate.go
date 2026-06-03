package message

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&ClientMessage{})
}

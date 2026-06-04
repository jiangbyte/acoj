package client_message

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&ClientMessage{})
}

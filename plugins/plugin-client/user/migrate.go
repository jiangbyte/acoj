package user

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&ClientUser{})
}

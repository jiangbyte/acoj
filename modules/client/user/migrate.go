package user

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&ClientUser{})
}

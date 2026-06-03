package dict

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysDict{})
}

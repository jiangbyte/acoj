package dict

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysDict{})
}

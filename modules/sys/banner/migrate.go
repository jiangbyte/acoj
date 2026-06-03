package banner

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysBanner{})
}

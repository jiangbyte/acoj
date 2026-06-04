package banner

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysBanner{})
}

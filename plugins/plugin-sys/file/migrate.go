package file

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysFile{})
}

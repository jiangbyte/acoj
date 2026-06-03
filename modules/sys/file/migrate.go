package file

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysFile{})
}

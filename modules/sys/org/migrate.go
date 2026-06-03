package org

import "hei-gin/core/db"

func init() {
	db.RegisterModel(&SysOrg{})
}

package org

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&SysOrg{})
}

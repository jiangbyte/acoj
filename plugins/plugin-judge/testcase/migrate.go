package testcase

import (
	"hei-gin/sdk/db"
)

func init() {
	db.RegisterModel(&JudgeTestcase{})
}

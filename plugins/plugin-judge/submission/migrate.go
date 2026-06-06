package submission

import (
	"hei-gin/sdk/db"
)

func init() {
	db.RegisterModel(&JudgeSubmission{})
}

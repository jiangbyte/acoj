package tag

import (
	"hei-gin/sdk/db"
)

func init() {
	db.RegisterModel(&JudgeTag{})
}

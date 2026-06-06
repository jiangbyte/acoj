package problem

import (
	"hei-gin/sdk/db"
)

func init() {
	db.RegisterModel(&JudgeProblem{})
}

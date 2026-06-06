package problemset

import (
	"hei-gin/sdk/db"
)

func init() {
	db.RegisterModel(&JudgeProblemset{})
	db.RegisterModel(&RelProblemsetProblem{})
}

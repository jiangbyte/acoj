package contest

import (
	"hei-gin/sdk/db"
)

func init() {
	db.RegisterModel(&JudgeContest{})
	db.RegisterModel(&RelContestProblem{})
	db.RegisterModel(&RelContestUser{})
}

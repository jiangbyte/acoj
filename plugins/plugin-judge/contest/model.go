package contest

import "time"

// JudgeContest 竞赛实体
type JudgeContest struct {
	ID          string     `gorm:"primaryKey;size:32" json:"id"`
	Title       string     `gorm:"size:255;index" json:"title"`
	Description string     `gorm:"type:text" json:"description"`
	Type        string     `gorm:"size:16;default:ACM" json:"type"` // ACM / OI / IOI
	Rule        string     `gorm:"size:16;default:PRIVATE" json:"rule"` // PUBLIC / PRIVATE / PASSWORD
	Password    string     `gorm:"size:255" json:"password"`
	StartTime   *time.Time `json:"start_time"`
	EndTime     *time.Time `json:"end_time"`
	Status      string     `gorm:"size:16;default:PENDING" json:"status"` // PENDING / RUNNING / ENDED / CANCELED
	CreatedBy   string     `gorm:"size:32" json:"created_by"`
	CreatedAt   *time.Time `json:"created_at"`
	UpdatedAt   *time.Time `json:"updated_at"`
}

func (JudgeContest) TableName() string { return "judge_contest" }

// RelContestProblem 竞赛-题目关联
type RelContestProblem struct {
	ID         string `gorm:"primaryKey;size:32" json:"id"`
	ContestID  string `gorm:"size:32;uniqueIndex:idx_contest_problem" json:"contest_id"`
	ProblemID  string `gorm:"size:32;uniqueIndex:idx_contest_problem;index" json:"problem_id"`
	Sort       int    `gorm:"default:0" json:"sort"`
	Score      int    `gorm:"default:100" json:"score"`
}

func (RelContestProblem) TableName() string { return "rel_contest_problem" }

// RelContestUser 竞赛-用户关联（报名）
type RelContestUser struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	ContestID string     `gorm:"size:32;uniqueIndex:idx_contest_user" json:"contest_id"`
	UserID    string     `gorm:"size:32;uniqueIndex:idx_contest_user;index" json:"user_id"`
	CreatedAt *time.Time `json:"created_at"`
}

func (RelContestUser) TableName() string { return "rel_contest_user" }

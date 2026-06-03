package contest

import "time"

type JudgeContest struct {
	ID                string     `gorm:"primaryKey;size:32" json:"id"`
	Title             string     `gorm:"size:255;not null" json:"title"`
	Description       string     `gorm:"type:longtext" json:"description"`
	Mode              string     `gorm:"size:32" json:"mode"`
	StartTime         time.Time  `json:"start_time"`
	EndTime           time.Time  `json:"end_time"`
	FreezeTime        *time.Time `json:"freeze_time"`
	UnfreezeTime      *time.Time `json:"unfreeze_time"`
	Duration          int        `json:"duration"`
	Status            string     `gorm:"size:16;default:PENDING" json:"status"`
	ShowRank          bool       `gorm:"default:true" json:"show_rank"`
	ShowAnswer        bool       `gorm:"default:false" json:"show_answer"`
	MaxAttempts       int        `gorm:"default:0" json:"max_attempts"`
	PenaltyDecay      float64    `gorm:"default:0" json:"penalty_decay"`
	LateSubmitPenalty float64    `gorm:"default:0" json:"late_submit_penalty"`
	CreatedAt         *time.Time `json:"created_at"`
	CreatedBy         *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt         *time.Time `json:"updated_at"`
	UpdatedBy         *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeContest) TableName() string { return "judge_contest" }

type JudgeContestProblem struct {
	ID            string  `gorm:"primaryKey;size:32" json:"id"`
	ContestID     string  `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"contest_id"`
	ProblemID     string  `gorm:"size:32;index" json:"problem_id"`
	SortOrder     int     `json:"sort_order"`
	Label         string  `gorm:"size:8" json:"label"`
	TimeLimitMs   *int    `json:"time_limit_ms"`
	MemoryLimitKb *int64  `json:"memory_limit_kb"`
	Score         int     `json:"score"`
	IsPretestOnly bool    `json:"is_pretest_only"`
}

func (JudgeContestProblem) TableName() string { return "judge_contest_problem" }

type JudgeContestParticipant struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	ContestID string     `gorm:"size:32;index;uniqueIndex:idx_contest_user;constraint:OnDelete:CASCADE" json:"contest_id"`
	UserID    string     `gorm:"size:32;index;uniqueIndex:idx_contest_user" json:"user_id"`
	StartTime *time.Time `json:"start_time"`
	Status    string     `gorm:"size:16;default:NORMAL" json:"status"`
}

func (JudgeContestParticipant) TableName() string { return "judge_contest_participant" }

type JudgeContestRankItem struct {
	ID           string     `gorm:"primaryKey;size:32" json:"id"`
	ContestID    string     `gorm:"size:32;index;uniqueIndex:idx_rank_cup;constraint:OnDelete:CASCADE" json:"contest_id"`
	UserID       string     `gorm:"size:32;index;uniqueIndex:idx_rank_cup" json:"user_id"`
	ProblemID    string     `gorm:"size:32;index;uniqueIndex:idx_rank_cup" json:"problem_id"`
	SubmissionID string     `gorm:"size:32" json:"submission_id"`
	Attempts     int        `gorm:"default:0" json:"attempts"`
	TimePenalty  int64      `gorm:"default:0" json:"time_penalty"`
	Score        int        `gorm:"default:0" json:"score"`
	IsAccepted   bool       `json:"is_accepted"`
	FirstAC      bool       `json:"first_ac"`
	SubmitAt     *time.Time `json:"submit_at"`
}

func (JudgeContestRankItem) TableName() string { return "judge_contest_rank_item" }

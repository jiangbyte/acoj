package contest

import "time"

type ContestVO struct {
	ID                string             `json:"id"`
	Title             string             `json:"title"`
	Description       string             `json:"description"`
	Mode              string             `json:"mode"`
	StartTime         string             `json:"start_time"`
	EndTime           string             `json:"end_time"`
	FreezeTime        *string            `json:"freeze_time"`
	UnfreezeTime      *string            `json:"unfreeze_time"`
	Duration          int                `json:"duration"`
	Status            string             `json:"status"`
	ShowRank          bool               `json:"show_rank"`
	ShowAnswer        bool               `json:"show_answer"`
	MaxAttempts       int                `json:"max_attempts"`
	PenaltyDecay      float64            `json:"penalty_decay"`
	LateSubmitPenalty float64            `json:"late_submit_penalty"`
	ParticipantCount  int64              `json:"participant_count,omitempty"`
	CreatedAt         *string            `json:"created_at"`
	UpdatedAt         *string            `json:"updated_at"`
	Problems          []ContestProblemVO           `json:"problems,omitempty"`
	Announcements    []ContestAnnouncementVO      `json:"announcements,omitempty"`
}

type ContestProblemVO struct {
	ID            string `json:"id"`
	ProblemID     string `json:"problem_id"`
	SortOrder     int    `json:"sort_order"`
	Label         string `json:"label"`
	TimeLimitMs   *int   `json:"time_limit_ms"`
	MemoryLimitKb *int64 `json:"memory_limit_kb"`
	Score         int    `json:"score"`
	IsPretestOnly bool   `json:"is_pretest_only"`
}

type ContestPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Title   string `json:"title" form:"title"`
	Mode    string `json:"mode" form:"mode"`
	Status  string `json:"status" form:"status"`
}

type ContestCreateParam struct {
	Title             string     `json:"title"`
	Description       string     `json:"description"`
	Mode              string     `json:"mode"`
	StartTime         time.Time  `json:"start_time"`
	EndTime           time.Time  `json:"end_time"`
	FreezeTime        *time.Time `json:"freeze_time"`
	UnfreezeTime      *time.Time `json:"unfreeze_time"`
	Duration          int        `json:"duration"`
	ShowRank          bool       `json:"show_rank"`
	ShowAnswer        bool       `json:"show_answer"`
	MaxAttempts       int        `json:"max_attempts"`
	PenaltyDecay      float64    `json:"penalty_decay"`
	LateSubmitPenalty float64    `json:"late_submit_penalty"`
}

type ContestModifyParam struct {
	ID                string     `json:"id"`
	Title             string     `json:"title"`
	Description       string     `json:"description"`
	Mode              string     `json:"mode"`
	StartTime         time.Time  `json:"start_time"`
	EndTime           time.Time  `json:"end_time"`
	FreezeTime        *time.Time `json:"freeze_time"`
	UnfreezeTime      *time.Time `json:"unfreeze_time"`
	Duration          int        `json:"duration"`
	ShowRank          bool       `json:"show_rank"`
	ShowAnswer        bool       `json:"show_answer"`
	MaxAttempts       int        `json:"max_attempts"`
	PenaltyDecay      float64    `json:"penalty_decay"`
	LateSubmitPenalty float64    `json:"late_submit_penalty"`
}




// ContestAnnouncementVO represents a contest announcement.
type ContestAnnouncementVO struct {
	ID        string  `json:"id"`
	Title     string  `json:"title"`
	Content   string  `json:"content"`
	Pinned    bool    `json:"pinned"`
	CreatedAt *string `json:"created_at"`
}

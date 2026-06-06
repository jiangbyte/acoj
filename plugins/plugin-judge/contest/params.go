package contest

import "hei-gin/sdk/pojo"

type ContestVO struct {
	ID          string `json:"id"`
	Title       string `json:"title"`
	Description string `json:"description"`
	Type        string `json:"type"`
	Rule        string `json:"rule"`
	StartTime   string `json:"start_time"`
	EndTime     string `json:"end_time"`
	Status      string `json:"status"`
	ProblemCount int   `json:"problem_count"`
	UserCount   int    `json:"user_count"`
	CreatedBy   string `json:"created_by"`
	CreatedAt   string `json:"created_at"`
}

type ContestDetailVO struct {
	ContestVO
	IsRegistered bool                 `json:"is_registered"`
	Problems     []ContestProblemItem `json:"problems"`
}

type ContestProblemItem struct {
	ProblemID string `json:"problem_id"`
	Title     string `json:"title"`
	Sort      int    `json:"sort"`
	Score     int    `json:"score"`
}

type ContestPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword" form:"keyword"`
	Type    string `json:"type" form:"type"`
	Status  string `json:"status" form:"status"`
}

type ContestCreateParam struct {
	Title       string   `json:"title" binding:"required"`
	Description string   `json:"description"`
	Type        string   `json:"type"`
	Rule        string   `json:"rule"`
	Password    string   `json:"password"`
	StartTime   string   `json:"start_time" binding:"required"`
	EndTime     string   `json:"end_time" binding:"required"`
	ProblemIDs  []string `json:"problem_ids"`
}

type ContestModifyParam struct {
	ID          string   `json:"id" binding:"required"`
	Title       string   `json:"title"`
	Description string   `json:"description"`
	Type        string   `json:"type"`
	Rule        string   `json:"rule"`
	Password    string   `json:"password"`
	StartTime   string   `json:"start_time"`
	EndTime     string   `json:"end_time"`
	ProblemIDs  []string `json:"problem_ids"`
}

type ContestRemoveParam pojo.IdsParam

type ContestRegisterParam struct {
	ContestID string `json:"contest_id" binding:"required"`
	Password  string `json:"password"`
}

// ContestRankItem 排行榜条目
type ContestRankItem struct {
	Rank     int              `json:"rank"`
	UserID   string           `json:"user_id"`
	Username string           `json:"username"`
	Solved   int              `json:"solved"`
	TotalTime int64           `json:"total_time"`
	Score    int              `json:"score"`
	Details  []ProblemRankItem `json:"details"`
}

type ProblemRankItem struct {
	ProblemID   string `json:"problem_id"`
	Label       string `json:"label"`
	Accepted    bool   `json:"accepted"`
	Attempts    int    `json:"attempts"`
	TimeUsed    int64  `json:"time_used"`
	Score       int    `json:"score"`
	SubmitTime  string `json:"submit_time"`
}

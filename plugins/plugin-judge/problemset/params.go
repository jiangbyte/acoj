package problemset

import "hei-gin/sdk/pojo"

type ProblemsetVO struct {
	ID          string `json:"id"`
	Title       string `json:"title"`
	Description string `json:"description"`
	Status      string `json:"status"`
	Sort        int    `json:"sort"`
	ProblemCount int   `json:"problem_count"`
	CreatedBy   string `json:"created_by"`
	CreatedAt   string `json:"created_at"`
}

type ProblemsetPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword" form:"keyword"`
	Status  string `json:"status" form:"status"`
}

type ProblemsetCreateParam struct {
	Title       string `json:"title" binding:"required"`
	Description string `json:"description"`
	Status      string `json:"status"`
	Sort        int    `json:"sort"`
	ProblemIDs  []string `json:"problem_ids"`
}

type ProblemsetModifyParam struct {
	ID          string   `json:"id" binding:"required"`
	Title       string   `json:"title"`
	Description string   `json:"description"`
	Status      string   `json:"status"`
	Sort        *int     `json:"sort"`
	ProblemIDs  []string `json:"problem_ids"`
}

type ProblemsetRemoveParam pojo.IdsParam

type ProblemsetAddProblemParam struct {
	ProblemsetID string `json:"problemset_id" binding:"required"`
	ProblemID    string `json:"problem_id" binding:"required"`
	Sort         int    `json:"sort"`
}

type ProblemsetRemoveProblemParam struct {
	ProblemsetID string `json:"problemset_id" binding:"required"`
	ProblemID    string `json:"problem_id" binding:"required"`
}

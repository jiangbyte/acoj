package submission

import "hei-gin/sdk/pojo"

type SubmissionVO struct {
	ID              string `json:"id"`
	ProblemID       string `json:"problem_id"`
	ProblemTitle    string `json:"problem_title"`
	UserID          string `json:"user_id"`
	Username        string `json:"username"`
	ContestID       string `json:"contest_id"`
	Language        string `json:"language"`
	Code            string `json:"code"`
	Status          string `json:"status"`
	SubmissionType  string `json:"submission_type"`
	Score           int    `json:"score"`
	TimeUsed        int64  `json:"time_used"`
	MemoryUsed      int64  `json:"memory_used"`
	ErrorMessage    string `json:"error_message"`
	CreatedAt       string `json:"created_at"`
}

type SubmissionPageParam struct {
	Current        int    `json:"current" form:"current"`
	Size           int    `json:"size" form:"size"`
	ProblemID      string `json:"problem_id" form:"problem_id"`
	UserID         string `json:"user_id" form:"user_id"`
	Status         string `json:"status" form:"status"`
	Language       string `json:"language" form:"language"`
	ContestID      string `json:"contest_id" form:"contest_id"`
	SubmissionType string `json:"submission_type" form:"submission_type"`
}

type SubmissionCreateParam struct {
	ProblemID      string `json:"problem_id" binding:"required"`
	Language       string `json:"language" binding:"required"`
	Code           string `json:"code" binding:"required"`
	ContestID      string `json:"contest_id"`
	SubmissionType string `json:"submission_type"`
}

type SubmissionRejudgeParam pojo.IdsParam

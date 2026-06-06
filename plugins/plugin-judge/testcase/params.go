package testcase

import "hei-gin/sdk/pojo"

type TestcaseVO struct {
	ID        string `json:"id"`
	ProblemID string `json:"problem_id"`
	Input     string `json:"input"`
	Output    string `json:"output"`
	Order     int    `json:"order"`
	IsSample  bool   `json:"is_sample"`
	Score     int    `json:"score"`
	CreatedAt string `json:"created_at"`
}

type TestcaseListParam struct {
	ProblemID string `json:"problem_id" form:"problem_id" binding:"required"`
}

type TestcaseCreateParam struct {
	ProblemID string `json:"problem_id" binding:"required"`
	Input     string `json:"input"`
	Output    string `json:"output"`
	Order     int    `json:"order"`
	IsSample  bool   `json:"is_sample"`
	Score     int    `json:"score"`
}

type TestcaseModifyParam struct {
	ID       string `json:"id" binding:"required"`
	Input    string `json:"input"`
	Output   string `json:"output"`
	Order    *int   `json:"order"`
	IsSample *bool  `json:"is_sample"`
	Score    *int   `json:"score"`
}

type TestcaseRemoveParam pojo.IdsParam

type TestcaseBatchCreateParam struct {
	ProblemID string                  `json:"problem_id" binding:"required"`
	Cases     []TestcaseCreateParam   `json:"cases" binding:"required"`
}

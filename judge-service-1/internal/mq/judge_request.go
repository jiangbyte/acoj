package mq

type JudgeRequest struct {
	UserId      string  `json:"userId"`
	ProblemId   string  `json:"problemId"`
	SetId       string  `json:"setId"`
	Language    string  `json:"language"`
	Code        string  `json:"code"`
	SubmitType  bool    `json:"submitType"`
	MaxTime     float64 `json:"maxTime"`   // ms
	MaxMemory   float64 `json:"maxMemory"` // kb
	ID          string  `json:"id"`
	IsSet       bool    `json:"isSet"`
	JudgeTaskId string  `json:"judgeTaskId"`
}

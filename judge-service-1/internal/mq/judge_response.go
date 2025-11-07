package mq

type JudgeResponse struct {
	UserId     string  `json:"userId"`
	ProblemId  string  `json:"problemId"`
	Language   string  `json:"language"`
	Code       string  `json:"code"`
	SubmitType bool    `json:"submitType"`
	MaxTime    float64 `json:"maxTime"`   // ms
	MaxMemory  float64 `json:"maxMemory"` // kb
	Message    string  `json:"message"`
	ID         string  `json:"id"`

	ModuleId   string `json:"moduleId"`
	ModuleType string `json:"moduleType"`

	Status      string `json:"status"`
	ExitCode    int    `json:"exitCode"` // 程序退出码
	JudgeTaskId string `json:"judgeTaskId"`

	Score float64 `json:"score"`
}

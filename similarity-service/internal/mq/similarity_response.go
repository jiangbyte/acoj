package mq

type SimilarityResultMessage struct {
	TaskID         string   `json:"taskId"`         // 任务ID
	SubmitID       string   `json:"submitId"`       // 提交ID
	SetID          string   `json:"setId"`          // 题集
	ProblemID      string   `json:"problemId"`      // 多个或单个题目
	IsSet          bool     `json:"isSet"`          // 是否是题集
	UserID         string   `json:"userId"`         // 多个或单个题目
	Language       string   `json:"language"`       // 语言
	MinMatchLength int      `json:"minMatchLength"` // 敏感度
	Threshold      float64  `json:"threshold"`      // 阈值
	TaskType       bool     `json:"taskType"`       // 是否手动
	CodeTokens     []int    `json:"codeTokens"`
	CodeTokenNames []string `json:"codeTokenNames"`
	CodeTokenTexts []string `json:"codeTokenTexts"`
	Similarity     float64  `json:"similarity"` // 相似度
}

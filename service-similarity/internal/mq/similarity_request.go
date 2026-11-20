package mq

type SimilarityMessage struct {
	ReportID       string   `json:"reportId"`       // 统计ID
	TaskID         string   `json:"taskId"`         // 任务ID
	LibIDs         []string `json:"libIds"`         // 代码库ID
	MinMatchLength int      `json:"minMatchLength"` // 敏感度
	Threshold      float64  `json:"threshold"`      // 检测阈值
	TaskType       bool     `json:"taskType"`       // 手动
}

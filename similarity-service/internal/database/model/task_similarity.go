package model

import (
	"database/sql"
	"time"
)

type CloneLevel struct {
	CloneLevel     string          `json:"cloneLevel"`
	CloneLevelName string          `json:"cloneLevelName"`
	Similarity     sql.NullFloat64 `json:"similarity"`
	Count          int             `json:"count"`
	Percentage     float64         `json:"percentage"`
}

type TaskReportStats struct {
	SampleCount   int     `json:"sampleCount"`
	GroupCount    int     `json:"groupCount"`
	AvgSimilarity float64 `json:"avgSimilarity"`
	MaxSimilarity float64 `json:"maxSimilarity"`
}

// 检测结果任务库
type TaskSimilarity struct {
	ID               string     `gorm:"column:id;primaryKey;type:varchar(32)"`
	TaskID           string     `gorm:"column:task_id;type:varchar(32)"`
	TaskType         bool       `gorm:"column:task_type;default:false"`
	ProblemID        string     `gorm:"column:problem_id;type:varchar(32)"`
	SetID            string     `gorm:"column:set_id;type:varchar(32)"`
	IsSet            bool       `gorm:"column:is_set;default:false"`
	Language         string     `gorm:"column:language;type:varchar(64)"`
	Similarity       float64    `gorm:"column:similarity;type:decimal(10,2);default:0.00"`
	SubmitUser       string     `gorm:"column:submit_user;type:varchar(32)"`
	SubmitCode       string     `gorm:"column:submit_code;type:longtext"`
	SubmitCodeLength int        `gorm:"column:submit_code_length;default:0"`
	SubmitID         string     `gorm:"column:submit_id;type:varchar(32)"`
	SubmitTime       *time.Time `gorm:"column:submit_time"`
	SubmitCodeToken  string     `gorm:"column:submit_code_token;type:json"`
	SubmitTokenName  string     `gorm:"column:submit_token_name;type:json"`
	SubmitTokenTexts string     `gorm:"column:submit_token_texts;type:json"`
	OriginUser       string     `gorm:"column:origin_user;type:varchar(32)"`
	OriginCode       string     `gorm:"column:origin_code;type:longtext"`
	OriginCodeLength int        `gorm:"column:origin_code_length;default:0"`
	OriginID         string     `gorm:"column:origin_id;type:varchar(32)"`
	OriginTime       *time.Time `gorm:"column:origin_time"`
	OriginCodeToken  string     `gorm:"column:origin_code_token;type:json"`
	OriginTokenName  string     `gorm:"column:origin_token_name;type:json"`
	OriginTokenTexts string     `gorm:"column:origin_token_texts;type:json"`
	Deleted          bool       `gorm:"column:deleted;default:false"`
	CreateTime       *time.Time `gorm:"column:create_time"`
	CreateUser       string     `gorm:"column:create_user;type:varchar(32)"`
	UpdateTime       *time.Time `gorm:"column:update_time"`
	UpdateUser       string     `gorm:"column:update_user;type:varchar(32)"`
}

func (TaskSimilarity) TableName() string {
	return "task_similarity"
}

package model

import (
	"time"
)

// 报告库表
type TaskReports struct {
	ID                     string     `gorm:"column:id;primaryKey;type:varchar(32)"`
	ReportType             int        `gorm:"column:report_type;default:0"`
	TaskID                 string     `gorm:"column:task_id;type:varchar(32)"`
	SetID                  string     `gorm:"column:set_id;type:varchar(32)"`
	IsSet                  bool       `gorm:"column:is_set;default:false"`
	ProblemID              string     `gorm:"column:problem_id;type:varchar(32)"`
	SampleCount            int        `gorm:"column:sample_count;default:0"`
	SimilarityGroupCount   int        `gorm:"column:similarity_group_count;default:0"`
	AvgSimilarity          float64    `gorm:"column:avg_similarity;type:decimal(10,2);default:0.00"`
	MaxSimilarity          float64    `gorm:"column:max_similarity;type:decimal(10,2);default:0.00"`
	Threshold              float64    `gorm:"column:threshold;type:decimal(10,2);default:0.50"`
	SimilarityDistribution string     `gorm:"column:similarity_distribution;type:json"`
	DegreeStatistics       string     `gorm:"column:degree_statistics;type:json"`
	CheckMode              int        `gorm:"column:check_mode;default:1"`
	Deleted                bool       `gorm:"column:deleted;default:false"`
	CreateTime             *time.Time `gorm:"column:create_time"`
	CreateUser             string     `gorm:"column:create_user;type:varchar(32)"`
	UpdateTime             *time.Time `gorm:"column:update_time"`
	UpdateUser             string     `gorm:"column:update_user;type:varchar(32)"`
}

func (TaskReports) TableName() string {
	return "task_reports"
}

package model

import (
	"time"
)

// 判题结果用例表
type DataJudgeCase struct {
	ID             string     `gorm:"column:id;primaryKey;type:varchar(32)"`
	SubmitID       string     `gorm:"column:submit_id;type:varchar(32);not null"`
	CaseSign       string     `gorm:"column:case_sign;type:varchar(255)"`
	InputData      string     `gorm:"column:input_data;type:longtext"`
	OutputData     string     `gorm:"column:output_data;type:longtext"`
	ExpectedOutput string     `gorm:"column:expected_output;type:longtext"`
	InputFilePath  string     `gorm:"column:input_file_path;type:varchar(500)"`
	InputFileSize  int64      `gorm:"column:input_file_size;default:0"`
	OutputFilePath string     `gorm:"column:output_file_path;type:varchar(500)"`
	OutputFileSize int64      `gorm:"column:output_file_size;default:0"`
	MaxTime        float64    `gorm:"column:max_time;type:decimal(10,2);default:0.00"`
	MaxMemory      float64    `gorm:"column:max_memory;type:decimal(10,2);default:0.00"`
	IsSample       bool       `gorm:"column:is_sample;default:false"`
	Score          float64    `gorm:"column:score;type:decimal(10,2);default:0.00"`
	Status         string     `gorm:"column:status;type:varchar(32)"`
	Message        string     `gorm:"column:message;type:text"`
	ExitCode       int        `gorm:"column:exit_code;default:0"`
	Deleted        bool       `gorm:"column:deleted;default:false"`
	CreateTime     *time.Time `gorm:"column:create_time"`
	CreateUser     string     `gorm:"column:create_user;type:varchar(32)"`
	UpdateTime     *time.Time `gorm:"column:update_time"`
	UpdateUser     string     `gorm:"column:update_user;type:varchar(32)"`
}

func (DataJudgeCase) TableName() string {
	return "data_judge_case"
}

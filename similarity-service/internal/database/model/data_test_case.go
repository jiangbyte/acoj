package model

import (
	"time"
)

// 题目测试用例表
type DataTestCase struct {
	ID             string     `gorm:"column:id;primaryKey;type:varchar(32)"`
	ProblemID      string     `gorm:"column:problem_id;type:varchar(32);not null"`
	CaseSign       string     `gorm:"column:case_sign;type:varchar(255)"`
	InputData      string     `gorm:"column:input_data;type:longtext"`
	ExpectedOutput string     `gorm:"column:expected_output;type:longtext"`
	InputFilePath  string     `gorm:"column:input_file_path;type:varchar(500)"`
	InputFileSize  int64      `gorm:"column:input_file_size;default:0"`
	OutputFilePath string     `gorm:"column:output_file_path;type:varchar(500)"`
	OutputFileSize int64      `gorm:"column:output_file_size;default:0"`
	IsSample       bool       `gorm:"column:is_sample;default:false"`
	Score          float64    `gorm:"column:score;type:decimal(10,2);default:0.00"`
	Deleted        bool       `gorm:"column:deleted;default:false"`
	CreateTime     *time.Time `gorm:"column:create_time"`
	CreateUser     string     `gorm:"column:create_user;type:varchar(32)"`
	UpdateTime     *time.Time `gorm:"column:update_time"`
	UpdateUser     string     `gorm:"column:update_user;type:varchar(32)"`
}

func (DataTestCase) TableName() string {
	return "data_test_case"
}

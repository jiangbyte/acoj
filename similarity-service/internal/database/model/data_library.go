package model

import (
	"time"
)

// 提交样本库
type DataLibrary struct {
	ID             string     `gorm:"column:id;primaryKey;type:varchar(32)"`
	UserID         string     `gorm:"column:user_id;type:varchar(32)"`
	SetID          string     `gorm:"column:set_id;type:varchar(32)"`
	IsSet          bool       `gorm:"column:is_set;default:false"`
	ProblemID      string     `gorm:"column:problem_id;type:varchar(32)"`
	SubmitID       string     `gorm:"column:submit_id;type:varchar(32)"`
	SubmitTime     *time.Time `gorm:"column:submit_time"`
	Language       string     `gorm:"column:language;type:varchar(64)"`
	Code           string     `gorm:"column:code;type:longtext"`
	CodeToken      string     `gorm:"column:code_token;type:json"` // 实际使用时可定义为具体的结构体类型
	CodeTokenName  string     `gorm:"column:code_token_name;type:json"`
	CodeTokenTexts string     `gorm:"column:code_token_texts;type:json"`
	CodeLength     int        `gorm:"column:code_length;default:0"`
	AccessCount    int        `gorm:"column:access_count;default:0"`
	Deleted        bool       `gorm:"column:deleted;default:false"`
	CreateTime     *time.Time `gorm:"column:create_time"`
	CreateUser     string     `gorm:"column:create_user;type:varchar(32)"`
	UpdateTime     *time.Time `gorm:"column:update_time"`
	UpdateUser     string     `gorm:"column:update_user;type:varchar(32)"`
}

func (DataLibrary) TableName() string {
	return "data_library"
}

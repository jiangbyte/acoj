package judge

import "time"

// JudgeConfig 判题配置实体
type JudgeConfig struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	Key       string     `gorm:"size:64;uniqueIndex" json:"key"`
	Value     string     `gorm:"type:text" json:"value"`
	Desc      string     `gorm:"size:255" json:"desc"`
	CreatedAt *time.Time `json:"created_at"`
	UpdatedAt *time.Time `json:"updated_at"`
}

func (JudgeConfig) TableName() string { return "judge_config" }

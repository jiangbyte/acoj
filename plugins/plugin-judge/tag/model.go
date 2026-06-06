package tag

import "time"

// JudgeTag 标签实体
type JudgeTag struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	Name      string     `gorm:"size:64;uniqueIndex" json:"name"`
	Color     string     `gorm:"size:16" json:"color"`
	CreatedAt *time.Time `json:"created_at"`
	UpdatedAt *time.Time `json:"updated_at"`
}

func (JudgeTag) TableName() string { return "judge_tag" }

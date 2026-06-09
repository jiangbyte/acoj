package sandbox

import "time"

// JudgeSandbox 沙箱实例持久化模型
type JudgeSandbox struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	Name      string     `gorm:"size:64;uniqueIndex" json:"name"`
	Endpoint  string     `gorm:"size:255" json:"endpoint"`
	Timeout   int        `gorm:"default:30" json:"timeout"`
	Status    string     `gorm:"size:16;default:active;index" json:"status"` // active / offline / removed
	CreatedAt *time.Time `json:"created_at"`
	CreatedBy *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt *time.Time `json:"updated_at"`
	UpdatedBy *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeSandbox) TableName() string { return "judge_sandbox" }

package model

import "time"

// JudgeSandboxInstance represents a go-judge sandbox instance registration.
type JudgeSandboxInstance struct {
	ID            string     `gorm:"primaryKey;size:32" json:"id"`
	Addr          string     `gorm:"uniqueIndex;size:255" json:"addr"`
	Status        string     `gorm:"size:16" json:"status"`
	Weight        int        `gorm:"default:10" json:"weight"`
	Version       string     `gorm:"size:32" json:"version"`
	CPUCores      int        `json:"cpu_cores"`
	MemoryTotal   int64      `json:"memory_total"`
	LastHeartbeat time.Time  `json:"last_heartbeat"`
	FailureCount  int        `gorm:"default:0" json:"failure_count"`
	CreatedAt     *time.Time `json:"created_at"`
	CreatedBy     *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt     *time.Time `json:"updated_at"`
	UpdatedBy     *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeSandboxInstance) TableName() string { return "judge_sandbox_instance" }

const (
	StatusActive   = "ACTIVE"
	StatusUnhealthy = "UNHEALTHY"
	StatusRemoved  = "REMOVED"
)

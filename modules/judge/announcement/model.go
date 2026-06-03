package announcement

import "time"

// JudgeContestAnnouncement represents a contest announcement.
type JudgeContestAnnouncement struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	ContestID string     `gorm:"size:32;index;constraint:OnDelete:CASCADE" json:"contest_id"`
	Title     string     `gorm:"size:255;not null" json:"title"`
	Content   string     `gorm:"type:longtext" json:"content"`
	Pinned    bool       `gorm:"default:false" json:"pinned"`
	CreatedAt *time.Time `json:"created_at"`
	CreatedBy *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt *time.Time `json:"updated_at"`
	UpdatedBy *string    `gorm:"size:32" json:"updated_by"`
}

func (JudgeContestAnnouncement) TableName() string { return "judge_contest_announcement" }

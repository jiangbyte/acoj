package broadcast

import "time"

// Broadcast 全站通知
type Broadcast struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	Title     string     `gorm:"size:255;not null" json:"title"`
	Content   string     `gorm:"type:text" json:"content"`
	Scope     string     `gorm:"size:20;not null;default:ALL" json:"scope"`
	SenderID  string     `gorm:"size:32;not null" json:"sender_id"`
	CreatedAt *time.Time `json:"created_at"`
	UpdatedAt *time.Time `json:"updated_at"`
}

func (Broadcast) TableName() string { return "im_broadcast" }

// BroadcastRead 已读记录
type BroadcastRead struct {
	BroadcastID string     `gorm:"primaryKey;size:32" json:"broadcast_id"`
	UserID      string     `gorm:"primaryKey;size:32" json:"user_id"`
	UserType    string     `gorm:"primaryKey;size:20" json:"user_type"`
	ReadAt      *time.Time `json:"read_at"`
}

func (BroadcastRead) TableName() string { return "im_broadcast_read" }

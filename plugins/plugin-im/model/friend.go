package model

import "time"

// ─── Friend Models ─────────────────────────────────────────────────────

// FriendRequest 好友请求
type FriendRequest struct {
	ID           string     `gorm:"primaryKey;size:32" json:"id"`
	SenderID     string     `gorm:"size:32;not null;index:idx_fr_sender_status" json:"sender_id"`
	SenderType   string     `gorm:"size:20;not null" json:"sender_type"`
	ReceiverID   string     `gorm:"size:32;not null;index:idx_fr_receiver_status" json:"receiver_id"`
	ReceiverType string     `gorm:"size:20;not null" json:"receiver_type"`
	Remark       string     `gorm:"size:255" json:"remark"`
	Status       string     `gorm:"size:10;not null;default:pending" json:"status"` // pending | accepted | rejected
	CreatedAt    *time.Time `json:"created_at"`
	UpdatedAt    *time.Time `json:"updated_at"`
}

func (FriendRequest) TableName() string { return "im_friend_request" }

// Friendship 好友关系（双向各存一条）
type Friendship struct {
	ID         string     `gorm:"primaryKey;size:32" json:"id"`
	UserID     string     `gorm:"size:32;not null;uniqueIndex:idx_fs_pair" json:"user_id"`
	UserType   string     `gorm:"size:20;not null;uniqueIndex:idx_fs_pair" json:"user_type"`
	FriendID   string     `gorm:"size:32;not null;uniqueIndex:idx_fs_pair" json:"friend_id"`
	FriendType string     `gorm:"size:20;not null;uniqueIndex:idx_fs_pair" json:"friend_type"`
	Remark     string     `gorm:"size:100" json:"remark"`
	CreatedAt  *time.Time `json:"created_at"`
}

func (Friendship) TableName() string { return "im_friendship" }

// FriendBlock 黑名单
type FriendBlock struct {
	ID         string     `gorm:"primaryKey;size:32" json:"id"`
	UserID     string     `gorm:"size:32;not null;uniqueIndex:idx_fb_pair" json:"user_id"`
	UserType   string     `gorm:"size:20;not null;uniqueIndex:idx_fb_pair" json:"user_type"`
	BlockedID  string     `gorm:"size:32;not null;uniqueIndex:idx_fb_pair" json:"blocked_id"`
	BlockedType string    `gorm:"size:20;not null;uniqueIndex:idx_fb_pair" json:"blocked_type"`
	CreatedAt  *time.Time `json:"created_at"`
}

func (FriendBlock) TableName() string { return "im_friend_block" }

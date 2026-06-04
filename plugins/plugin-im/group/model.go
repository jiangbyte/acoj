package group

import (
	"time"
)

// GroupChat 群组
type GroupChat struct {
	ID         string    `gorm:"primaryKey;size:32" json:"id"`
	Name       string    `gorm:"size:100;not null" json:"name"`
	Avatar     string    `gorm:"size:255" json:"avatar"`
	OwnerID    string    `gorm:"size:32;not null;index" json:"owner_id"`
	OwnerType  string    `gorm:"size:20;not null" json:"owner_type"` // BUSINESS | CONSUMER
	GroupType  string    `gorm:"size:20;not null;default:mixed" json:"group_type"` // mixed | consumer_only
	Notice     string    `gorm:"type:text" json:"notice"`
	MaxMembers int       `gorm:"default:200" json:"max_members"`
	Status     string    `gorm:"size:10;not null;default:normal" json:"status"` // normal | dissolved
	CreatedAt  *time.Time `json:"created_at"`
	UpdatedAt  *time.Time `json:"updated_at"`
}

func (GroupChat) TableName() string { return "group_chat" }

// GroupMember 群成员
type GroupMember struct {
	ID        string    `gorm:"primaryKey;size:32" json:"id"`
	GroupID   string    `gorm:"size:32;not null;uniqueIndex:idx_group_user;index" json:"group_id"`
	UserID    string    `gorm:"size:32;not null;uniqueIndex:idx_group_user" json:"user_id"`
	UserType  string    `gorm:"size:20;not null;uniqueIndex:idx_group_user" json:"user_type"`
	Role      string    `gorm:"size:10;not null;default:member" json:"role"` // owner | admin | member
	Nickname  string    `gorm:"size:100" json:"nickname"`
	MutedUntil *time.Time `json:"muted_until"`
	JoinedAt  *time.Time `json:"joined_at"`
	Status    string    `gorm:"size:10;not null;default:active" json:"status"` // active | left | kicked
}

func (GroupMember) TableName() string { return "group_member" }

// GroupMessage 群消息
type GroupMessage struct {
	ID         string     `gorm:"primaryKey;size:32" json:"id"`
	GroupID    string     `gorm:"size:32;not null;index:idx_group_created,priority:1" json:"group_id"`
	SenderID   string     `gorm:"size:32;not null;index" json:"sender_id"`
	SenderType string     `gorm:"size:20;not null" json:"sender_type"`
	Content    string     `gorm:"type:text" json:"content"`
	Extra      string     `gorm:"type:text" json:"extra"`               // JSON TEXT, not native JSON
	MsgType    string     `gorm:"size:20;not null;default:text" json:"msg_type"`
	ReplyTo    string     `gorm:"size:32;index" json:"reply_to"`
	CreatedAt  *time.Time `gorm:"index:idx_group_created,priority:2" json:"created_at"`
}

func (GroupMessage) TableName() string { return "group_message" }

// GroupMessageRead 群消息已读
type GroupMessageRead struct {
	MessageID string     `gorm:"primaryKey;size:32" json:"message_id"`
	GroupID   string     `gorm:"size:32;not null;index" json:"group_id"`
	UserID    string     `gorm:"primaryKey;size:32" json:"user_id"`
	UserType  string     `gorm:"primaryKey;size:20" json:"user_type"`
	ReadAt    *time.Time `json:"read_at"`
}

func (GroupMessageRead) TableName() string { return "group_message_read" }

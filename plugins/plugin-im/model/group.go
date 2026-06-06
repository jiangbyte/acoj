package model

import "time"

// ─── Group Models ──────────────────────────────────────────────────────

// Group 群组
type Group struct {
	ID         string     `gorm:"primaryKey;size:32" json:"id"`
	Name       string     `gorm:"size:100;not null" json:"name"`
	Avatar     string     `gorm:"size:255" json:"avatar"`
	OwnerID    string     `gorm:"size:32;not null;index" json:"owner_id"`
	OwnerType  string     `gorm:"size:20;not null" json:"owner_type"`
	GroupType  string     `gorm:"size:20;not null;default:mixed" json:"group_type"`
	Notice     string     `gorm:"type:text" json:"notice"`
	IsPublic   bool       `gorm:"default:false" json:"is_public"`
	MaxMembers int        `gorm:"default:200" json:"max_members"`
	Status     string     `gorm:"size:10;not null;default:normal" json:"status"`
	CreatedAt  *time.Time `json:"created_at"`
	UpdatedAt  *time.Time `json:"updated_at"`
}

func (Group) TableName() string { return "im_group" }

// GroupMember 群成员
type GroupMember struct {
	ID         string     `gorm:"primaryKey;size:32" json:"id"`
	GroupID    string     `gorm:"size:32;not null;uniqueIndex:idx_gm_group_user;index" json:"group_id"`
	UserID     string     `gorm:"size:32;not null;uniqueIndex:idx_gm_group_user" json:"user_id"`
	UserType   string     `gorm:"size:20;not null;uniqueIndex:idx_gm_group_user" json:"user_type"`
	Role       string     `gorm:"size:10;not null;default:member" json:"role"`
	Nickname   string     `gorm:"size:100" json:"nickname"`
	MutedUntil *time.Time `json:"muted_until"`
	JoinedAt   *time.Time `json:"joined_at"`
	Status     string     `gorm:"size:10;not null;default:active" json:"status"`
}

func (GroupMember) TableName() string { return "im_group_member" }

// GroupJoinRequest 入群申请
type GroupJoinRequest struct {
	ID       string     `gorm:"primaryKey;size:32" json:"id"`
	GroupID  string     `gorm:"size:32;not null;index" json:"group_id"`
	UserID   string     `gorm:"size:32;not null" json:"user_id"`
	UserType string     `gorm:"size:20;not null" json:"user_type"`
	Reason   string     `gorm:"size:255" json:"reason"`
	Status   string     `gorm:"size:10;not null;default:pending" json:"status"` // pending | approved | rejected
	HandledBy string    `gorm:"size:32" json:"handled_by"`
	CreatedAt *time.Time `json:"created_at"`
	UpdatedAt *time.Time `json:"updated_at"`
}

func (GroupJoinRequest) TableName() string { return "im_group_join_request" }

// GroupMessage 群消息
type GroupMessage struct {
	ID         string     `gorm:"primaryKey;size:32" json:"id"`
	GroupID    string     `gorm:"size:32;not null;index:idx_gmsg_group_created,priority:1" json:"group_id"`
	SenderID   string     `gorm:"size:32;not null;index" json:"sender_id"`
	SenderType string     `gorm:"size:20;not null" json:"sender_type"`
	Content    string     `gorm:"type:text" json:"content"`
	Extra      string     `gorm:"type:text" json:"extra"`
	MsgType    string     `gorm:"size:20;not null;default:text" json:"msg_type"`
	ReplyTo    string     `gorm:"size:32;index" json:"reply_to"`
	CreatedAt  *time.Time `gorm:"index:idx_gmsg_group_created,priority:2" json:"created_at"`
}

func (GroupMessage) TableName() string { return "im_group_message" }

// GroupMessageRead 群消息已读
type GroupMessageRead struct {
	ID        string     `gorm:"primaryKey;size:32" json:"id"`
	MessageID string     `gorm:"size:32;not null;uniqueIndex:idx_gmr_msg_user" json:"message_id"`
	GroupID   string     `gorm:"size:32;not null;index" json:"group_id"`
	UserID    string     `gorm:"size:32;not null;uniqueIndex:idx_gmr_msg_user" json:"user_id"`
	UserType  string     `gorm:"size:20;not null;uniqueIndex:idx_gmr_msg_user" json:"user_type"`
	ReadAt    *time.Time `json:"read_at"`
}

func (GroupMessageRead) TableName() string { return "im_group_message_read" }

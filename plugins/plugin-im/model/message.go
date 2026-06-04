package model

import (
	"crypto/sha1"
	"fmt"
	"time"

	"hei-gin/sdk/enums"
)

// Shared message type constants
const (
	MsgTypeText   = "TEXT"
	MsgTypeImage  = "IMAGE"
	MsgTypeFile   = "FILE"
	MsgTypeSystem = "SYSTEM"
)

// MsgExtraImage holds extra metadata for IMAGE messages.
type MsgExtraImage struct {
	Width     int    `json:"w,omitempty"`
	Height    int    `json:"h,omitempty"`
	Format    string `json:"format,omitempty"`
	Thumbnail string `json:"thumbnail,omitempty"`
}

// MsgExtraFile holds extra metadata for FILE messages.
type MsgExtraFile struct {
	Name string `json:"name"`
	Size int64  `json:"size"`
	MIME string `json:"mime"`
}

// MsgExtraSystem holds extra metadata for SYSTEM messages.
type MsgExtraSystem struct {
	Action     string `json:"action"`
	OperatorID string `json:"operator_id,omitempty"`
	UserID     string `json:"user_id,omitempty"`
	UserType   string `json:"user_type,omitempty"`
}

// ClientMessage is the model for client_message table.
type ClientMessage struct {
	ID             string              `gorm:"primaryKey;size:32" json:"id"`
	ConversationID string              `gorm:"size:32;not null;index" json:"conversation_id"`
	Title          string              `gorm:"size:255;not null" json:"title"`
	Content        string              `gorm:"type:text" json:"content"`
	Extra          string              `gorm:"type:text" json:"extra"`
	SenderID       *string             `gorm:"size:32;index" json:"sender_id"`
	SenderType     enums.LoginTypeEnum `gorm:"size:20;index" json:"sender_type"`
	ReceiverID     string              `gorm:"size:32;not null;index" json:"receiver_id"`
	ReceiverType   enums.LoginTypeEnum `gorm:"size:20;index" json:"receiver_type"`
	MessageType    string              `gorm:"size:20;not null;default:text" json:"message_type"`
	Status         string              `gorm:"size:10;not null;index;default:unread" json:"status"`
	ReadAt         *time.Time          `json:"read_at"`
	CreatedAt      *time.Time          `json:"created_at"`
	UpdatedAt      *time.Time          `json:"updated_at"`
}

func (ClientMessage) TableName() string { return "client_message" }

// SysMessage is the model for sys_message table.
type SysMessage struct {
	ID             string              `gorm:"primaryKey;size:32" json:"id"`
	ConversationID string              `gorm:"size:32;not null;index" json:"conversation_id"`
	Title          string              `gorm:"size:255;not null" json:"title"`
	Content        string              `gorm:"type:text" json:"content"`
	Extra          string              `gorm:"type:text" json:"extra"`
	SenderID       *string             `gorm:"size:32;index" json:"sender_id"`
	SenderType     enums.LoginTypeEnum `gorm:"size:20;index" json:"sender_type"`
	ReceiverID     string              `gorm:"size:32;not null;index" json:"receiver_id"`
	ReceiverType   enums.LoginTypeEnum `gorm:"size:20;index" json:"receiver_type"`
	MessageType    string              `gorm:"size:20;not null;default:text" json:"message_type"`
	Status         string              `gorm:"size:10;not null;index;default:unread" json:"status"`
	ReadAt         *time.Time          `json:"read_at"`
	CreatedAt      *time.Time          `json:"created_at"`
	UpdatedAt      *time.Time          `json:"updated_at"`
}

func (SysMessage) TableName() string { return "sys_message" }

// GenerateConversationID generates a deterministic conversation ID from two user identifiers.
func GenerateConversationID(u1ID string, u1Type enums.LoginTypeEnum, u2ID string, u2Type enums.LoginTypeEnum) string {
	key1 := string(u1Type) + ":" + u1ID
	key2 := string(u2Type) + ":" + u2ID
	if key1 > key2 {
		key1, key2 = key2, key1
	}
	h := sha1.Sum([]byte(key1 + "|" + key2))
	return fmt.Sprintf("%x", h[:8])
}

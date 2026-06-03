package message

import (
	"time"

	"hei-gin/core/enums"
)

// ClientMessage represents a consumer (client) site message.
type ClientMessage struct {
	ID           string               `gorm:"primaryKey;size:32" json:"id"`
	Title        string               `gorm:"size:255;not null" json:"title"`
	Content      string               `gorm:"type:text" json:"content"`
	SenderID     *string              `gorm:"size:32" json:"sender_id"`
	SenderType   enums.LoginTypeEnum  `gorm:"size:20;index" json:"sender_type"`
	ReceiverID   string               `gorm:"size:32;not null;index" json:"receiver_id"`
	ReceiverType enums.LoginTypeEnum  `gorm:"size:20;index" json:"receiver_type"`
	MessageType  string               `gorm:"size:20;not null;default:system" json:"message_type"`
	Status       string               `gorm:"size:10;not null;index;default:unread" json:"status"`
	ReadAt       *time.Time           `json:"read_at"`
	CreatedAt    *time.Time           `json:"created_at"`
	UpdatedAt    *time.Time           `json:"updated_at"`
}

func (ClientMessage) TableName() string { return "client_message" }

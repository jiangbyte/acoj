package message

import (
	"crypto/sha1"
	"fmt"
	"time"

	"hei-gin/core/enums"
)

// SysMessage represents a business (admin) site message.
type SysMessage struct {
	ID             string              `gorm:"primaryKey;size:32" json:"id"`
	ConversationID string              `gorm:"size:32;not null;index" json:"conversation_id"`
	Title          string              `gorm:"size:255;not null" json:"title"`
	Content        string              `gorm:"type:text" json:"content"`
	SenderID       *string             `gorm:"size:32;index" json:"sender_id"`
	SenderType     enums.LoginTypeEnum `gorm:"size:20;index" json:"sender_type"`
	ReceiverID     string              `gorm:"size:32;not null;index" json:"receiver_id"`
	ReceiverType   enums.LoginTypeEnum `gorm:"size:20;index" json:"receiver_type"`
	MessageType    string              `gorm:"size:20;not null;default:system" json:"message_type"`
	Status         string              `gorm:"size:10;not null;index;default:unread" json:"status"`
	ReadAt         *time.Time          `json:"read_at"`
	CreatedAt      *time.Time          `json:"created_at"`
	UpdatedAt      *time.Time          `json:"updated_at"`
}

func (SysMessage) TableName() string { return "sys_message" }

// GenerateConversationID creates a deterministic conversation ID from two participants.
// The result is the same regardless of argument order.
func GenerateConversationID(u1ID string, u1Type enums.LoginTypeEnum, u2ID string, u2Type enums.LoginTypeEnum) string {
	key1 := string(u1Type) + ":" + u1ID
	key2 := string(u2Type) + ":" + u2ID
	if key1 > key2 {
		key1, key2 = key2, key1
	}
	h := sha1.Sum([]byte(key1 + "|" + key2))
	return fmt.Sprintf("%x", h[:8])
}

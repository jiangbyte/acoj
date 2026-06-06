package model

import "time"

// ImFile stores files uploaded through the IM module.
type ImFile struct {
	ID           string    `gorm:"primaryKey;size:32" json:"id"`
	ConversationID string  `gorm:"size:32;index" json:"conversation_id"`
	SenderID     string    `gorm:"size:32;not null;index" json:"sender_id"`
	SenderType   string    `gorm:"size:20;not null" json:"sender_type"`
	MsgType      string    `gorm:"size:20;not null" json:"msg_type"` // IMAGE | FILE
	OriginalName string    `gorm:"size:255;not null" json:"original_name"`
	FileURL      string    `gorm:"size:500;not null" json:"file_url"`
	FileSize     int64     `gorm:"default:0" json:"file_size"`
	FileType     string    `gorm:"size:64" json:"file_type"`
	CreatedAt    *time.Time `json:"created_at"`
}

func (ImFile) TableName() string { return "im_file" }

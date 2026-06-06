package model

import "time"

// ImFile stores files uploaded through the IM module.
// Reuses the same storage engine as SysFile but uses a dedicated table.
type ImFile struct {
	ID           string `gorm:"primaryKey;size:32" json:"id"`
	Engine       string `gorm:"size:32;not null" json:"engine"`                 // LOCAL, MINIO, S3
	Bucket       string `gorm:"size:128;not null" json:"bucket"`                // storage bucket
	FileKey      string `gorm:"size:500;not null;index" json:"file_key"`        // unique object key in storage
	Name         string `gorm:"size:255;not null" json:"name"`                  // original filename
	Suffix       string `gorm:"size:32" json:"suffix"`                          // file extension
	SizeKb       int64  `gorm:"default:0" json:"size_kb"`                       // file size in KB
	SizeInfo     string `gorm:"size:32" json:"size_info"`                       // formatted size
	StoragePath  string `gorm:"size:500" json:"storage_path"`                   // path in storage backend
	DownloadPath string `gorm:"size:500" json:"download_path"`                  // HTTP download URL
	Thumbnail    string `gorm:"size:500" json:"thumbnail"`                      // thumbnail URL for images
	Checksum     string `gorm:"size:128" json:"checksum"`                       // SHA256 hex
	ChecksumAlgo string `gorm:"size:16" json:"checksum_algo"`                   // "sha256"

	// IM-specific fields
	ConversationID string `gorm:"size:32;index" json:"conversation_id"`
	SenderID       string `gorm:"size:32;not null;index" json:"sender_id"`
	SenderType     string `gorm:"size:20;not null" json:"sender_type"`
	MsgType        string `gorm:"size:20;not null" json:"msg_type"` // IMAGE | FILE

	CreatedAt time.Time `json:"created_at"`
}

func (ImFile) TableName() string { return "im_file" }

// ImFileVO is the view object for IM file responses.
type ImFileVO struct {
	ID           string `json:"id"`
	Engine       string `json:"engine"`
	Bucket       string `json:"bucket"`
	FileKey      string `json:"file_key"`
	Name         string `json:"name"`
	Suffix       string `json:"suffix"`
	SizeKb       int64  `json:"size_kb"`
	SizeInfo     string `json:"size_info"`
	DownloadPath string `json:"download_path"`
	Thumbnail    string `json:"thumbnail"`

	ConversationID string `json:"conversation_id"`
	SenderID       string `json:"sender_id"`
	SenderType     string `json:"sender_type"`
	MsgType        string `json:"msg_type"`

	CreatedAt string `json:"created_at"`
}

func (e *ImFile) ToVO() *ImFileVO {
	createdAt := ""
	if !e.CreatedAt.IsZero() {
		createdAt = e.CreatedAt.Format("2006-01-02 15:04:05")
	}
	return &ImFileVO{
		ID:             e.ID,
		Engine:         e.Engine,
		Bucket:         e.Bucket,
		FileKey:        e.FileKey,
		Name:           e.Name,
		Suffix:         e.Suffix,
		SizeKb:         e.SizeKb,
		SizeInfo:       e.SizeInfo,
		DownloadPath:   e.DownloadPath,
		Thumbnail:      e.Thumbnail,
		ConversationID: e.ConversationID,
		SenderID:       e.SenderID,
		SenderType:     e.SenderType,
		MsgType:        e.MsgType,
		CreatedAt:      createdAt,
	}
}

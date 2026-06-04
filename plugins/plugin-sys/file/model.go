package file

import "time"

type SysFile struct {
	ID          string    `gorm:"primaryKey;size:32" json:"id"`
	Storage     string    `gorm:"size:32;not null" json:"storage"`
	Category    string    `gorm:"size:64;not null" json:"category"`
	OriginalName string   `gorm:"size:255;not null" json:"original_name"`
	FileName    string    `gorm:"size:255;not null" json:"file_name"`
	FilePath    *string   `gorm:"size:500" json:"file_path"`
	FileURL     *string   `gorm:"size:500" json:"file_url"`
	FileSize    int64     `gorm:"default:0" json:"file_size"`
	FileType    *string   `gorm:"size:64" json:"file_type"`
	FileSuffix  *string   `gorm:"size:32" json:"file_suffix"`
	Checksum     *string   `gorm:"size:128" json:"checksum"`
	ChecksumAlgo  *string   `gorm:"size:16" json:"checksum_algo"`
	Bucket      *string   `gorm:"size:128" json:"bucket"`
	ObjectKey   *string   `gorm:"size:500" json:"object_key"`
	Extra       *string   `gorm:"type:text" json:"extra"`
	CreatedAt   *time.Time `json:"created_at"`
	CreatedBy   *string    `gorm:"size:32" json:"created_by"`
	UpdatedAt   *time.Time `json:"updated_at"`
	UpdatedBy   *string    `gorm:"size:32" json:"updated_by"`
}

func (SysFile) TableName() string { return "sys_file" }

package file

import "time"

// SysFile follows the design from Snowy's DevFile entity.
type SysFile struct {
	ID            string    `gorm:"primaryKey;size:32" json:"id"`
	Engine        string    `gorm:"size:32;not null" json:"engine"`                 // LOCAL, MINIO, S3
	Bucket        string    `gorm:"size:128;not null" json:"bucket"`                // storage bucket
	FileKey       string    `gorm:"size:500;not null;uniqueIndex" json:"file_key"`  // unique object key
	Name          string    `gorm:"size:255;not null" json:"name"`                  // original filename
	Suffix        string    `gorm:"size:32" json:"suffix"`                          // file extension
	SizeKb        int64     `gorm:"default:0" json:"size_kb"`                       // file size in KB
	SizeInfo      string    `gorm:"size:32" json:"size_info"`                       // formatted size
	ObjName       string    `gorm:"size:500" json:"obj_name"`                       // object name (same as FileKey)
	StoragePath   string    `gorm:"size:500" json:"storage_path"`                   // path in storage backend
	DownloadPath  string    `gorm:"size:500" json:"download_path"`                  // HTTP download URL
	IsDownloadAuth bool     `gorm:"default:false" json:"is_download_auth"`          // requires auth to download
	Thumbnail     string    `gorm:"size:500" json:"thumbnail"`                      // thumbnail URL for images
	Checksum      string    `gorm:"size:128" json:"checksum"`                       // SHA256 hex
	ChecksumAlgo  string    `gorm:"size:16" json:"checksum_algo"`                   // "sha256"
	ExtJson       string    `gorm:"type:text" json:"ext_json"`                      // extra metadata JSON
	CreatedAt     time.Time `json:"created_at"`
	CreatedBy     string    `gorm:"size:32" json:"created_by"`
	UpdatedAt     time.Time `json:"updated_at"`
	UpdatedBy     string    `gorm:"size:32" json:"updated_by"`
}

func (SysFile) TableName() string { return "sys_file" }

// FileVO matches snowy's DevFile VO structure.
type FileVO struct {
	ID            string `json:"id"`
	Engine        string `json:"engine"`
	Bucket        string `json:"bucket"`
	FileKey       string `json:"file_key"`
	Name          string `json:"name"`
	Suffix        string `json:"suffix"`
	SizeKb        int64  `json:"size_kb"`
	SizeInfo      string `json:"size_info"`
	ObjName       string `json:"obj_name"`
	StoragePath   string `json:"storage_path"`
	DownloadPath  string `json:"download_path"`
	IsDownloadAuth bool `json:"is_download_auth"`
	Thumbnail     string `json:"thumbnail"`
	Checksum      string `json:"checksum"`
	ChecksumAlgo  string `json:"checksum_algo"`
	ExtJson       string `json:"ext_json"`
	CreatedAt     string `json:"created_at"`
	CreatedBy     string `json:"created_by"`
	UpdatedAt     string `json:"updated_at"`
	UpdatedBy     string `json:"updated_by"`
}

func (e *SysFile) ToVO() *FileVO {
	createdAt := ""
	updatedAt := ""
	if !e.CreatedAt.IsZero() {
		createdAt = e.CreatedAt.Format("2006-01-02 15:04:05")
	}
	if !e.UpdatedAt.IsZero() {
		updatedAt = e.UpdatedAt.Format("2006-01-02 15:04:05")
	}
	return &FileVO{
		ID:            e.ID,
		Engine:        e.Engine,
		Bucket:        e.Bucket,
		FileKey:       e.FileKey,
		Name:          e.Name,
		Suffix:        e.Suffix,
		SizeKb:        e.SizeKb,
		SizeInfo:      e.SizeInfo,
		ObjName:       e.ObjName,
		StoragePath:   e.StoragePath,
		DownloadPath:  e.DownloadPath,
		IsDownloadAuth: e.IsDownloadAuth,
		Thumbnail:     e.Thumbnail,
		Checksum:      e.Checksum,
		ChecksumAlgo:  e.ChecksumAlgo,
		ExtJson:       e.ExtJson,
		CreatedAt:     createdAt,
		CreatedBy:     e.CreatedBy,
		UpdatedAt:     updatedAt,
		UpdatedBy:     e.UpdatedBy,
	}
}

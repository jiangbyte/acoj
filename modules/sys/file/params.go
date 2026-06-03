package file

// FileVO is the view object for file records.
type FileVO struct {
	ID           string  `json:"id"`
	Storage      string  `json:"storage"`
	Category     string  `json:"category"`
	OriginalName string  `json:"original_name"`
	FileName     string  `json:"file_name"`
	FilePath     *string `json:"file_path"`
	FileURL      *string `json:"file_url"`
	FileSize     int64   `json:"file_size"`
	FileType     *string `json:"file_type"`
	FileSuffix   *string `json:"file_suffix"`
	Checksum     *string `json:"checksum"`
	ChecksumAlgo *string `json:"checksum_algo"`
	Bucket       *string `json:"bucket"`
	ObjectKey    *string `json:"object_key"`
	Extra        *string `json:"extra"`
	CreatedAt    string  `json:"created_at"`
	CreatedBy    *string `json:"created_by"`
	UpdatedAt    string  `json:"updated_at"`
	UpdatedBy    *string `json:"updated_by"`
}

// FilePageParam is the query parameter for file pagination.
type FilePageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	Keyword  string `json:"keyword" form:"keyword"`
	Category string `json:"category" form:"category"`
	Storage  string `json:"storage" form:"storage"`
}

// ChunkUploadInitParam is the request to initiate a chunked upload.
type ChunkUploadInitParam struct {
	FileName    string `json:"file_name" form:"file_name" binding:"required"`
	FileSize    int64  `json:"file_size" form:"file_size" binding:"required"`
	TotalChunks int    `json:"total_chunks" form:"total_chunks" binding:"required"`
	Storage     string `json:"storage" form:"storage"`
	Category    string `json:"category" form:"category"`
	Checksum    string `json:"checksum" form:"checksum"`       // optional full-file SHA256
}

// ChunkUploadResult is the response after initiating a chunked upload.
type ChunkUploadResult struct {
	UploadID    string `json:"upload_id"`
	ChunkSize   int64  `json:"chunk_size"`
	TotalChunks int    `json:"total_chunks"`
}

// ChunkUploadParam is the request to upload a single chunk.
type ChunkUploadParam struct {
	UploadID   string `json:"upload_id" form:"upload_id" binding:"required"`
	ChunkIndex int    `json:"chunk_index" form:"chunk_index" binding:"required"`
	Checksum   string `json:"checksum" form:"checksum"` // optional per-chunk SHA256
}

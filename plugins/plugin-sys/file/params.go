package file

type FilePageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword" form:"keyword"`
	Engine  string `json:"engine" form:"engine"`
	Bucket  string `json:"bucket" form:"bucket"`
}

type FileUploadResult struct {
	ID           string `json:"id"`
	Engine       string `json:"engine"`
	Bucket       string `json:"bucket"`
	FileKey      string `json:"file_key"`
	Name string `json:"original_name"`
	Suffix   string `json:"file_suffix"`
	SizeKb   int64  `json:"file_size_kb"`
	SizeInfo     string `json:"size_info"`
	DownloadPath string `json:"download_path"`
	Thumbnail    string `json:"thumbnail"`
}

type ChunkUploadInitParam struct {
	FileName    string `json:"file_name" form:"file_name" binding:"required"`
	FileSize    int64  `json:"file_size" form:"file_size" binding:"required"`
	TotalChunks int    `json:"total_chunks" form:"total_chunks" binding:"required"`
	Engine      string `json:"engine" form:"engine"`
	Bucket      string `json:"bucket" form:"bucket"`
}

type ChunkUploadResult struct {
	UploadID    string `json:"upload_id"`
	FileKey     string `json:"file_key"`
	ChunkSize   int64  `json:"chunk_size"`
	TotalChunks int    `json:"total_chunks"`
}

type ChunkUploadParam struct {
	UploadID    string `json:"upload_id" form:"upload_id" binding:"required"`
	ChunkIndex  int    `json:"chunk_index" form:"chunk_index" binding:"required"`
	TotalChunks int    `json:"total_chunks" form:"total_chunks"`
	Checksum    string `json:"checksum" form:"checksum"`
}

type ChunkCompleteParam struct {
	UploadID     string `json:"upload_id" form:"upload_id" binding:"required"`
	FileKey      string `json:"file_key" form:"file_key" binding:"required"`
	Name string `json:"original_name" form:"original_name" binding:"required"`
	FileSize     int64  `json:"file_size" form:"file_size" binding:"required"`
	Engine       string `json:"engine" form:"engine"`
	Bucket       string `json:"bucket" form:"bucket"`
}

type ChunkAbortParam struct {
	UploadID string `json:"upload_id" form:"upload_id" binding:"required"`
	FileKey  string `json:"file_key" form:"file_key"`
	Engine   string `json:"engine" form:"engine"`
	Bucket   string `json:"bucket" form:"bucket"`
}

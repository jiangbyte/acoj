package file

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
	Bucket       *string `json:"bucket"`
	ObjectKey    *string `json:"object_key"`
	Extra        *string `json:"extra"`
	CreatedAt    string  `json:"created_at"`
	CreatedBy    *string `json:"created_by"`
	UpdatedAt    string  `json:"updated_at"`
	UpdatedBy    *string `json:"updated_by"`
}

type FilePageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	Keyword  string `json:"keyword" form:"keyword"`
	Category string `json:"category" form:"category"`
	Storage  string `json:"storage" form:"storage"`
}

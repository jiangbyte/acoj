package file

type FileVO struct {
	ID             string  `json:"id"`
	Engine         *string `json:"engine"`
	Bucket         *string `json:"bucket"`
	FileKey        *string `json:"file_key"`
	Name           *string `json:"name"`
	Suffix         *string `json:"suffix"`
	SizeKB         *int64  `json:"size_kb"`
	SizeInfo       *string `json:"size_info"`
	ObjName        *string `json:"obj_name"`
	StoragePath    *string `json:"storage_path"`
	DownloadPath   *string `json:"download_path"`
	IsDownloadAuth *int    `json:"is_download_auth"`
	Thumbnail      *string `json:"thumbnail"`
	Extra          *string `json:"extra"`
	CreatedAt      string  `json:"created_at"`
	CreatedBy      *string `json:"created_by"`
}

type FilePageParam struct {
	Current        int    `json:"current" form:"current"`
	Size           int    `json:"size" form:"size"`
	Engine         string `json:"engine" form:"engine"`
	Keyword        string `json:"keyword" form:"keyword"`
	DateRangeStart string `json:"date_range_start" form:"date_range_start"`
	DateRangeEnd   string `json:"date_range_end" form:"date_range_end"`
}

type FileIdParam struct {
	ID string `json:"id"`
}

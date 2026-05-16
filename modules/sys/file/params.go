package file

type FileVO struct {
	ID             string  `json:"id,omitempty"`
	Engine         *string `json:"engine,omitempty"`
	Bucket         *string `json:"bucket,omitempty"`
	FileKey        *string `json:"file_key,omitempty"`
	Name           *string `json:"name,omitempty"`
	Suffix         *string `json:"suffix,omitempty"`
	SizeKB         *int64  `json:"size_kb,omitempty"`
	SizeInfo       *string `json:"size_info,omitempty"`
	ObjName        *string `json:"obj_name,omitempty"`
	StoragePath    *string `json:"storage_path,omitempty"`
	DownloadPath   *string `json:"download_path,omitempty"`
	IsDownloadAuth *int    `json:"is_download_auth,omitempty"`
	Thumbnail      *string `json:"thumbnail,omitempty"`
	Extra          *string `json:"extra,omitempty"`
	CreatedAt      string  `json:"created_at,omitempty"`
	CreatedBy      *string `json:"created_by,omitempty"`
}

type FilePageParam struct {
	Current        int    `json:"current" form:"current"`
	Size           int    `json:"size" form:"size"`
	Engine         string `json:"engine,omitempty" form:"engine"`
	Keyword        string `json:"keyword,omitempty" form:"keyword"`
	DateRangeStart string `json:"date_range_start,omitempty" form:"date_range_start"`
	DateRangeEnd   string `json:"date_range_end,omitempty" form:"date_range_end"`
}

type FileIdParam struct {
	ID string `json:"id"`
}

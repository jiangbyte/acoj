package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

type FileUploadReq struct {
	g.Meta `path:"/api/v1/sys/file/upload" method:"post" summary:"上传文件" tags:"文件管理"`
	Engine string `json:"engine"`
}

type FileUploadRes struct {
	Id           string `json:"id"`
	Name         string `json:"name"`
	Engine       string `json:"engine"`
	DownloadPath string `json:"download_path"`
	SizeInfo     string `json:"size_info"`
}

type FileDownloadReq struct {
	g.Meta `path:"/api/v1/sys/file/download" method:"get" summary:"下载文件" tags:"文件管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type FileDownloadRes struct{}

type FilePageReq struct {
	g.Meta         `path:"/api/v1/sys/file/page" method:"get" summary:"分页查询文件" tags:"文件管理"`
	Keyword        string `json:"keyword"`
	Engine         string `json:"engine"`
	DateRangeStart string `json:"date_range_start"`
	DateRangeEnd   string `json:"date_range_end"`
	utility.PageReq
}

type FilePageRes struct {
	utility.PageRes
}

type FileDetailReq struct {
	g.Meta `path:"/api/v1/sys/file/detail" method:"get" summary:"获取文件详情" tags:"文件管理"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type FileDetailRes struct {
	Id             string `json:"id"`
	Engine         string `json:"engine"`
	Bucket         string `json:"bucket"`
	FileKey        string `json:"file_key"`
	Name           string `json:"name"`
	Suffix         string `json:"suffix"`
	SizeKb         int64  `json:"size_kb"`
	SizeInfo       string `json:"size_info"`
	ObjName        string `json:"obj_name"`
	StoragePath    string `json:"storage_path"`
	DownloadPath   string `json:"download_path"`
	IsDownloadAuth int    `json:"is_download_auth"`
	Thumbnail      string `json:"thumbnail"`
	Extra          string `json:"extra"`
	CreatedAt      string `json:"created_at"`
	CreatedBy      string `json:"created_by"`
	CreatedName    string `json:"created_name"`
	UpdatedAt      string `json:"updated_at"`
	UpdatedBy      string `json:"updated_by"`
	UpdatedName    string `json:"updated_name"`
}

type FileRemoveReq struct {
	g.Meta `path:"/api/v1/sys/file/remove" method:"post" summary:"删除文件" tags:"文件管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type FileRemoveRes struct{}

type FileRemoveAbsoluteReq struct {
	g.Meta `path:"/api/v1/sys/file/remove-absolute" method:"post" summary:"彻底删除文件" tags:"文件管理"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type FileRemoveAbsoluteRes struct{}

// --- Export ---

type FileExportReq struct {
	g.Meta     `path:"/api/v1/sys/file/export" method:"get" summary:"导出文件数据" tags:"文件管理"`
	ExportType string `json:"export_type"`
	SelectedId string `json:"selected_id"`
	Current    int    `json:"current"`
	Size       int    `json:"size"`
}

type FileExportRes struct{}

// --- Template ---

type FileTemplateReq struct {
	g.Meta `path:"/api/v1/sys/file/template" method:"get" summary:"下载文件导入模板" tags:"文件管理"`
}

type FileTemplateRes struct{}

// --- Import ---

type FileImportReq struct {
	g.Meta `path:"/api/v1/sys/file/import" method:"post" summary:"导入文件数据" tags:"文件管理"`
}

type FileImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

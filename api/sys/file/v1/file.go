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
	Id       string `json:"id"`
	FileName string `json:"file_name"`
	FilePath string `json:"file_path"`
	FileSize int64  `json:"file_size"`
	Engine   string `json:"engine"`
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
	Id        string `json:"id"`
	FileName  string `json:"file_name"`
	FilePath  string `json:"file_path"`
	FileSize  int64  `json:"file_size"`
	FileExt   string `json:"file_ext"`
	MimeType  string `json:"mime_type"`
	Engine    string `json:"engine"`
	CreatedAt string `json:"created_at"`
	CreatedBy string `json:"created_by"`
	UpdatedAt string `json:"updated_at"`
	UpdatedBy string `json:"updated_by"`
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

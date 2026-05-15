package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysFile struct {
	gmeta.Meta     `orm:"table:sys_file"`
	Id             string      `json:"id"              description:"主键"`
	Engine         string      `json:"engine"          description:"存储引擎"`
	Bucket         string      `json:"bucket"          description:"存储桶"`
	FileKey        string      `json:"fileKey"         description:"文件Key"`
	Name           string      `json:"name"            description:"文件名称"`
	Suffix         string      `json:"suffix"          description:"文件后缀"`
	SizeKb         int64       `json:"sizeKb"          description:"文件大小kb"`
	SizeInfo       string      `json:"sizeInfo"        description:"文件大小（格式化后）"`
	ObjName        string      `json:"objName"         description:"文件的对象名"`
	StoragePath    string      `json:"storagePath"     description:"文件存储路径"`
	DownloadPath   string      `json:"downloadPath"    description:"文件下载路径"`
	IsDownloadAuth int         `json:"isDownloadAuth"  description:"文件下载是否需要授权"`
	Thumbnail      string      `json:"thumbnail"       description:"图片缩略图"`
	Extra          string      `json:"extra"           description:"扩展信息"`
	CreatedAt      *gtime.Time `json:"createdAt"       description:"创建时间"`
	CreatedBy      string      `json:"createdBy"       description:"创建用户"`
	UpdatedAt      *gtime.Time `json:"updatedAt"       description:"修改时间"`
	UpdatedBy      string      `json:"updatedBy"       description:"修改用户"`
}

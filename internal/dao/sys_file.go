package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysFile = sysFileDao{}

type sysFileDao struct {
	Table   string
	Columns sysFileColumns
}

type sysFileColumns struct {
	Id             string
	Engine         string
	Bucket         string
	FileKey        string
	Name           string
	Suffix         string
	SizeKb         string
	SizeInfo       string
	ObjName        string
	StoragePath    string
	DownloadPath   string
	IsDownloadAuth string
	Thumbnail      string
	Extra          string
	CreatedAt      string
	CreatedBy      string
	UpdatedAt      string
	UpdatedBy      string
}

func init() {
	SysFile.Table = "sys_file"
	SysFile.Columns = sysFileColumns{
		Id:             "id",
		Engine:         "engine",
		Bucket:         "bucket",
		FileKey:        "file_key",
		Name:           "name",
		Suffix:         "suffix",
		SizeKb:         "size_kb",
		SizeInfo:       "size_info",
		ObjName:        "obj_name",
		StoragePath:    "storage_path",
		DownloadPath:   "download_path",
		IsDownloadAuth: "is_download_auth",
		Thumbnail:      "thumbnail",
		Extra:          "extra",
		CreatedAt:      "created_at",
		CreatedBy:      "created_by",
		UpdatedAt:      "updated_at",
		UpdatedBy:      "updated_by",
	}
}

func (d sysFileDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}

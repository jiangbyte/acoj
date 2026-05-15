package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysFile struct {
	gmeta.Meta     `orm:"table:sys_file"`
	Id             interface{} `json:"id"`
	Engine         interface{} `json:"engine"`
	Bucket         interface{} `json:"bucket"`
	FileKey        interface{} `json:"fileKey"`
	Name           interface{} `json:"name"`
	Suffix         interface{} `json:"suffix"`
	SizeKb         interface{} `json:"sizeKb"`
	SizeInfo       interface{} `json:"sizeInfo"`
	ObjName        interface{} `json:"objName"`
	StoragePath    interface{} `json:"storagePath"`
	DownloadPath   interface{} `json:"downloadPath"`
	IsDownloadAuth interface{} `json:"isDownloadAuth"`
	Thumbnail      interface{} `json:"thumbnail"`
	Extra          interface{} `json:"extra"`
	CreatedAt      interface{} `json:"createdAt"`
	CreatedBy      interface{} `json:"createdBy"`
	UpdatedAt      interface{} `json:"updatedAt"`
	UpdatedBy      interface{} `json:"updatedBy"`
}

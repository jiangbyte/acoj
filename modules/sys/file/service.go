package file

import (
	"context"
	"io"
	"os"
	"path/filepath"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

func Page(c *gin.Context, param *FilePageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}
	if param.Size > 100 {
		param.Size = 100
	}

	query := db.DB.WithContext(ctx).Model(&SysFile{})
	if param.Category != "" {
		query = query.Where("category = ?", param.Category)
	}
	if param.Storage != "" {
		query = query.Where("storage = ?", param.Storage)
	}
	if param.Keyword != "" {
		kw := "%" + param.Keyword + "%"
		query = query.Where("original_name LIKE ? OR file_name LIKE ?", kw, kw)
	}

	var total int64
	query.Count(&total)

	var records []SysFile
	query.Order("created_at DESC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)
	return result.PageDataResult(c, records, total, param.Current, param.Size)
}

func Create(c *gin.Context, vo *FileVO) {
	ctx := context.Background()
	now := time.Now()

	entity := SysFile{
		ID: utils.GenerateID(), Storage: vo.Storage, Category: vo.Category,
		OriginalName: vo.OriginalName, FileName: vo.FileName, FileSize: vo.FileSize,
		CreatedAt: &now, UpdatedAt: &now,
	}
	if vo.FilePath != nil {
		entity.FilePath = vo.FilePath
	}
	if vo.FileURL != nil {
		entity.FileURL = vo.FileURL
	}
	if vo.FileType != nil {
		entity.FileType = vo.FileType
	}
	if vo.FileSuffix != nil {
		entity.FileSuffix = vo.FileSuffix
	}
	if vo.Bucket != nil {
		entity.Bucket = vo.Bucket
	}
	if vo.ObjectKey != nil {
		entity.ObjectKey = vo.ObjectKey
	}
	if vo.Extra != nil {
		entity.Extra = vo.Extra
	}

	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("添加文件记录失败: "+err.Error(), 500))
	}
}

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&SysFile{})
}

func Detail(c *gin.Context, id string) *SysFile {
	if id == "" {
		return nil
	}
	ctx := context.Background()
	var entity SysFile
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		return nil
	}
	return &entity
}

func RemoveAbsolute(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()
	var files []SysFile
	db.DB.WithContext(ctx).Where("id IN ?", ids).Find(&files)
	for _, f := range files {
		if f.FilePath != nil && *f.FilePath != "" {
			// Attempt physical deletion, ignore errors
			_ = os.Remove(*f.FilePath)
		}
	}
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&SysFile{})
}

func Upload(c *gin.Context) *FileVO {
	file, header, err := c.Request.FormFile("file")
	if err != nil {
		panic(exception.NewBusinessError("上传文件失败: "+err.Error(), 400))
	}
	defer file.Close()

	storage := c.PostForm("storage")
	if storage == "" {
		storage = "LOCAL"
	}
	category := c.PostForm("category")
	if category == "" {
		category = "DEFAULT"
	}

	now := time.Now()
	ext := filepath.Ext(header.Filename)
	fileName := utils.GenerateID() + ext
	uploadDir := "./uploads/" + now.Format("2006/01/02")
	os.MkdirAll(uploadDir, 0755)
	filePath := filepath.Join(uploadDir, fileName)

	dst, err := os.Create(filePath)
	if err != nil {
		panic(exception.NewBusinessError("保存文件失败: "+err.Error(), 500))
	}
	defer dst.Close()

	if _, err := io.Copy(dst, file); err != nil {
		panic(exception.NewBusinessError("写入文件失败: "+err.Error(), 500))
	}

	entity := SysFile{
		ID:           utils.GenerateID(),
		Storage:      storage,
		Category:     category,
		OriginalName: header.Filename,
		FileName:     fileName,
		FilePath:     &filePath,
		FileSize:     header.Size,
		FileSuffix:   &ext,
		CreatedAt:    &now,
		UpdatedAt:    &now,
	}

	if err := db.DB.WithContext(context.Background()).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("保存文件记录失败: "+err.Error(), 500))
	}

	return &FileVO{
		ID: entity.ID, Storage: entity.Storage, Category: entity.Category,
		OriginalName: entity.OriginalName, FileName: entity.FileName,
		FilePath: entity.FilePath, FileSize: entity.FileSize, FileSuffix: entity.FileSuffix,
	}
}

func Download(c *gin.Context, id string) {
	ctx := context.Background()
	var entity SysFile
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		panic(exception.NewBusinessError("文件不存在", 404))
	}
	if entity.FilePath == nil || *entity.FilePath == "" {
		panic(exception.NewBusinessError("文件路径为空", 404))
	}
	c.File(*entity.FilePath)
}

package file

import (
	"context"
	"fmt"
	"io"
	"net/url"
	"os"
	"path/filepath"
	"strings"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysfile"

	"entgo.io/ent/dialect/sql"
	"github.com/gin-gonic/gin"
)

// Upload handles file upload: parses multipart form, saves file to disk,
// creates a DB record, and returns the upload result.
func Upload(c *gin.Context) gin.H {
	ctx := context.Background()

	file, header, err := c.Request.FormFile("file")
	if err != nil {
		panic(exception.NewBusinessError("获取上传文件失败: "+err.Error(), 400))
	}
	defer file.Close()

	engine := c.Request.FormValue("engine")
	if engine == "" {
		engine = "LOCAL"
	}

	data, err := io.ReadAll(file)
	if err != nil {
		panic(exception.NewBusinessError("读取文件失败: "+err.Error(), 500))
	}

	suffix := strings.ToLower(filepath.Ext(header.Filename))

	fileID := utils.GenerateID()
	storagePath := "uploads/" + fileID + suffix
	sizeKB := int64(len(data) / 1024)
	sizeInfo := formatSize(len(data))
	downloadPath := "/api/v1/sys/file/download?id=" + fileID

	if err := os.MkdirAll("uploads", 0755); err != nil {
		panic(exception.NewBusinessError("创建上传目录失败: "+err.Error(), 500))
	}
	if err := os.WriteFile(storagePath, data, 0644); err != nil {
		panic(exception.NewBusinessError("保存文件失败: "+err.Error(), 500))
	}

	_, err = db.Client.SysFile.Create().
		SetID(fileID).
		SetEngine(engine).
		SetName(header.Filename).
		SetSuffix(suffix).
		SetSizeKB(sizeKB).
		SetSizeInfo(sizeInfo).
		SetStoragePath(storagePath).
		SetDownloadPath(downloadPath).
		SetCreatedAt(time.Now()).
		Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("保存文件记录失败: "+err.Error(), 500))
	}

	return gin.H{
		"id":            fileID,
		"name":          header.Filename,
		"engine":        engine,
		"download_path": downloadPath,
		"size_info":     sizeInfo,
	}
}

// formatSize returns a human-readable file size string.
func formatSize(bytes int) string {
	if bytes < 1024 {
		return fmt.Sprintf("%d B", bytes)
	} else if bytes < 1024*1024 {
		return fmt.Sprintf("%.2f KB", float64(bytes)/1024)
	} else {
		return fmt.Sprintf("%.2f MB", float64(bytes)/(1024*1024))
	}
}

// Download reads the file from storage and returns it with Content-Disposition header.
func Download(c *gin.Context, id string) {
	ctx := context.Background()

	entity, err := db.Client.SysFile.Get(ctx, id)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("文件不存在", 404))
		}
		panic(exception.NewBusinessError("查询文件失败: "+err.Error(), 500))
	}

	data, err := os.ReadFile(*entity.StoragePath)
	if err != nil {
		panic(exception.NewBusinessError("读取文件失败: "+err.Error(), 500))
	}

	filename := url.QueryEscape(*entity.Name)
	c.Header("Content-Disposition", `attachment; filename*=UTF-8''`+filename)
	c.Data(200, "application/octet-stream", data)
}

// Page returns a paginated list of files with optional filters.
func Page(c *gin.Context, param *FilePageParam) gin.H {
	ctx := context.Background()

	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	offset := (param.Current - 1) * param.Size

	query := db.Client.SysFile.Query()

	if param.Engine != "" {
		query = query.Where(sysfile.EngineEQ(param.Engine))
	}
	if param.Keyword != "" {
		query = query.Where(sysfile.NameContains(param.Keyword))
	}
	if param.DateRangeStart != "" {
		t, err := time.Parse("2006-01-02 15:04:05", param.DateRangeStart)
		if err == nil {
			query = query.Where(sysfile.CreatedAtGTE(t))
		}
	}
	if param.DateRangeEnd != "" {
		t, err := time.Parse("2006-01-02 15:04:05", param.DateRangeEnd)
		if err == nil {
			query = query.Where(sysfile.CreatedAtLTE(t))
		}
	}

	total, err := query.Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询文件列表失败: "+err.Error(), 500))
	}

	records, err := query.
		Order(sysfile.ByCreatedAt(sql.OrderDesc())).
		Limit(param.Size).
		Offset(offset).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询文件列表失败: "+err.Error(), 500))
	}

	vos := make([]*FileVO, 0, len(records))
	for _, r := range records {
		vos = append(vos, entToVO(r))
	}

	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

// Detail returns a single file by ID.
func Detail(c *gin.Context, id string) *FileVO {
	if id == "" {
		return nil
	}

	ctx := context.Background()
	entity, err := db.Client.SysFile.Get(ctx, id)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil
		}
		panic(exception.NewBusinessError("查询文件详情失败: "+err.Error(), 500))
	}

	return entToVO(entity)
}

// Remove soft-deletes file records by IDs.
func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}

	ctx := context.Background()
	_, err := db.Client.SysFile.Delete().Where(sysfile.IDIn(ids...)).Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除文件失败: "+err.Error(), 500))
	}
}

// RemoveAbsolute hard-deletes files: removes files from disk and deletes DB records.
func RemoveAbsolute(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}

	ctx := context.Background()

	entities, err := db.Client.SysFile.Query().Where(sysfile.IDIn(ids...)).All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询文件失败: "+err.Error(), 500))
	}

	for _, entity := range entities {
		if entity.StoragePath != nil {
			_ = os.Remove(*entity.StoragePath)
		}
	}

	_, err = db.Client.SysFile.Delete().Where(sysfile.IDIn(ids...)).Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除文件失败: "+err.Error(), 500))
	}
}

// entToVO converts an ent SysFile entity to a FileVO.
func entToVO(entity *ent.SysFile) *FileVO {
	vo := &FileVO{
		ID: entity.ID,
	}

	if entity.Engine != nil {
		vo.Engine = entity.Engine
	}
	if entity.Bucket != nil {
		vo.Bucket = entity.Bucket
	}
	if entity.FileKey != nil {
		vo.FileKey = entity.FileKey
	}
	if entity.Name != nil {
		vo.Name = entity.Name
	}
	if entity.Suffix != nil {
		vo.Suffix = entity.Suffix
	}
	if entity.SizeKB != nil {
		vo.SizeKB = entity.SizeKB
	}
	if entity.SizeInfo != nil {
		vo.SizeInfo = entity.SizeInfo
	}
	if entity.ObjName != nil {
		vo.ObjName = entity.ObjName
	}
	if entity.StoragePath != nil {
		vo.StoragePath = entity.StoragePath
	}
	if entity.DownloadPath != nil {
		vo.DownloadPath = entity.DownloadPath
	}
	if entity.Thumbnail != nil {
		vo.Thumbnail = entity.Thumbnail
	}
	if entity.Extra != nil {
		vo.Extra = entity.Extra
	}
	if entity.CreatedAt != nil {
		vo.CreatedAt = entity.CreatedAt.Format("2006-01-02 15:04:05")
	}
	if entity.CreatedBy != nil {
		vo.CreatedBy = entity.CreatedBy
	}

	// IsDownloadAuth: *bool -> *int (true->1, false->0, nil->nil)
	if entity.IsDownloadAuth != nil {
		v := 0
		if *entity.IsDownloadAuth {
			v = 1
		}
		vo.IsDownloadAuth = &v
	}

	return vo
}

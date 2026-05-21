package file

import (
	"context"
	"fmt"
	"io"
	"log"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"strings"
	"time"

	"hei-gin/config"
	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/storage"
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

	// Limit upload size
	if config.C.App.UploadMaxSize > 0 && header.Size > config.C.App.UploadMaxSize {
		panic(exception.NewBusinessError("上传文件大小超过限制", 400))
	}

	engine := c.Request.FormValue("engine")
	if engine == "" {
		engine = "LOCAL"
	}

	data, err := io.ReadAll(io.LimitReader(file, 100*1024*1024+1))
	if err != nil {
		panic(exception.NewBusinessError("读取文件失败: "+err.Error(), 500))
	}
	if len(data) > 100*1024*1024 {
		panic(exception.NewBusinessError("文件大小超过限制", 400))
	}

	suffix := strings.ToLower(filepath.Ext(header.Filename))

	// MIME type validation
	allowedSuffixes := map[string]bool{
		".jpg": true, ".jpeg": true, ".png": true, ".gif": true, ".bmp": true, ".webp": true,
		".svg": true, ".ico": true,
		".pdf": true, ".doc": true, ".docx": true, ".xls": true, ".xlsx": true,
		".ppt": true, ".pptx": true,
		".txt": true, ".csv": true, ".json": true, ".xml": true, ".yaml": true, ".yml": true,
		".zip": true, ".rar": true, ".7z": true, ".tar": true, ".gz": true,
		".mp4": true, ".avi": true, ".mov": true, ".wmv": true, ".flv": true,
		".mp3": true, ".wav": true, ".wma": true, ".aac": true,
	}
	if !allowedSuffixes[suffix] {
		panic(exception.NewBusinessError("不支持的文件类型: "+suffix, 400))
	}

	// Check actual MIME type from content
	mimeType := http.DetectContentType(data[:min(len(data), 512)])
	disallowedMimes := map[string]bool{
		"application/x-msdownload":    true,
		"application/x-msdos-program": true,
		"application/x-msi":           true,
		"application/x-javascript":    true,
		"text/javascript":             true,
		"application/javascript":      true,
		"application/vnd.php":         true,
		"application/x-httpd-php":     true,
		"text/html":                   true,
		"application/x-sh":            true,
		"application/x-csh":           true,
	}
	if disallowedMimes[mimeType] {
		panic(exception.NewBusinessError("不允许上传可执行文件或脚本", 400))
	}

	fileID := utils.GenerateID()
	sizeKB := int64(len(data) / 1024)
	sizeInfo := formatSize(len(data))
	downloadPath := "/api/v1/sys/file/download?id=" + fileID

	storer := getStorage(engine)
	storagePath, err := storer.Store("default", fileID+suffix, data)
	if err != nil {
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

	// Path traversal protection
	cleanPath, err := filepath.Abs(*entity.StoragePath)
	if err != nil {
		panic(exception.NewBusinessError("文件路径错误", 500))
	}
	uploadsAbs, err := filepath.Abs("uploads")
	if err != nil {
		panic(exception.NewBusinessError("系统错误", 500))
	}
	if !strings.HasPrefix(cleanPath, uploadsAbs) {
		panic(exception.NewBusinessError("文件路径不合法", 403))
	}

	data, err := os.ReadFile(cleanPath)
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
	if param.Size > 100 {
		param.Size = 100
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
		} else {
			log.Printf("[FILE] Invalid date range start: %s, error: %v", param.DateRangeStart, err)
		}
	}
	if param.DateRangeEnd != "" {
		t, err := time.Parse("2006-01-02 15:04:05", param.DateRangeEnd)
		if err == nil {
			query = query.Where(sysfile.CreatedAtLTE(t))
		} else {
			log.Printf("[FILE] Invalid date range end: %s, error: %v", param.DateRangeEnd, err)
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

// RemoveAbsolute hard-deletes files: removes DB records first, then deletes disk files.
func RemoveAbsolute(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}

	ctx := context.Background()

	entities, err := db.Client.SysFile.Query().Where(sysfile.IDIn(ids...)).All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询文件失败: "+err.Error(), 500))
	}

	// Delete DB records first
	_, err = db.Client.SysFile.Delete().Where(sysfile.IDIn(ids...)).Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除文件失败: "+err.Error(), 500))
	}

	// Then delete disk files (best-effort)
	for _, entity := range entities {
		if entity.StoragePath != nil {
			if err := os.Remove(*entity.StoragePath); err != nil {
				log.Printf("[FILE] Failed to remove file %s: %v", *entity.StoragePath, err)
			}
		}
	}
}

// getStorage returns the appropriate storage backend based on the engine name.
func getStorage(engine string) storage.FileStorage {
	switch engine {
	case "LOCAL":
		return storage.NewLocalStorage("uploads")
	case "MINIO":
		// Requires MinIO config to be added to Config struct
		log.Printf("[FILE] MINIO engine selected but not fully configured, falling back to LOCAL")
		return storage.NewLocalStorage("uploads")
	default:
		log.Printf("[FILE] Unknown storage engine: %s, falling back to LOCAL", engine)
		return storage.NewLocalStorage("uploads")
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

package file

import (
	"bytes"
	"context"
	"encoding/base64"
	"fmt"
	"image"
	_ "image/gif" // register GIF decoder
	"image/jpeg"
	_ "image/jpeg" // register JPEG decoder
	_ "image/png"  // register PNG decoder
	"io"
	"math"
	"os"
	"path/filepath"
	"runtime"
	"strings"
	"time"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
	"github.com/gogf/gf/v2/os/gfile"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func init() {
	auth.RegisterPermission("sys:file:upload", "sys/file", "BACKEND", "文件上传")
	auth.RegisterPermission("sys:file:download", "sys/file", "BACKEND", "文件下载")
	auth.RegisterPermission("sys:file:page", "sys/file", "BACKEND", "文件查询")
	auth.RegisterPermission("sys:file:detail", "sys/file", "BACKEND", "文件详情")
	auth.RegisterPermission("sys:file:remove", "sys/file", "BACKEND", "文件删除")
	auth.RegisterPermission("sys:file:export", "sys/file", "BACKEND", "文件导出")
	auth.RegisterPermission("sys:file:template", "sys/file", "BACKEND", "文件导入模板")
	auth.RegisterPermission("sys:file:import", "sys/file", "BACKEND", "文件导入")
}

const (
	EngineLocal   = "LOCAL"
	EngineMinio   = "MINIO"
	EngineS3      = "S3"
	EngineAliyun  = "ALIYUN"
	EngineTencent = "TENCENT"
)

// imageExtensions lists image file extensions that support thumbnail generation.
var imageExtensions = map[string]bool{
	".jpg":  true,
	".jpeg": true,
	".png":  true,
	".gif":  true,
}

// ---------------------------------------------------------------------------
// Helpers: config reading
// ---------------------------------------------------------------------------

// getConfigValue reads a single config value from sys_config table.
func getConfigValue(ctx context.Context, key string) (string, error) {
	row, err := dao.SysConfig.Ctx().Ctx(ctx).Where("config_key", key).One()
	if err != nil {
		return "", err
	}
	if row == nil {
		return "", nil
	}
	return row["config_value"].String(), nil
}

// getLoginId extracts the current user's login ID from context.
func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}

// getEngine determines the storage engine to use.
// If an engine is explicitly provided, it is used (upper-cased).
// Otherwise, the default engine is read from sys_config (SYS_DEFAULT_FILE_ENGINE).
// Falls back to LOCAL.
func getEngine(ctx context.Context, engine string) string {
	if engine != "" {
		return strings.ToUpper(engine)
	}
	defaultEngine, err := getConfigValue(ctx, "SYS_DEFAULT_FILE_ENGINE")
	if err != nil || defaultEngine == "" {
		return EngineLocal
	}
	return defaultEngine
}

// createStorage creates a StorageInterface instance for the given engine,
// reading the required config from sys_config.
func createStorage(ctx context.Context, engine string) (StorageInterface, error) {
	switch engine {
	case EngineLocal:
		folder, err := getLocalFolder(ctx)
		if err != nil {
			return nil, err
		}
		return NewLocalStorage(folder), nil

	case EngineMinio:
		return createS3Storage(ctx, "SYS_FILE_MINIO_", engine, true)

	case EngineS3:
		return createS3Storage(ctx, "SYS_FILE_S3_", engine, true)

	case EngineAliyun:
		// Aliyun OSS uses S3-compatible API with virtual-hosted-style addressing
		return createS3Storage(ctx, "SYS_FILE_ALIYUN_", engine, false)

	case EngineTencent:
		// Tencent COS uses S3-compatible API with virtual-hosted-style addressing
		return createS3Storage(ctx, "SYS_FILE_TENCENT_", engine, false)

	default:
		return nil, fmt.Errorf("不支持的存储引擎: %s", engine)
	}
}

func getLocalFolder(ctx context.Context) (string, error) {
	isWindows := runtime.GOOS == "windows"
	key := "SYS_FILE_LOCAL_FOLDER_FOR_UNIX"
	if isWindows {
		key = "SYS_FILE_LOCAL_FOLDER_FOR_WINDOWS"
	}
	folder, err := getConfigValue(ctx, key)
	if err != nil {
		return "", err
	}
	if folder == "" {
		if isWindows {
			folder = "D:/hei-file-upload"
		} else {
			folder = "/data/hei-file-upload"
		}
	}
	return folder, nil
}

func createS3Storage(ctx context.Context, prefix, engine string, pathStyle bool) (StorageInterface, error) {
	var endpoint, accessKey, secretKey, bucket, region string
	var err error

	endpoint, _ = getConfigValue(ctx, prefix+"END_POINT")

	if engine == EngineAliyun {
		accessKey, _ = getConfigValue(ctx, prefix+"ACCESS_KEY_ID")
		secretKey, _ = getConfigValue(ctx, prefix+"ACCESS_KEY_SECRET")
	} else {
		accessKey, _ = getConfigValue(ctx, prefix+"ACCESS_KEY")
		secretKey, _ = getConfigValue(ctx, prefix+"SECRET_KEY")
	}

	bucket, _ = getConfigValue(ctx, prefix+"DEFAULT_BUCKET_NAME")
	if bucket == "" {
		bucket = "hei-files"
	}

	if engine == EngineS3 {
		region, _ = getConfigValue(ctx, prefix+"REGION")
	}
	if region == "" {
		region = "us-east-1"
	}

	storage, err := NewS3Storage(endpoint, accessKey, secretKey, bucket, region, pathStyle)
	if err != nil {
		return nil, fmt.Errorf("无法创建%s存储引擎: %w", engine, err)
	}
	return storage, nil
}

// ---------------------------------------------------------------------------
// Helpers: file utilities
// ---------------------------------------------------------------------------

// generateFileKey creates a date-partitioned file key: YYYY/MM/DD/{id}{suffix}.
func generateFileKey(fileID, suffix string) string {
	now := time.Now()
	return fmt.Sprintf("%d/%02d/%02d/%s%s", now.Year(), now.Month(), now.Day(), fileID, suffix)
}

// formatSize formats a byte size into a human-readable string (e.g., "1.5MB").
func formatSize(sizeBytes int64) string {
	switch {
	case sizeBytes < 1024:
		return fmt.Sprintf("%dB", sizeBytes)
	case sizeBytes < 1024*1024:
		return fmt.Sprintf("%.1fKB", float64(sizeBytes)/1024)
	case sizeBytes < 1024*1024*1024:
		return fmt.Sprintf("%.1fMB", float64(sizeBytes)/1024/1024)
	default:
		return fmt.Sprintf("%.1fGB", float64(sizeBytes)/1024/1024/1024)
	}
}

// generateThumbnail creates a base64-encoded JPEG thumbnail (max 300x300) for image files.
// Returns empty string if the file is not an image or thumbnail generation fails.
func generateThumbnail(data []byte, suffix string) string {
	suffix = strings.ToLower(suffix)
	if !imageExtensions[suffix] {
		return ""
	}

	img, _, err := image.Decode(bytes.NewReader(data))
	if err != nil {
		return ""
	}

	thumb := resizeImage(img, 300, 300)
	buf := new(bytes.Buffer)
	if err := jpeg.Encode(buf, thumb, &jpeg.Options{Quality: 80}); err != nil {
		return ""
	}

	return "data:image/jpeg;base64," + base64.StdEncoding.EncodeToString(buf.Bytes())
}

// resizeImage resizes an image to fit within maxW x maxH while preserving aspect ratio,
// using nearest-neighbor interpolation.
func resizeImage(img image.Image, maxW, maxH int) image.Image {
	bounds := img.Bounds()
	w := bounds.Dx()
	h := bounds.Dy()

	if w <= maxW && h <= maxH {
		return img
	}

	ratio := math.Min(float64(maxW)/float64(w), float64(maxH)/float64(h))
	newW := int(float64(w) * ratio)
	if newW < 1 {
		newW = 1
	}
	newH := int(float64(h) * ratio)
	if newH < 1 {
		newH = 1
	}

	dst := image.NewRGBA(image.Rect(0, 0, newW, newH))
	for y := 0; y < newH; y++ {
		for x := 0; x < newW; x++ {
			sx := x * w / newW
			sy := y * h / newH
			dst.Set(x, y, img.At(sx, sy))
		}
	}
	return dst
}

// enrichNames fills in created_name and updated_name from the sys_user table.
func enrichNames(ctx context.Context, item g.Map) {
	if id, ok := item["created_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["created_name"] = row["nickname"].String()
		}
	}
	if id, ok := item["updated_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["updated_name"] = row["nickname"].String()
		}
	}
}

// batchEnrichNames enriches creator/updater names for a list of records.
func batchEnrichNames(ctx context.Context, list []g.Map) {
	for _, item := range list {
		enrichNames(ctx, item)
	}
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

// Upload handles file upload, supporting multiple storage engines.
func Upload(ctx context.Context, file ghttp.UploadFile, engine string) (g.Map, error) {
	loginId := getLoginId(ctx)

	// Determine engine from param or config
	engine = getEngine(ctx, engine)

	// Read file data
	f, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}
	defer f.Close()

	data, err := io.ReadAll(f)
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}

	sizeBytes := len(data)
	suffix := strings.ToLower(filepath.Ext(file.Filename))
	fileID := utility.GenerateID()
	objName := fileID + suffix
	fileKey := generateFileKey(fileID, suffix)

	// Get storage backend
	storage, err := createStorage(ctx, engine)
	if err != nil {
		return nil, err
	}
	bucket := storage.GetDefaultBucket()

	// Store the file in the storage backend
	if err := storage.Store(ctx, bucket, fileKey, data); err != nil {
		return nil, fmt.Errorf("文件存储失败: %w", err)
	}

	// Generate thumbnail for images
	thumbnail := ""
	if imageExtensions[suffix] {
		thumbnail = generateThumbnail(data, suffix)
	}

	// Build download path (via the application's download endpoint)
	baseURL := getBaseURL(ctx)
	downloadPath := fmt.Sprintf("%s/api/v1/sys/file/download?id=%s", baseURL, fileID)

	// Insert DB record
	_, err = dao.SysFile.Ctx().Ctx(ctx).Insert(g.Map{
		"id":               fileID,
		"engine":           engine,
		"bucket":           bucket,
		"file_key":         fileKey,
		"name":             file.Filename,
		"suffix":           suffix,
		"size_kb":          int64(sizeBytes / 1024),
		"size_info":        formatSize(int64(sizeBytes)),
		"obj_name":         objName,
		"storage_path":     storage.GetURL(bucket, fileKey),
		"download_path":    downloadPath,
		"is_download_auth": 0,
		"thumbnail":        thumbnail,
		"created_by":       loginId,
	})
	if err != nil {
		return nil, err
	}

	return g.Map{
		"id":            fileID,
		"name":          file.Filename,
		"engine":        engine,
		"download_path": downloadPath,
		"size_info":     formatSize(int64(sizeBytes)),
	}, nil
}

// getBaseURL extracts the base URL from the current request for building download links.
func getBaseURL(ctx context.Context) string {
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return "http://localhost:8080"
	}
	scheme := "http"
	if r.TLS != nil {
		scheme = "https"
	}
	return fmt.Sprintf("%s://%s", scheme, r.Host)
}

// Download reads a file from the storage backend and returns its bytes and name.
func Download(ctx context.Context, id string) ([]byte, string, error) {
	row, err := dao.SysFile.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil {
		return nil, "", err
	}
	if row == nil {
		return nil, "", nil
	}

	engine := row["engine"].String()
	bucket := row["bucket"].String()
	fileKey := row["file_key"].String()
	name := row["name"].String()

	// For old records where storage engine fields may be empty,
	// fall back to local file path serving.
	if engine == "" || bucket == "" || fileKey == "" {
		storagePath := row["storage_path"].String()
		if storagePath == "" {
			return nil, "", os.ErrNotExist
		}
		absPath := gfile.Join(gfile.Pwd(), storagePath)
		data, err := os.ReadFile(absPath)
		if err != nil {
			return nil, "", err
		}
		return data, name, nil
	}

	storage, err := createStorage(ctx, engine)
	if err != nil {
		return nil, "", err
	}

	data, err := storage.GetBytes(ctx, bucket, fileKey)
	if err != nil {
		return nil, "", fmt.Errorf("读取文件失败: %w", err)
	}

	return data, name, nil
}

// Page returns a paginated list of file records.
func Page(ctx context.Context, keyword, engine, dateRangeStart, dateRangeEnd string, current, size int) (*utility.PageRes, error) {
	m := dao.SysFile.Ctx().Ctx(ctx).OrderDesc("created_at")
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("name LIKE ?", kw)
	}
	if engine != "" {
		m = m.Where("engine", engine)
	}
	if dateRangeStart != "" {
		m = m.Where("created_at >= ?", dateRangeStart)
	}
	if dateRangeEnd != "" {
		m = m.Where("created_at <= ?", dateRangeEnd)
	}

	count, err := m.Count()
	if err != nil {
		return nil, err
	}
	all, err := m.Page(current, size).All()
	if err != nil {
		return nil, err
	}
	list := all.List()

	// Enrich creator/updater names
	batchEnrichNames(ctx, list)

	return utility.NewPageRes(list, count, current, size), nil
}

// Detail returns a single file record with enriched names.
func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysFile.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}

	result := g.Map{
		"id":               row["id"].String(),
		"engine":           row["engine"].String(),
		"bucket":           row["bucket"].String(),
		"file_key":         row["file_key"].String(),
		"name":             row["name"].String(),
		"suffix":           row["suffix"].String(),
		"size_kb":          row["size_kb"].Int64(),
		"size_info":        row["size_info"].String(),
		"obj_name":         row["obj_name"].String(),
		"storage_path":     row["storage_path"].String(),
		"download_path":    row["download_path"].String(),
		"is_download_auth": row["is_download_auth"].Int(),
		"thumbnail":        row["thumbnail"].String(),
		"extra":            row["extra"].String(),
		"created_at":       row["created_at"].String(),
		"created_by":       row["created_by"].String(),
		"updated_at":       row["updated_at"].String(),
		"updated_by":       row["updated_by"].String(),
	}

	enrichNames(ctx, result)
	return result, nil
}

// Remove deletes file records from the database (soft delete).
func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysFile.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

// RemoveAbsolute deletes files from both the storage backend and the database.
func RemoveAbsolute(ctx context.Context, ids []string) error {
	rows, err := dao.SysFile.Ctx().Ctx(ctx).WherePri(ids).All()
	if err != nil {
		return err
	}

	for _, row := range rows {
		engine := row["engine"].String()
		bucket := row["bucket"].String()
		fileKey := row["file_key"].String()

		// Try to delete from storage backend
		if engine != "" && bucket != "" && fileKey != "" {
			storage, err := createStorage(ctx, engine)
			if err == nil {
				_ = storage.Delete(ctx, bucket, fileKey)
			}
		} else {
			// Fall back to local file deletion for old records
			storagePath := row["storage_path"].String()
			if storagePath != "" {
				absPath := gfile.Join(gfile.Pwd(), storagePath)
				if gfile.Exists(absPath) {
					_ = gfile.Remove(absPath)
				}
			}
		}
	}

	// Delete from database
	_, err = dao.SysFile.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

// ---------------------------------------------------------------------------
// Export / Import / Template
// ---------------------------------------------------------------------------

// Export exports file data as an Excel file.
func Export(ctx context.Context, exportType string, selectedIds []string, current, size int) (*bytes.Buffer, error) {
	var records []g.Map

	switch exportType {
	case "current":
		pageSize := size
		if pageSize <= 0 {
			pageSize = 10
		}
		pageCurrent := current
		if pageCurrent <= 0 {
			pageCurrent = 1
		}
		m := dao.SysFile.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysFile.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default: // "all"
		m := dao.SysFile.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}

	return utility.CreateExcelFromData(data, "文件数据")
}

// DownloadTemplate downloads an import template Excel file.
func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"engine", "name", "suffix", "size_kb", "size_info", "obj_name", "storage_path", "download_path"}
	return utility.CreateExcelTemplate(headers, "文件数据")
}

// Import imports file data from an uploaded Excel file.
func Import(ctx context.Context, file ghttp.UploadFile) (g.Map, error) {
	f, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}
	defer f.Close()

	content, err := io.ReadAll(f)
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}

	rows, err := utility.ParseExcelFromBytes(content, true)
	if err != nil {
		return nil, err
	}

	if len(rows) == 0 {
		return nil, fmt.Errorf("导入数据不能为空")
	}

	imported := 0
	for _, row := range rows {
		id := utility.GenerateID()
		_, err := dao.SysFile.Ctx().Ctx(ctx).Insert(g.Map{
			"id":            id,
			"engine":        row["engine"],
			"bucket":        row["bucket"],
			"file_key":      row["file_key"],
			"name":          row["name"],
			"suffix":        row["suffix"],
			"size_kb":       row["size_kb"],
			"size_info":     row["size_info"],
			"obj_name":      row["obj_name"],
			"storage_path":  row["storage_path"],
			"download_path": row["download_path"],
			"created_by":    getLoginId(ctx),
		})
		if err == nil {
			imported++
		}
	}

	return g.Map{
		"total":   imported,
		"message": fmt.Sprintf("成功导入%d条数据", imported),
	}, nil
}

// cleanMapForExport removes nil values and converts types for Excel export.
func cleanMapForExport(m g.Map) map[string]interface{} {
	result := make(map[string]interface{}, len(m))
	for k, v := range m {
		if v == nil {
			result[k] = ""
		} else {
			result[k] = v
		}
	}
	delete(result, "id")
	return result
}

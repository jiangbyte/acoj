package file

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"net/http"
	"path/filepath"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/storage"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

// computeSHA256 computes the SHA-256 hex digest of a reader.
// The reader is consumed; caller must reset if needed.
func computeSHA256(r io.Reader) (string, error) {
	h := sha256.New()
	if _, err := io.Copy(h, r); err != nil {
		return "", err
	}
	return hex.EncodeToString(h.Sum(nil)), nil
}

// ===== Basic CRUD =====

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
	if vo.Checksum != nil {
		entity.Checksum = vo.Checksum
		algo := "sha256"
		entity.ChecksumAlgo = &algo
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

// ===== Storage backend =====

func RemoveAbsolute(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()
	var files []SysFile
	db.DB.WithContext(ctx).Where("id IN ?", ids).Find(&files)
	for _, f := range files {
		s := storage.GetStorage(f.Storage)
		if s == nil {
			continue
		}
		if f.Bucket != nil && f.ObjectKey != nil {
			_ = s.Delete(*f.Bucket, *f.ObjectKey)
		}
	}
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&SysFile{})
}

// ===== Single-file Upload =====

func Upload(c *gin.Context) *FileVO {
	file, header, err := c.Request.FormFile("file")
	if err != nil {
		panic(exception.NewBusinessError("上传文件失败: "+err.Error(), 400))
	}
	defer file.Close()

	storageType := c.PostForm("storage")
	if storageType == "" {
		storageType = "LOCAL"
	}
	category := c.PostForm("category")
	if category == "" {
		category = "DEFAULT"
	}
	expectedChecksum := c.PostForm("checksum") // optional SHA256 from client

	now := time.Now()
	ext := filepath.Ext(header.Filename)
	fileName := utils.GenerateID() + ext

	s := storage.GetStorage(storageType)
	if s == nil {
		panic(exception.NewBusinessError("不支持的存储类型: "+storageType, 400))
	}

	// Store the file
	bucket := category
	fileKey := fileName

	// Read into buffer to compute checksum and store simultaneously.
	// For large files, this uses memory — the chunked upload API is the
	// recommended path for large files.
	data, err := io.ReadAll(file)
	if err != nil {
		panic(exception.NewBusinessError("读取文件失败: "+err.Error(), 500))
	}

	// Compute checksum before storing
	fileChecksum, err := computeSHA256(byteReader(data))
	if err != nil {
		panic(exception.NewBusinessError("计算文件校验和失败: "+err.Error(), 500))
	}

	// Verify checksum if client provided one
	if expectedChecksum != "" && expectedChecksum != fileChecksum {
		panic(exception.NewBusinessError(fmt.Sprintf("文件校验和不匹配: 期望 %s, 实际 %s", expectedChecksum, fileChecksum), 400))
	}

	filePath, err := s.Store(bucket, fileKey, data)
	if err != nil {
		panic(exception.NewBusinessError("保存文件失败: "+err.Error(), 500))
	}

	algo := "sha256"
	entity := SysFile{
		ID:           utils.GenerateID(),
		Storage:      storageType,
		Category:     category,
		OriginalName: header.Filename,
		FileName:     fileName,
		FilePath:     &filePath,
		FileSize:     header.Size,
		FileSuffix:   &ext,
		Checksum:     &fileChecksum,
		ChecksumAlgo: &algo,
		Bucket:       &bucket,
		ObjectKey:    &fileKey,
		CreatedAt:    &now,
		UpdatedAt:    &now,
	}

	if err := db.DB.WithContext(context.Background()).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("保存文件记录失败: "+err.Error(), 500))
	}

	return &FileVO{
		ID: entity.ID, Storage: entity.Storage, Category: entity.Category,
		OriginalName: entity.OriginalName, FileName: entity.FileName,
		FilePath: entity.FilePath, FileSize: entity.FileSize,
		FileSuffix: entity.FileSuffix, Checksum: entity.Checksum,
		ChecksumAlgo: entity.ChecksumAlgo,
		Bucket: entity.Bucket, ObjectKey: entity.ObjectKey,
	}
}

// ===== Chunked Upload =====

func InitChunkUpload(c *gin.Context, param *ChunkUploadInitParam) *ChunkUploadResult {
	storageType := param.Storage
	if storageType == "" {
		storageType = "LOCAL"
	}
	category := param.Category
	if category == "" {
		category = "DEFAULT"
	}

	s := storage.GetStorage(storageType)
	if s == nil {
		panic(exception.NewBusinessError("不支持的存储类型: "+storageType, 400))
	}

	cu, ok := s.(storage.ChunkedUploader)
	if !ok {
		panic(exception.NewBusinessError("存储后端不支持分片上传: "+storageType, 400))
	}

	ext := filepath.Ext(param.FileName)
	fileKey := utils.GenerateID() + ext
	bucket := category

	uploadID, err := cu.InitChunkUpload(bucket, fileKey, param.TotalChunks)
	if err != nil {
		panic(exception.NewBusinessError("初始化分片上传失败: "+err.Error(), 500))
	}

	// Calculate per-chunk size
	chunkSize := param.FileSize / int64(param.TotalChunks)
	if param.FileSize%int64(param.TotalChunks) != 0 {
		chunkSize++
	}

	// Store metadata in context for later completion
	c.Set("_chunk_bucket", bucket)
	c.Set("_chunk_fileKey", fileKey)
	c.Set("_chunk_storage", storageType)
	c.Set("_chunk_category", category)
	c.Set("_chunk_originalName", param.FileName)
	c.Set("_chunk_fileSize", param.FileSize)
	c.Set("_chunk_expectedChecksum", param.Checksum)
	c.Set("_chunk_uploadID", uploadID)

	return &ChunkUploadResult{
		UploadID:    uploadID,
		ChunkSize:   chunkSize,
		TotalChunks: param.TotalChunks,
	}
}

func UploadChunk(c *gin.Context, param *ChunkUploadParam) {
	uploadID := param.UploadID
	if uploadID == "" {
		panic(exception.NewBusinessError("upload_id 不能为空", 400))
	}

	// Determine storage from the init context
	storageType, _ := c.Get("_chunk_storage")
	if storageType == nil {
		storageType = "LOCAL"
	}
	bucket, _ := c.Get("_chunk_bucket")
	if bucket == nil {
		bucket = "DEFAULT"
	}
	fileKey, _ := c.Get("_chunk_fileKey")
	if fileKey == nil {
		fileKey = "unknown"
	}

	s := storage.GetStorage(storageType.(string))
	if s == nil {
		panic(exception.NewBusinessError("不支持的存储类型", 400))
	}

	cu, ok := s.(storage.ChunkedUploader)
	if !ok {
		panic(exception.NewBusinessError("存储后端不支持分片上传", 400))
	}

	file, _, err := c.Request.FormFile("chunk")
	if err != nil {
		panic(exception.NewBusinessError("读取分片失败: "+err.Error(), 400))
	}
	defer file.Close()

	chunk := storage.ChunkInfo{
		UploadID:   uploadID,
		ChunkIndex: param.ChunkIndex,
		Checksum:   param.Checksum,
		Data:       file,
	}

	if err := cu.UploadChunk(bucket.(string), fileKey.(string), uploadID, chunk); err != nil {
		panic(exception.NewBusinessError("上传分片失败: "+err.Error(), 500))
	}
}

func CompleteChunkUpload(c *gin.Context) {
	uploadID, _ := c.Get("_chunk_uploadID")
	if uploadID == nil {
		uploadID = c.PostForm("upload_id")
	}
	uploadIDStr, ok := uploadID.(string)
	if !ok || uploadIDStr == "" {
		panic(exception.NewBusinessError("upload_id 不能为空", 400))
	}

	storageType, _ := c.Get("_chunk_storage")
	if storageType == nil {
		storageType = "LOCAL"
	}
	storageTypeStr := storageType.(string)

	bucket, _ := c.Get("_chunk_bucket")
	bucketStr, _ := bucket.(string)
	fileKey, _ := c.Get("_chunk_fileKey")
	fileKeyStr, _ := fileKey.(string)
	category, _ := c.Get("_chunk_category")
	categoryStr, _ := category.(string)
	originalName, _ := c.Get("_chunk_originalName")
	originalNameStr, _ := originalName.(string)
	fileSize, _ := c.Get("_chunk_fileSize")
	fileSizeInt, _ := fileSize.(int64)
	expectedChecksum, _ := c.Get("_chunk_expectedChecksum")
	expectedChecksumStr, _ := expectedChecksum.(string)

	s := storage.GetStorage(storageTypeStr)
	if s == nil {
		panic(exception.NewBusinessError("不支持的存储类型", 400))
	}

	cu, ok := s.(storage.ChunkedUploader)
	if !ok {
		panic(exception.NewBusinessError("存储后端不支持分片上传", 400))
	}

	filePath, err := cu.CompleteChunkUpload(bucketStr, fileKeyStr, uploadIDStr)
	if err != nil {
		panic(exception.NewBusinessError("合并分片文件失败: "+err.Error(), 500))
	}

	// Verify checksum if requested
	if expectedChecksumStr != "" {
		data, err := s.GetBytes(bucketStr, fileKeyStr)
		if err == nil {
			actualChecksum, _ := computeSHA256(byteReader(data))
			if actualChecksum != expectedChecksumStr {
				panic(exception.NewBusinessError(fmt.Sprintf("文件校验和不匹配: 期望 %s, 实际 %s", expectedChecksumStr, actualChecksum), 400))
			}
		}
	}

	// Compute checksum for storage
	now := time.Now()
	ext := filepath.Ext(originalNameStr)
	algo := "sha256"

	var checksumPtr *string
	data, err := s.GetBytes(bucketStr, fileKeyStr)
	if err == nil {
		cs, _ := computeSHA256(byteReader(data))
		checksumPtr = &cs
	}

	entity := SysFile{
		ID:           utils.GenerateID(),
		Storage:      storageTypeStr,
		Category:     categoryStr,
		OriginalName: originalNameStr,
		FileName:     fileKeyStr,
		FilePath:     &filePath,
		FileSize:     fileSizeInt,
		FileSuffix:   &ext,
		Checksum:     checksumPtr,
		ChecksumAlgo: &algo,
		Bucket:       &bucketStr,
		ObjectKey:    &fileKeyStr,
		CreatedAt:    &now,
		UpdatedAt:    &now,
	}

	if err := db.DB.WithContext(context.Background()).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("保存文件记录失败: "+err.Error(), 500))
	}

	// Return FileVO
	c.JSON(200, result.Success(c, &FileVO{
		ID: entity.ID, Storage: entity.Storage, Category: entity.Category,
		OriginalName: entity.OriginalName, FileName: entity.FileName,
		FilePath: entity.FilePath, FileSize: entity.FileSize,
		FileSuffix: entity.FileSuffix, Checksum: entity.Checksum,
		ChecksumAlgo: entity.ChecksumAlgo,
		Bucket: entity.Bucket, ObjectKey: entity.ObjectKey,
	}))
}

func AbortChunkUpload(c *gin.Context) {
	uploadID := c.PostForm("upload_id")
	if uploadID == "" {
		panic(exception.NewBusinessError("upload_id 不能为空", 400))
	}

	storageType := c.PostForm("storage")
	if storageType == "" {
		storageType = "LOCAL"
	}
	bucket := c.PostForm("bucket")
	fileKey := c.PostForm("file_key")

	s := storage.GetStorage(storageType)
	if s == nil {
		panic(exception.NewBusinessError("不支持的存储类型", 400))
	}

	cu, ok := s.(storage.ChunkedUploader)
	if !ok {
		panic(exception.NewBusinessError("存储后端不支持分片上传", 400))
	}

	if err := cu.AbortChunkUpload(bucket, fileKey, uploadID); err != nil {
		panic(exception.NewBusinessError("取消分片上传失败: "+err.Error(), 500))
	}
}

// ===== Download =====

func Download(c *gin.Context, id string) {
	ctx := context.Background()
	var entity SysFile
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		panic(exception.NewBusinessError("文件不存在", 404))
	}

	if entity.FileURL != nil && *entity.FileURL != "" {
		c.Redirect(http.StatusFound, *entity.FileURL)
		return
	}

	if entity.FilePath == nil || *entity.FilePath == "" {
		panic(exception.NewBusinessError("文件路径为空", 404))
	}
	c.File(*entity.FilePath)
}

// byteReader returns an io.Reader from a byte slice.
func byteReader(data []byte) io.Reader {
	return &byteSliceReader{data: data}
}

type byteSliceReader struct {
	data []byte
	pos  int
}

func (r *byteSliceReader) Read(p []byte) (int, error) {
	if r.pos >= len(r.data) {
		return 0, io.EOF
	}
	n := copy(p, r.data[r.pos:])
	r.pos += n
	return n, nil
}

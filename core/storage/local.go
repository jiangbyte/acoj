package storage

import (
	"io"
	"os"
	"path/filepath"
	"crypto/rand"
	"encoding/hex"
	"fmt"
)

// LocalStorage implements FileStorage on the local filesystem.
type LocalStorage struct {
	uploadFolder string
}

// NewLocalStorage creates a new local filesystem storage backend.
func NewLocalStorage(uploadFolder string) *LocalStorage {
	return &LocalStorage{uploadFolder: uploadFolder}
}

// GetDefaultBucket returns the default bucket name for local storage.
func (s *LocalStorage) GetDefaultBucket() string {
	return "local"
}

// _ensurePath creates parent directories and returns the full file path.
func (s *LocalStorage) _ensurePath(bucket, fileKey string) (string, error) {
	fullPath := filepath.Join(s.uploadFolder, bucket, fileKey)
	dir := filepath.Dir(fullPath)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return "", err
	}
	return fullPath, nil
}

// Store writes raw bytes to the local filesystem and returns the full path.
func (s *LocalStorage) Store(bucket, fileKey string, data []byte) (string, error) {
	path, err := s._ensurePath(bucket, fileKey)
	if err != nil {
		return "", err
	}
	if err := os.WriteFile(path, data, 0644); err != nil {
		return "", err
	}
	return path, nil
}

// StoreStream copies data from a reader to the local filesystem and returns the full path.
func (s *LocalStorage) StoreStream(bucket, fileKey string, reader io.Reader) (string, error) {
	path, err := s._ensurePath(bucket, fileKey)
	if err != nil {
		return "", err
	}
	f, err := os.Create(path)
	if err != nil {
		return "", err
	}
	defer f.Close()
	if _, err := io.Copy(f, reader); err != nil {
		return "", err
	}
	return path, nil
}

// GetBytes reads and returns the raw bytes of a stored file.
func (s *LocalStorage) GetBytes(bucket, fileKey string) ([]byte, error) {
	path := filepath.Join(s.uploadFolder, bucket, fileKey)
	return os.ReadFile(path)
}

// GetURL returns the local file path as a URL.
func (s *LocalStorage) GetURL(bucket, fileKey string) string {
	return filepath.Join(s.uploadFolder, bucket, fileKey)
}

// GetAuthURL returns the same URL as GetURL since local files have no auth mechanism.
func (s *LocalStorage) GetAuthURL(bucket, fileKey string, timeoutMs int) (string, error) {
	return s.GetURL(bucket, fileKey), nil
}

// Delete removes a file from the local filesystem.
// Ignores not-found errors for idempotent deletion.
func (s *LocalStorage) Delete(bucket, fileKey string) error {
	path := filepath.Join(s.uploadFolder, bucket, fileKey)
	err := os.Remove(path)
	if os.IsNotExist(err) {
		return nil
	}
	return err
}

// Exists checks whether a file exists on the local filesystem.
func (s *LocalStorage) Exists(bucket, fileKey string) (bool, error) {
	path := filepath.Join(s.uploadFolder, bucket, fileKey)
	_, err := os.Stat(path)
	if os.IsNotExist(err) {
		return false, nil
	}
	return err == nil, err
}

// Copy copies a file from source to destination on the local filesystem.
func (s *LocalStorage) Copy(srcBucket, srcKey, dstBucket, dstKey string) error {
	src := filepath.Join(s.uploadFolder, srcBucket, srcKey)
	dst, err := s._ensurePath(dstBucket, dstKey)
	if err != nil {
		return err
	}
	return copyFile(src, dst)
}

// copyFile is a helper that copies a file from src to dst.
func copyFile(src, dst string) error {
	sourceFile, err := os.Open(src)
	if err != nil {
		return err
	}
	defer sourceFile.Close()

	destFile, err := os.Create(dst)
	if err != nil {
		return err
	}
	defer destFile.Close()

	_, err = io.Copy(destFile, sourceFile)
	return err
}

// ===== ChunkedUploader implementation =====

// InitChunkUpload creates a temporary directory for chunk storage.
func (s *LocalStorage) InitChunkUpload(bucket, fileKey string, totalChunks int) (string, error) {
	uploadID := filepath.Base(fileKey) + "_" + randomSuffix()
	dir := filepath.Join(s.uploadFolder, "tmp", uploadID)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return "", err
	}
	return uploadID, nil
}

// UploadChunk saves a single chunk to the temporary directory.
// The chunk is written to a file named by its zero-padded ChunkIndex
// so that directory listing yields correct order for merging.
func (s *LocalStorage) UploadChunk(bucket, fileKey, uploadID string, chunk ChunkInfo) error {
	dir := filepath.Join(s.uploadFolder, "tmp", uploadID)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return err
	}
	chunkPath := filepath.Join(dir, chunkFileName(chunk.ChunkIndex, chunk.TotalChunks))
	data, err := io.ReadAll(chunk.Data)
	if err != nil {
		return err
	}
	return os.WriteFile(chunkPath, data, 0644)
}

// CompleteChunkUpload reads all chunks in order, merges them into the final file,
// and cleans up the temporary directory.
func (s *LocalStorage) CompleteChunkUpload(bucket, fileKey, uploadID string) (string, error) {
	tmpDir := filepath.Join(s.uploadFolder, "tmp", uploadID)
	entries, err := os.ReadDir(tmpDir)
	if err != nil {
		return "", err
	}
	if len(entries) == 0 {
		return "", os.ErrNotExist
	}

	finalPath, err := s._ensurePath(bucket, fileKey)
	if err != nil {
		return "", err
	}

	dst, err := os.Create(finalPath)
	if err != nil {
		return "", err
	}
	defer dst.Close()

	for _, entry := range entries {
		if entry.IsDir() {
			continue
		}
		chunkPath := filepath.Join(tmpDir, entry.Name())
		src, err := os.Open(chunkPath)
		if err != nil {
			return "", err
		}
		if _, err := io.Copy(dst, src); err != nil {
			src.Close()
			return "", err
		}
		src.Close()
	}

	// Cleanup temp directory
	os.RemoveAll(tmpDir)

	return finalPath, nil
}

// AbortChunkUpload deletes the temporary directory and all chunks.
func (s *LocalStorage) AbortChunkUpload(bucket, fileKey, uploadID string) error {
	dir := filepath.Join(s.uploadFolder, "tmp", uploadID)
	return os.RemoveAll(dir)
}

// chunkFileName returns a zero-padded filename so alphanumeric sort matches index order.
func chunkFileName(index, total int) string {
	digits := 1
	for t := total; t >= 10; t /= 10 {
		digits++
	}
	return fmt.Sprintf("%0*d", digits, index)
}

// randomSuffix generates a short random hex string for upload ID uniqueness.
func randomSuffix() string {
	b := make([]byte, 8)
	rand.Read(b)
	return hex.EncodeToString(b)
}

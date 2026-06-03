package storage

import (
	"io"
	"os"
	"path/filepath"
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

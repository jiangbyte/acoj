package file

import (
	"context"
	"os"
	"path/filepath"
)

// LocalStorage implements StorageInterface for the local filesystem.
type LocalStorage struct {
	uploadFolder string
}

// NewLocalStorage creates a new LocalStorage with the given upload folder.
func NewLocalStorage(folder string) *LocalStorage {
	return &LocalStorage{uploadFolder: folder}
}

// GetDefaultBucket returns the default bucket name for local storage.
func (s *LocalStorage) GetDefaultBucket() string {
	return "local"
}

// Store saves data to the local filesystem at {uploadFolder}/{bucket}/{fileKey}.
func (s *LocalStorage) Store(_ context.Context, bucket, fileKey string, data []byte) error {
	fullPath := filepath.Join(s.uploadFolder, bucket, fileKey)
	if err := os.MkdirAll(filepath.Dir(fullPath), 0755); err != nil {
		return err
	}
	return os.WriteFile(fullPath, data, 0644)
}

// GetBytes reads data from the local filesystem at {uploadFolder}/{bucket}/{fileKey}.
func (s *LocalStorage) GetBytes(_ context.Context, bucket, fileKey string) ([]byte, error) {
	fullPath := filepath.Join(s.uploadFolder, bucket, fileKey)
	return os.ReadFile(fullPath)
}

// GetURL returns the local filesystem path as the URL.
func (s *LocalStorage) GetURL(bucket, fileKey string) string {
	return filepath.Join(s.uploadFolder, bucket, fileKey)
}

// Delete removes a file from the local filesystem.
func (s *LocalStorage) Delete(_ context.Context, bucket, fileKey string) error {
	fullPath := filepath.Join(s.uploadFolder, bucket, fileKey)
	if err := os.Remove(fullPath); err != nil && !os.IsNotExist(err) {
		return err
	}
	return nil
}

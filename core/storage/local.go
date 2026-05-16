package storage

import (
	"io"
	"os"
	"path/filepath"
)

// LocalStorage implements FileStorageInterface on the local filesystem.
type LocalStorage struct {
	UploadFolder string
}

func NewLocalStorage(uploadFolder string) *LocalStorage {
	return &LocalStorage{UploadFolder: uploadFolder}
}

func (s *LocalStorage) GetDefaultBucket() string {
	return "local"
}

func (s *LocalStorage) ensurePath(bucket, fileKey string) (string, error) {
	fullPath := filepath.Join(s.UploadFolder, bucket, fileKey)
	dir := filepath.Dir(fullPath)
	if err := os.MkdirAll(dir, 0755); err != nil {
		return "", err
	}
	return fullPath, nil
}

func (s *LocalStorage) Store(bucket, fileKey string, data []byte) (string, error) {
	path, err := s.ensurePath(bucket, fileKey)
	if err != nil {
		return "", err
	}
	if err := os.WriteFile(path, data, 0644); err != nil {
		return "", err
	}
	return path, nil
}

func (s *LocalStorage) StoreStream(bucket, fileKey string, reader io.Reader) (string, error) {
	path, err := s.ensurePath(bucket, fileKey)
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

func (s *LocalStorage) GetBytes(bucket, fileKey string) ([]byte, error) {
	path := filepath.Join(s.UploadFolder, bucket, fileKey)
	return os.ReadFile(path)
}

func (s *LocalStorage) GetURL(bucket, fileKey string) (string, error) {
	return filepath.Join(s.UploadFolder, bucket, fileKey), nil
}

func (s *LocalStorage) GetAuthURL(bucket, fileKey string, timeoutMs int) (string, error) {
	return s.GetURL(bucket, fileKey)
}

func (s *LocalStorage) Delete(bucket, fileKey string) error {
	path := filepath.Join(s.UploadFolder, bucket, fileKey)
	return os.Remove(path)
}

func (s *LocalStorage) Exists(bucket, fileKey string) (bool, error) {
	path := filepath.Join(s.UploadFolder, bucket, fileKey)
	_, err := os.Stat(path)
	if os.IsNotExist(err) {
		return false, nil
	}
	return err == nil, err
}

func (s *LocalStorage) Copy(srcBucket, srcKey, dstBucket, dstKey string) error {
	src := filepath.Join(s.UploadFolder, srcBucket, srcKey)
	dst, err := s.ensurePath(dstBucket, dstKey)
	if err != nil {
		return err
	}
	return copyFile(src, dst)
}

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

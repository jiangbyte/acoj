package storage

import (
	"encoding/json"

	"hei-gin/sdk/config"
)

// loadConfig reads storage config from config.C.Raw["storage"].
func loadConfig() Config {
	cfg := Config{
		Default: "LOCAL",
		Local:   LocalConfig{UploadFolder: "./uploads"},
	}
	raw, ok := config.C.Raw["storage"]
	if !ok {
		return cfg
	}
	// Marshal-unmarshal round-trip via JSON for map→struct conversion.
	data, err := json.Marshal(raw)
	if err != nil {
		return cfg
	}
	json.Unmarshal(data, &cfg)
	return cfg
}

// DefaultStorage returns the configured default FileStorage backend.
func DefaultStorage() FileStorage {
	cfg := loadConfig()
	return GetStorage(cfg.Default)
}

// GetStorage returns a FileStorage by type name.
// Supported types: "LOCAL", "MINIO", "S3".
// Falls back to LocalStorage for unknown types.
func GetStorage(storageType string) FileStorage {
	cfg := loadConfig()

	switch storageType {
	case "LOCAL":
		return NewLocalStorage(cfg.Local.UploadFolder)

	case "MINIO":
		m := cfg.Minio
		if m.Endpoint == "" {
			return nil
		}
		return NewMinioStorage(m.Endpoint, m.AccessKey, m.SecretKey,
			m.Bucket, m.Secure, m.Region)

	case "S3":
		s := cfg.S3
		if s.Endpoint == "" {
			return nil
		}
		return NewS3Storage(s.Endpoint, s.AccessKey, s.SecretKey,
			s.Bucket, s.Region, s.PathStyle)

	default:
		return NewLocalStorage(cfg.Local.UploadFolder)
	}
}

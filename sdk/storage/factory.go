package storage

import (
	"encoding/json"
	"log"

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
	data, err := json.Marshal(raw)
	if err != nil {
		return cfg
	}
	json.Unmarshal(data, &cfg)
	return cfg
}

// GetConfig returns the current storage configuration.
func GetConfig() Config {
	return loadConfig()
}

// GetStorage returns an Engine by type name.
// Supported types: "LOCAL", "MINIO", "S3".
// Falls back to LocalStorage for unknown types.
func GetStorage(storageType string) Engine {
	cfg := loadConfig()
	return getStorageByConfig(cfg, storageType)
}

func getStorageByConfig(cfg Config, storageType string) Engine {
	switch storageType {
	case "LOCAL":
		if cfg.Local.UploadFolder == "" {
			log.Printf("[storage] LOCAL: upload_folder not configured, using default ./uploads")
			cfg.Local.UploadFolder = "./uploads"
		}
		return NewLocal(cfg.Local.UploadFolder, cfg.Local.BaseURL)

	case "MINIO":
		m := cfg.Minio
		if m.Endpoint == "" {
			log.Printf("[storage] MINIO: endpoint not configured, returning nil")
			return nil
		}
		return NewMinio(m.Endpoint, m.AccessKey, m.SecretKey, m.Bucket, m.Secure, m.Region, m.BaseURL)

	case "S3":
		s := cfg.S3
		if s.Endpoint == "" {
			log.Printf("[storage] S3: endpoint not configured, returning nil")
			return nil
		}
		return NewS3(s.Endpoint, s.AccessKey, s.SecretKey, s.Bucket, s.Region, s.PathStyle, s.BaseURL)

	default:
		return NewLocal(cfg.Local.UploadFolder, cfg.Local.BaseURL)
	}
}

// GetURL returns the full HTTP URL for a stored file.
// It checks engine-specific base URL first, then falls back to the default base URL.
func GetURL(storageType, bucket, fileKey string) string {
	cfg := loadConfig()
	eng := getStorageByConfig(cfg, storageType)
	if eng == nil {
		return ""
	}

	// Try engine-specific URL builder
	if ub, ok := eng.(interface{ GetURL(bucket, fileKey string) string }); ok {
		url := ub.GetURL(bucket, fileKey)
		if url != "" {
			// If URL is relative and DefaultBaseURL is configured, prepend it
			if len(url) > 0 && url[0] == '/' && cfg.DefaultBaseURL != "" {
				return cfg.DefaultBaseURL + url
			}
			return url
		}
	}

	return ""
}

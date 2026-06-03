package storage

// Config holds storage backend configuration.
// Loaded from config.C.Raw["storage"].
type Config struct {
	// Default backend type: "LOCAL", "MINIO", or "S3".
	Default string          `yaml:"default"`
	Local   LocalConfig     `yaml:"local"`
	Minio   MinioConfig     `yaml:"minio"`
	S3      S3Config        `yaml:"s3"`
}

type LocalConfig struct {
	UploadFolder string `yaml:"upload_folder"`
}

type MinioConfig struct {
	Endpoint  string `yaml:"endpoint"`
	AccessKey string `yaml:"access_key"`
	SecretKey string `yaml:"secret_key"`
	Bucket    string `yaml:"bucket"`
	Secure    bool   `yaml:"secure"`
	Region    string `yaml:"region"`
}

type S3Config struct {
	Endpoint  string `yaml:"endpoint"`
	AccessKey string `yaml:"access_key"`
	SecretKey string `yaml:"secret_key"`
	Bucket    string `yaml:"bucket"`
	Region    string `yaml:"region"`
	PathStyle bool   `yaml:"path_style"`
}

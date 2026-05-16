package config

import (
	"os"

	"gopkg.in/yaml.v3"
)

type Config struct {
	App       AppConfig       `yaml:"app"`
	DB        DatabaseConfig  `yaml:"db"`
	Redis     RedisConfig     `yaml:"redis"`
	JWT       JWTConfig       `yaml:"jwt"`
	SM2       SM2Config       `yaml:"sm2"`
	CORS      CORSConfig      `yaml:"cors"`
	Snowflake SnowflakeConfig `yaml:"snowflake"`
}

type AppConfig struct {
	Name             string `yaml:"name"`
	Version          string `yaml:"version"`
	Debug            bool   `yaml:"debug"`
	Host             string `yaml:"host"`
	Port             int    `yaml:"port"`
	UploadMaxSize    int64  `yaml:"upload_max_size"`
	TimeoutKeepAlive int    `yaml:"timeout_keep_alive"`
}

type DatabaseConfig struct {
	Host           string `yaml:"host"`
	Port           int    `yaml:"port"`
	User           string `yaml:"user"`
	Password       string `yaml:"password"`
	Database       string `yaml:"database"`
	PoolSize       int    `yaml:"pool_size"`
	MaxOverflow    int    `yaml:"max_overflow"`
	PoolRecycle    int    `yaml:"pool_recycle"`
	PoolPrePing    bool   `yaml:"pool_pre_ping"`
	PoolTimeout    int    `yaml:"pool_timeout"`
	ConnectTimeout int    `yaml:"connect_timeout"`
	Echo           bool   `yaml:"echo"`
}

type RedisConfig struct {
	Host                 string `yaml:"host"`
	Port                 int    `yaml:"port"`
	Password             string `yaml:"password"`
	Database             int    `yaml:"database"`
	MaxConnections       int    `yaml:"max_connections"`
	SocketConnectTimeout int    `yaml:"socket_connect_timeout"`
	SocketTimeout        int    `yaml:"socket_timeout"`
	RetryOnTimeout       bool   `yaml:"retry_on_timeout"`
	HealthCheckInterval  int    `yaml:"health_check_interval"`
}

type JWTConfig struct {
	SecretKey     string `yaml:"secret_key"`
	Algorithm     string `yaml:"algorithm"`
	ExpireSeconds int    `yaml:"expire_seconds"`
	TokenName     string `yaml:"token_name"`
}

type SM2Config struct {
	PrivateKey string `yaml:"private_key"`
	PublicKey  string `yaml:"public_key"`
}

type CORSConfig struct {
	AllowOrigins     []string `yaml:"allow_origins"`
	AllowMethods     []string `yaml:"allow_methods"`
	AllowHeaders     []string `yaml:"allow_headers"`
	AllowCredentials bool     `yaml:"allow_credentials"`
}

type SnowflakeConfig struct {
	Instance int64 `yaml:"instance"`
}

var C *Config

func Load(path string) error {
	data, err := os.ReadFile(path)
	if err != nil {
		return err
	}
	C = &Config{}
	return yaml.Unmarshal(data, C)
}

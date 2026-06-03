package config

import (
	"os"

	"gopkg.in/yaml.v3"
)

type Config struct {
	App       AppConfig       `yaml:"app"`
	DB        DatabaseConfig  `yaml:"db"`
	Redis     RedisConfig     `yaml:"redis"`
	Token     TokenConfig     `yaml:"token"`
	SM2       SM2Config       `yaml:"sm2"`
	CORS      CORSConfig      `yaml:"cors"`
	Snowflake SnowflakeConfig `yaml:"snowflake"`
	Judge     JudgeConfig     `yaml:"judge"`
	Contest   ContestConfig   `yaml:"contest"`
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

type TokenConfig struct {
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
	Judge     JudgeConfig     `yaml:"judge"`
	Contest   ContestConfig   `yaml:"contest"`
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
// ===== Judge Config =====

type JudgeConfig struct {
	Sandbox JudgeSandboxConfig `yaml:"sandbox"`
	Redis JudgeRedisConfig `yaml:"redis"`
	Concurrency        int               `yaml:"concurrency"`
	CompileTimeLimit   int               `yaml:"compile_time_limit"`
	CompileMemoryLimit int64             `yaml:"compile_memory_limit"`
	OutputLimit        int64             `yaml:"output_limit"`
	SubtaskShortcut    bool              `yaml:"subtask_shortcut"`
	ShutdownTimeout    string            `yaml:"shutdown_timeout"`
	Registry           JudgeRegistryConfig `yaml:"registry"`
	Languages          []JudgeLangConfig `yaml:"languages"`
}


type JudgeSandboxConfig struct {
	Addrs []string `yaml:"addrs"`
}

type JudgeRegistryConfig struct {
	HeartbeatTimeout string `yaml:"heartbeat_timeout"`
	RemoveAfter      string `yaml:"remove_after"`
	ProbeInterval    string `yaml:"probe_interval"`
	ProbeTimeout     string `yaml:"probe_timeout"`
	MaxFailures      int    `yaml:"max_failures"`
}

type JudgeLangConfig struct {
	Name        string   `yaml:"name"`
	CompileArgs []string `yaml:"compile_args"`
	RunArgs     []string `yaml:"run_args"`
	Extension   string   `yaml:"extension"`
}

type ContestConfig struct {
	ACMPenaltyMinutes    int  `yaml:"acm_penalty_minutes"`
	HackTimeLimitMs      int  `yaml:"hack_time_limit_ms"`
	SystemTestConcurrency int `yaml:"system_test_concurrency"`
}

func (c *Config) JudgeEnabled() bool {
	return c.Judge.Concurrency > 0
}

type JudgeRedisConfig struct {
	StreamKey      string `yaml:"stream_key"`
	ConsumerGroup  string `yaml:"consumer_group"`
	ClaimIdleMs    int    `yaml:"claim_idle_ms"`
}

package config

import (
	"github.com/zeromicro/go-zero/rest"
)

type LanguageConfig struct {
	Name        string   `json:"name"`        // 语言名称，如 "go", "python"
	SourceFile  string   `json:"sourceFile"`  // 源文件名，如 "main", "Main"
	CompileFile string   `json:"compileFile"` // 编译文件名，如 "main", "Main"
	Extension   string   `json:"extension"`   // 文件扩展名，如 ".go", ".py"
	CompileCmd  []string `json:"compileCmd"`  // 编译命令模板，如 ["go", "build", "-o", "{output}", "{source}"]
	RunCmd      []string `json:"runCmd"`      // 运行命令模板，如 ["./{output}"] 或 ["python3", "{source}"]
	NeedCompile bool     `json:",default=true"`
}

type NacosConfig struct {
	ServerAddr string `json:",optional"` // 120.26.180.149:8848
	Username   string `json:",optional"` // 可选
	Password   string `json:",optional"` // 可选
	Namespace  string `json:",optional"` // 8fee08f3-44ea-4e26-a9b5-530c582330a3
	Group      string `json:",default=DEFAULT_GROUP"`
	DataId     string `json:",optional"`
}

// MySQL 配置
type MySQLConfig struct {
	Host            string `json:",default=127.0.0.1:3306"`
	Username        string `json:",default=root"`
	Password        string `json:",optional"`
	Database        string `json:",default="`
	Charset         string `json:",default=utf8mb4"`
	ParseTime       bool   `json:",default=true"`
	Loc             string `json:",default=Local"`
	MaxIdleConns    int    `json:",default=10"`
	MaxOpenConns    int    `json:",default=100"`
	ConnMaxLifetime string `json:",default=1h"`
}

type RabbitMQConfig struct {
	Host        string `json:",optional"` // 主机地址
	VirtualHost string `json:",optional"` // 虚拟主机
	// 常规队列
	Common struct {
		JudgeExchange    string `json:",optional"`
		JudgeQueue       string `json:",optional"`
		JudgeRoutingKey  string `json:",optional"`
		ResultExchange   string `json:",optional"`
		ResultQueue      string `json:",optional"`
		ResultRoutingKey string `json:",optional"`
	} `json:",optional"`
}

type RedisConfig struct {
	Host     string `json:",default=127.0.0.1:6379"`
	Password string `json:",optional"`
	DB       int    `json:",default=0"`
	PoolSize int    `json:",default=10"`
}

// SandboxConfig 沙箱：cgroup 资源限制 + 进程隔离（网络/用户/可选 bwrap 目录 jail）。
type SandboxConfig struct {
	// Mode: auto | cgroup。按本机挂载选择 v2 或 v1。
	Mode string `json:",default=auto"`
	// DisableNetwork：用户进程进入空网络命名空间，禁止访问外网/内网服务。
	DisableNetwork bool `json:",default=true"`
	// DropPrivileges：以 Uid/Gid 运行用户进程（需 judge 以 root 启动）。
	DropPrivileges bool `json:",default=true"`
	Uid            uint32 `json:",default=65534"`
	Gid            uint32 `json:",default=65534"`
	// UseBwrap：auto|on|off。auto 表示检测到 bubblewrap 则用其做目录隔离。
	UseBwrap string `json:",default=auto"`
}

// 配置队列
type Config struct {
	rest.RestConf
	Workspace string `json:",optional"` // 工作空间(文件工作路径)
	Nacos     NacosConfig
	MySQL     MySQLConfig      `json:",optional"` // MySQL 配置
	Languages []LanguageConfig `json:",optional"` // 支持的语言配置，设为可选
	RabbitMQ  RabbitMQConfig   `json:",optional"`
	Redis     RedisConfig      `json:",optional"` // 新增 Redis 配置
	Sandbox   SandboxConfig    `json:",optional"` // 沙箱模式
}

package config

import (
	"github.com/zeromicro/go-zero/rest"
)

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
		SimilarityExchange   string `json:",optional"`
		SimilarityQueue      string `json:",optional"`
		SimilarityRoutingKey string `json:",optional"`
		ResultExchange       string `json:",optional"`
		ResultQueue          string `json:",optional"`
		ResultRoutingKey     string `json:",optional"`
	} `json:",optional"`
}

// 配置队列
type Config struct {
	rest.RestConf
	Nacos    NacosConfig
	MySQL    MySQLConfig    `json:",optional"` // MySQL 配置
	RabbitMQ RabbitMQConfig `json:",optional"`
}

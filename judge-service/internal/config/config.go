package config

import (
	"github.com/zeromicro/go-zero/rest"
)

type LanguageConfig struct {
	Name               string   `json:"name"`               // 语言名称，如 "go", "python"
	SourceFile         string   `json:"sourceFile"`         // 源文件名，如 "main", "Main"
	CompileFile        string   `json:"compileFile"`        // 编译文件名，如 "main", "Main"
	DefaultCompileTime int64    `json:"defaultCompileTime"` // 默认编译时间，单位毫秒
	DefaultExecTime    int64    `json:"defaultExecTime"`    // 默认运行时间，单位毫秒
	Extension          string   `json:"extension"`          // 文件扩展名，如 ".go", ".py"
	CompileCmd         []string `json:"compileCmd"`         // 编译命令模板，如 ["go", "build", "-o", "{output}", "{source}"]
	RunCmd             []string `json:"runCmd"`             // 运行命令模板，如 ["./{output}"] 或 ["python3", "{source}"]
}

// 配置队列
type Config struct {
	rest.RestConf
	Workspace string
	Languages []LanguageConfig `json:"languages"` // 支持的语言配置
	RabbitMQ  struct {
		Host        string // 主机地址
		VirtualHost string // 虚拟主机
		// 普通题目队列
		Problems struct {
			JudgeExchange    string
			JudgeQueue       string
			JudgeRoutingKey  string
			ResultExchange   string
			ResultQueue      string
			ResultRoutingKey string
		}
		// 题集题目队列
		Sets struct {
			JudgeExchange    string
			JudgeQueue       string
			JudgeRoutingKey  string
			ResultExchange   string
			ResultQueue      string
			ResultRoutingKey string
		}
	}
}

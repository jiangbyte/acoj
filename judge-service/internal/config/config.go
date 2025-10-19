// package config

// import (
// 	"github.com/zeromicro/go-zero/rest"
// )

// type LanguageConfig struct {
// 	Name               string   `json:"name"`               // 语言名称，如 "go", "python"
// 	SourceFile         string   `json:"sourceFile"`         // 源文件名，如 "main", "Main"
// 	CompileFile        string   `json:"compileFile"`        // 编译文件名，如 "main", "Main"
// 	DefaultCompileTime int64    `json:"defaultCompileTime"` // 默认编译时间，单位毫秒
// 	DefaultExecTime    int64    `json:"defaultExecTime"`    // 默认运行时间，单位毫秒
// 	Extension          string   `json:"extension"`          // 文件扩展名，如 ".go", ".py"
// 	CompileCmd         []string `json:"compileCmd"`         // 编译命令模板，如 ["go", "build", "-o", "{output}", "{source}"]
// 	RunCmd             []string `json:"runCmd"`             // 运行命令模板，如 ["./{output}"] 或 ["python3", "{source}"]
// }

// type NacosConfig struct {
// 	ServerAddr string `json:",optional"` // 120.26.180.149:8848
// 	Username   string `json:",optional"` // 可选
// 	Password   string `json:",optional"` // 可选
// 	Namespace  string `json:",optional"` // 8fee08f3-44ea-4e26-a9b5-530c582330a3
// 	Group      string `json:",default=DEFAULT_GROUP"`
// 	DataId     string `json:",optional"`
// }

// // 配置队列
// type Config struct {
// 	rest.RestConf
// 	Name      string `json:",optional"` // 服务名称
// 	Workspace string `json:",optional"` // 工作空间
// 	// Workspace string
// 	Nacos     NacosConfig
// 	// Languages []LanguageConfig `json:"languages"` // 支持的语言配置
// 	Languages []LanguageConfig `json:",optional"` // 支持的语言配置
// 	RabbitMQ  struct {
// 		Host        string // 主机地址
// 		VirtualHost string // 虚拟主机
// 		// 普通题目队列
// 		Problems struct {
// 			JudgeExchange    string
// 			JudgeQueue       string
// 			JudgeRoutingKey  string
// 			ResultExchange   string
// 			ResultQueue      string
// 			ResultRoutingKey string
// 		}
// 		// 题集题目队列
// 		Sets struct {
// 			JudgeExchange    string
// 			JudgeQueue       string
// 			JudgeRoutingKey  string
// 			ResultExchange   string
// 			ResultQueue      string
// 			ResultRoutingKey string
// 		}
// 		Common struct {
// 			JudgeExchange    string
// 			JudgeQueue       string
// 			JudgeRoutingKey  string
// 			ResultExchange   string
// 			ResultQueue      string
// 			ResultRoutingKey string
// 		}
// 	}
// }

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

type NacosConfig struct {
	ServerAddr string `json:",optional"` // 120.26.180.149:8848
	Username   string `json:",optional"` // 可选
	Password   string `json:",optional"` // 可选
	Namespace  string `json:",optional"` // 8fee08f3-44ea-4e26-a9b5-530c582330a3
	Group      string `json:",default=DEFAULT_GROUP"`
	DataId     string `json:",optional"`
}

// 配置队列
type Config struct {
	rest.RestConf
	Workspace string `json:",optional"` // 工作空间
	Nacos     NacosConfig
	Languages []LanguageConfig `json:",optional"` // 支持的语言配置，设为可选
	RabbitMQ  struct {
		Host        string `json:",optional"` // 主机地址
		VirtualHost string `json:",optional"` // 虚拟主机
		// 普通题目队列
		Problems struct {
			JudgeExchange    string `json:",optional"`
			JudgeQueue       string `json:",optional"`
			JudgeRoutingKey  string `json:",optional"`
			ResultExchange   string `json:",optional"`
			ResultQueue      string `json:",optional"`
			ResultRoutingKey string `json:",optional"`
		} `json:",optional"`
		// 题集题目队列
		Sets struct {
			JudgeExchange    string `json:",optional"`
			JudgeQueue       string `json:",optional"`
			JudgeRoutingKey  string `json:",optional"`
			ResultExchange   string `json:",optional"`
			ResultQueue      string `json:",optional"`
			ResultRoutingKey string `json:",optional"`
		} `json:",optional"`
		Common struct {
			JudgeExchange    string `json:",optional"`
			JudgeQueue       string `json:",optional"`
			JudgeRoutingKey  string `json:",optional"`
			ResultExchange   string `json:",optional"`
			ResultQueue      string `json:",optional"`
			ResultRoutingKey string `json:",optional"`
		} `json:",optional"`
	} `json:",optional"`
}

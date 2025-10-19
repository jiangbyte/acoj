// package main

// import (
// 	"context"
// 	"flag"
// 	"fmt"
// 	"judge-service/internal/config"
// 	"judge-service/internal/logic"
// 	"judge-service/internal/nacos"
// 	"judge-service/internal/svc"
// 	"os"
// 	"os/signal"
// 	"syscall"

// 	"github.com/zeromicro/go-zero/core/conf"
// 	"github.com/zeromicro/go-zero/core/logx"
// )

// // var configFile = flag.String("f", "etc/judge.yaml", "the config file")

// var (
// 	configFile = flag.String("f", "etc/judge.yaml", "本地配置文件路径")
// 	useNacos   = flag.Bool("nacos", false, "是否使用 Nacos 配置中心")
// )

// func main() {
// 	flag.Parse()

// 	var localConfig config.Config
// 	// conf.MustLoad(*configFile, &localConfig)

// 	if *useNacos {
//         fmt.Println("Starting application with Nacos configuration...")

//         // 先加载本地配置获取 Nacos 连接信息
//         if err := conf.Load(*configFile, &localConfig); err != nil {
//             logx.Errorf("Failed to load local config: %v", err)
//             return
//         }

//         // 检查 Nacos 配置
//         if localConfig.Nacos.ServerAddr == "" {
//             logx.Error("Nacos server address is required when using nacos")
//             return
//         }

//         if localConfig.Nacos.DataId == "" {
//             logx.Error("Nacos dataId is required when using nacos")
//             return
//         }

//         // 初始化 Nacos 配置管理器
//         nacosManager, err := nacos.NewNacosConfigManager(
//             localConfig.Nacos.ServerAddr,
//             localConfig.Nacos.Username,
//             localConfig.Nacos.Password,
//             localConfig.Nacos.Namespace,
//             localConfig.Nacos.Group,
//         )
//         if err != nil {
//             logx.Errorf("Failed to create nacos manager: %v", err)
//             logx.Info("Fallback to local configuration")
//             // 回退到本地配置
//             conf.MustLoad(*configFile, &localConfig)
//         } else {
//             // 从 Nacos 获取配置
//             nacosConfig, err := nacosManager.GetConfig(localConfig.Nacos.DataId)
//             if err != nil {
//                 logx.Errorf("Failed to get config from nacos: %v", err)
//                 logx.Info("Fallback to local configuration")
//                 // 回退到本地配置
//                 conf.MustLoad(*configFile, &localConfig)
//             } else {
//                 // 解析 Nacos 配置
//                 if err := conf.LoadFromYamlBytes([]byte(nacosConfig), &localConfig); err != nil {
//                     logx.Errorf("Failed to parse nacos config: %v", err)
//                     logx.Info("Fallback to local configuration")
//                     // 回退到本地配置
//                     conf.MustLoad(*configFile, &localConfig)
//                 } else {
//                     fmt.Println("Loaded configuration from Nacos successfully")
//                 }
//             }
//         }
//     } else {
//         // 使用本地配置
//         conf.MustLoad(*configFile, &localConfig)
//         fmt.Println("Loaded configuration from local file")
//     }

// 	ctx := svc.NewServiceContext(localConfig)
// 	consumeLogic := logic.NewConsumeLogic(context.Background(), ctx)

// 	// 启动消费者
// 	go consumeLogic.StartConsuming()

// 	// 优雅退出
// 	ch := make(chan os.Signal, 1)
// 	signal.Notify(ch, syscall.SIGTERM, syscall.SIGINT)
// 	<-ch

// 	fmt.Println("Shutting down server...")
// 	// 关闭通道和连接
// 	// if err := ctx.ProblemChannel.Close(); err != nil {
// 	// 	logx.Errorf("Error closing ProblemChannel RabbitMQ connection: %v", err)
// 	// }
// 	// if err := ctx.ProblemSetChannel.Close(); err != nil {
// 	// 	logx.Errorf("Error closing ProblemSetChannel RabbitMQ connection: %v", err)
// 	// }
// 	if err := ctx.CommonChannel.Close(); err != nil {
// 		logx.Errorf("Error closing CommonChannel RabbitMQ connection: %v", err)
// 	}
// 	if err := ctx.RabbitMQ.Close(); err != nil {
// 		logx.Errorf("Error closing RabbitMQ connection: %v", err)
// 	}

// 	fmt.Println("Server exited")
// }

package main

import (
	"context"
	"flag"
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/logic"
	"judge-service/internal/nacos"
	"judge-service/internal/svc"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/zeromicro/go-zero/core/conf"
	"github.com/zeromicro/go-zero/core/logx"
)

var (
	configFile = flag.String("f", "etc/judge.yaml", "本地配置文件路径")
	useNacos   = flag.Bool("nacos", false, "是否使用 Nacos 配置中心")
)

func main() {
	flag.Parse()

	// 设置 Nacos 日志级别为 error，关闭 DEBUG 日志
	os.Setenv("NACOS_LOG_LEVEL", "error")

	var localConfig config.Config

	if *useNacos {
		fmt.Println("Starting application with Nacos configuration...")

		// 先加载本地配置获取 Nacos 连接信息
		if err := conf.Load(*configFile, &localConfig); err != nil {
			logx.Errorf("Failed to load local config: %v", err)
			return
		}

		// 检查 Nacos 配置
		if localConfig.Nacos.ServerAddr == "" {
			logx.Error("Nacos server address is required when using nacos")
			return
		}

		if localConfig.Nacos.DataId == "" {
			logx.Error("Nacos dataId is required when using nacos")
			return
		}

		// 初始化 Nacos 配置管理器
		nacosManager, err := nacos.NewNacosConfigManager(
			localConfig.Nacos.ServerAddr,
			localConfig.Nacos.Username,
			localConfig.Nacos.Password,
			localConfig.Nacos.Namespace,
			localConfig.Nacos.Group,
		)
		if err != nil {
			logx.Errorf("Failed to create nacos manager: %v", err)
			logx.Info("Fallback to local configuration")
			// 回退到本地配置
			conf.MustLoad(*configFile, &localConfig)
		} else {
			// 从 Nacos 获取配置
			nacosConfig, err := nacosManager.GetConfig(localConfig.Nacos.DataId)
			if err != nil {
				logx.Errorf("Failed to get config from nacos: %v", err)
				logx.Info("Fallback to local configuration")
				// 回退到本地配置
				conf.MustLoad(*configFile, &localConfig)
			} else {
				// 解析 Nacos 配置
				if err := conf.LoadFromYamlBytes([]byte(nacosConfig), &localConfig); err != nil {
					logx.Errorf("Failed to parse nacos config: %v", err)
					logx.Info("Fallback to local configuration")
					// 回退到本地配置
					conf.MustLoad(*configFile, &localConfig)
				} else {
					fmt.Println("Loaded configuration from Nacos successfully")
				}
			}
		}
	} else {
		// 使用本地配置
		conf.MustLoad(*configFile, &localConfig)
		fmt.Println("Loaded configuration from local file")
	}

	ctx := svc.NewServiceContext(localConfig)
	consumeLogic := logic.NewConsumeLogic(context.Background(), ctx)

	// 启动消费者
	go consumeLogic.StartConsuming()

	// 优雅退出
	ch := make(chan os.Signal, 1)
	signal.Notify(ch, syscall.SIGTERM, syscall.SIGINT)
	<-ch

	fmt.Println("Shutting down server...")

	// 注销服务
	if ctx.ServiceRegistry != nil {
		if err := ctx.ServiceRegistry.Deregister(); err != nil {
			logx.Errorf("Failed to deregister service: %v", err)
		} else {
			fmt.Println("Service deregistered from Nacos")
		}
	}

	// 关闭 RabbitMQ 连接
	if err := ctx.CommonChannel.Close(); err != nil {
		logx.Errorf("Error closing CommonChannel RabbitMQ connection: %v", err)
	}
	if err := ctx.RabbitMQ.Close(); err != nil {
		logx.Errorf("Error closing RabbitMQ connection: %v", err)
	}

	// 等待一段时间确保资源释放
	time.Sleep(2 * time.Second)
	fmt.Println("Server exited")
}

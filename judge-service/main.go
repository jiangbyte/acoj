// // package main

// // import (
// // 	"context"
// // 	"flag"
// // 	"fmt"
// // 	"judge-service/internal/config"
// // 	"judge-service/internal/logic"
// // 	"judge-service/internal/nacos"
// // 	"judge-service/internal/svc"
// // 	"os"
// // 	"os/signal"
// // 	"syscall"

// // 	"github.com/zeromicro/go-zero/core/conf"
// // 	"github.com/zeromicro/go-zero/core/logx"
// // )

// // // var configFile = flag.String("f", "etc/judge.yaml", "the config file")

// // var (
// // 	configFile = flag.String("f", "etc/judge.yaml", "本地配置文件路径")
// // 	useNacos   = flag.Bool("nacos", false, "是否使用 Nacos 配置中心")
// // )

// // func main() {
// // 	flag.Parse()

// // 	var localConfig config.Config
// // 	// conf.MustLoad(*configFile, &localConfig)

// // 	if *useNacos {
// //         fmt.Println("Starting application with Nacos configuration...")

// //         // 先加载本地配置获取 Nacos 连接信息
// //         if err := conf.Load(*configFile, &localConfig); err != nil {
// //             logx.Errorf("Failed to load local config: %v", err)
// //             return
// //         }

// //         // 检查 Nacos 配置
// //         if localConfig.Nacos.ServerAddr == "" {
// //             logx.Error("Nacos server address is required when using nacos")
// //             return
// //         }

// //         if localConfig.Nacos.DataId == "" {
// //             logx.Error("Nacos dataId is required when using nacos")
// //             return
// //         }

// //         // 初始化 Nacos 配置管理器
// //         nacosManager, err := nacos.NewNacosConfigManager(
// //             localConfig.Nacos.ServerAddr,
// //             localConfig.Nacos.Username,
// //             localConfig.Nacos.Password,
// //             localConfig.Nacos.Namespace,
// //             localConfig.Nacos.Group,
// //         )
// //         if err != nil {
// //             logx.Errorf("Failed to create nacos manager: %v", err)
// //             logx.Info("Fallback to local configuration")
// //             // 回退到本地配置
// //             conf.MustLoad(*configFile, &localConfig)
// //         } else {
// //             // 从 Nacos 获取配置
// //             nacosConfig, err := nacosManager.GetConfig(localConfig.Nacos.DataId)
// //             if err != nil {
// //                 logx.Errorf("Failed to get config from nacos: %v", err)
// //                 logx.Info("Fallback to local configuration")
// //                 // 回退到本地配置
// //                 conf.MustLoad(*configFile, &localConfig)
// //             } else {
// //                 // 解析 Nacos 配置
// //                 if err := conf.LoadFromYamlBytes([]byte(nacosConfig), &localConfig); err != nil {
// //                     logx.Errorf("Failed to parse nacos config: %v", err)
// //                     logx.Info("Fallback to local configuration")
// //                     // 回退到本地配置
// //                     conf.MustLoad(*configFile, &localConfig)
// //                 } else {
// //                     fmt.Println("Loaded configuration from Nacos successfully")
// //                 }
// //             }
// //         }
// //     } else {
// //         // 使用本地配置
// //         conf.MustLoad(*configFile, &localConfig)
// //         fmt.Println("Loaded configuration from local file")
// //     }

// // 	ctx := svc.NewServiceContext(localConfig)
// // 	consumeLogic := logic.NewConsumeLogic(context.Background(), ctx)

// // 	// 启动消费者
// // 	go consumeLogic.StartConsuming()

// // 	// 优雅退出
// // 	ch := make(chan os.Signal, 1)
// // 	signal.Notify(ch, syscall.SIGTERM, syscall.SIGINT)
// // 	<-ch

// // 	fmt.Println("Shutting down server...")
// // 	// 关闭通道和连接
// // 	// if err := ctx.ProblemChannel.Close(); err != nil {
// // 	// 	logx.Errorf("Error closing ProblemChannel RabbitMQ connection: %v", err)
// // 	// }
// // 	// if err := ctx.ProblemSetChannel.Close(); err != nil {
// // 	// 	logx.Errorf("Error closing ProblemSetChannel RabbitMQ connection: %v", err)
// // 	// }
// // 	if err := ctx.CommonChannel.Close(); err != nil {
// // 		logx.Errorf("Error closing CommonChannel RabbitMQ connection: %v", err)
// // 	}
// // 	if err := ctx.RabbitMQ.Close(); err != nil {
// // 		logx.Errorf("Error closing RabbitMQ connection: %v", err)
// // 	}

// // 	fmt.Println("Server exited")
// // }

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
// 	"time"

// 	"github.com/zeromicro/go-zero/core/conf"
// 	"github.com/zeromicro/go-zero/core/logx"
// )

// var (
// 	configFile = flag.String("f", "etc/judge.yaml", "本地配置文件路径")
// 	useNacos   = flag.Bool("nacos", false, "是否使用 Nacos 配置中心")
// )

// func main() {
// 	flag.Parse()

// 	// 设置 Nacos 日志级别为 error，关闭 DEBUG 日志
// 	os.Setenv("NACOS_LOG_LEVEL", "error")

// 	var localConfig config.Config
//     var nacosManager *nacos.ConfigManager

// 	if *useNacos {
// 		fmt.Println("Starting application with Nacos configuration...")

// 		// 先加载本地配置获取 Nacos 连接信息
// 		if err := conf.Load(*configFile, &localConfig); err != nil {
// 			logx.Errorf("Failed to load local config: %v", err)
// 			return
// 		}

// 		// 检查 Nacos 配置
// 		if localConfig.Nacos.ServerAddr == "" {
// 			logx.Error("Nacos server address is required when using nacos")
// 			return
// 		}

// 		if localConfig.Nacos.DataId == "" {
// 			logx.Error("Nacos dataId is required when using nacos")
// 			return
// 		}

// 		// 初始化 Nacos 配置管理器
// 		 var err error
// 		nacosManager, err = nacos.NewNacosConfigManager(
// 			localConfig.Nacos.ServerAddr,
// 			localConfig.Nacos.Username,
// 			localConfig.Nacos.Password,
// 			localConfig.Nacos.Namespace,
// 			localConfig.Nacos.Group,
// 		)
// 		if err != nil {
// 			logx.Errorf("Failed to create nacos manager: %v", err)
// 			logx.Info("Fallback to local configuration")
// 			// 回退到本地配置
// 			conf.MustLoad(*configFile, &localConfig)
// 		} else {
// 			// 从 Nacos 获取配置
// 			nacosConfig, err := nacosManager.GetConfig(localConfig.Nacos.DataId)
// 			if err != nil {
// 				logx.Errorf("Failed to get config from nacos: %v", err)
// 				logx.Info("Fallback to local configuration")
// 				// 回退到本地配置
// 				conf.MustLoad(*configFile, &localConfig)
// 			} else {
// 				// 解析 Nacos 配置
// 				if err := conf.LoadFromYamlBytes([]byte(nacosConfig), &localConfig); err != nil {
// 					logx.Errorf("Failed to parse nacos config: %v", err)
// 					logx.Info("Fallback to local configuration")
// 					// 回退到本地配置
// 					conf.MustLoad(*configFile, &localConfig)
// 				} else {
// 					fmt.Println("Loaded configuration from Nacos successfully")

// 					// 启动配置监听
// 					if err := startConfigListener(nacosManager, &localConfig, nacosConfig); err != nil {
// 						logx.Errorf("Failed to start config listener: %v", err)
// 					}
// 				}
// 			}
// 		}
// 	} else {
// 		// 使用本地配置
// 		conf.MustLoad(*configFile, &localConfig)
// 		fmt.Println("Loaded configuration from local file")
// 	}

// 	ctx := svc.NewServiceContext(localConfig)
// 	consumeLogic := logic.NewConsumeLogic(context.Background(), ctx)

// 	// 启动消费者
// 	go consumeLogic.StartConsuming()

// 	// 优雅退出
// 	ch := make(chan os.Signal, 1)
// 	signal.Notify(ch, syscall.SIGTERM, syscall.SIGINT)
// 	<-ch

// 	fmt.Println("Shutting down server...")

// 	// 取消配置监听
// 	if nacosManager != nil && localConfig.Nacos.DataId != "" {
// 		if err := nacosManager.CancelListenConfig(localConfig.Nacos.DataId); err != nil {
// 			logx.Errorf("Failed to cancel config listening: %v", err)
// 		}
// 	}

// 	// 注销服务
// 	if ctx.ServiceRegistry != nil {
// 		if err := ctx.ServiceRegistry.Deregister(); err != nil {
// 			logx.Errorf("Failed to deregister service: %v", err)
// 		} else {
// 			fmt.Println("Service deregistered from Nacos")
// 		}
// 	}

// 	// 关闭 RabbitMQ 连接
// 	if err := ctx.CommonChannel.Close(); err != nil {
// 		logx.Errorf("Error closing CommonChannel RabbitMQ connection: %v", err)
// 	}
// 	if err := ctx.RabbitMQ.Close(); err != nil {
// 		logx.Errorf("Error closing RabbitMQ connection: %v", err)
// 	}

// 	// 等待一段时间确保资源释放
// 	time.Sleep(2 * time.Second)
// 	fmt.Println("Server exited")
// }

// // startConfigListener 启动配置监听
// func startConfigListener(nacosManager *nacos.ConfigManager, currentConfig *config.Config, currentConfigContent string) error {
// 	if nacosManager == nil || currentConfig.Nacos.DataId == "" {
// 		return fmt.Errorf("nacos manager or dataId is nil")
// 	}

// 	// 定义配置变化回调函数
// 	onConfigChange := func(newConfigContent string) {
// 		logx.Info("Received configuration change from Nacos")

// 		var newConfig config.Config
// 		if err := conf.LoadFromYamlBytes([]byte(newConfigContent), &newConfig); err != nil {
// 			logx.Errorf("Failed to parse updated nacos config: %v", err)
// 			return
// 		}

// 		// 这里可以添加配置热更新逻辑
// 		// 例如：重启服务、更新连接等
// 		logx.Info("Configuration updated successfully, consider restarting service for changes to take effect")

// 		// 更新当前配置内容
// 		currentConfigContent = newConfigContent
// 		*currentConfig = newConfig
// 	}

// 	// 启动监听
// 	if err := nacosManager.ListenConfig(currentConfig.Nacos.DataId, onConfigChange); err != nil {
// 		return fmt.Errorf("failed to listen config changes: %v", err)
// 	}

// 	logx.Infof("Started listening for config changes, dataId: %s", currentConfig.Nacos.DataId)
// 	return nil
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
	"sync"
	"syscall"

	"github.com/zeromicro/go-zero/core/conf"
	"github.com/zeromicro/go-zero/core/logx"
)

var (
	configFile = flag.String("f", "etc/judge.yaml", "本地配置文件路径")
	useNacos   = flag.Bool("nacos", false, "是否使用 Nacos 配置中心")

	// 全局变量用于管理服务状态
	serviceCtx   *svc.ServiceContext
	consumeLogic *logic.ConsumeLogic
	ctxMutex     sync.RWMutex
	restartChan  chan struct{}
)

func main() {
	flag.Parse()

	var localConfig config.Config
	var nacosManager *nacos.ConfigManager

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
		var err error
		nacosManager, err = nacos.NewNacosConfigManager(
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
			configContent, err := nacosManager.GetConfig(localConfig.Nacos.DataId)
			if err != nil {
				logx.Errorf("Failed to get config from nacos: %v", err)
				logx.Info("Fallback to local configuration")
				// 回退到本地配置
				conf.MustLoad(*configFile, &localConfig)
			} else {
				// 解析 Nacos 配置
				if err := conf.LoadFromYamlBytes([]byte(configContent), &localConfig); err != nil {
					logx.Errorf("Failed to parse nacos config: %v", err)
					logx.Info("Fallback to local configuration")
					// 回退到本地配置
					conf.MustLoad(*configFile, &localConfig)
				} else {
					fmt.Println("Loaded configuration from Nacos successfully")

					// 启动配置监听
					if err := startConfigListener(nacosManager, &localConfig); err != nil {
						logx.Errorf("Failed to start config listener: %v", err)
					}
				}
			}
		}
	} else {
		// 使用本地配置
		conf.MustLoad(*configFile, &localConfig)
		fmt.Println("Loaded configuration from local file")
	}

	// 初始化服务上下文和业务逻辑
	initializeServices(localConfig)

	// 启动消费者
	go startConsumer()

	// 优雅退出
	ch := make(chan os.Signal, 1)
	signal.Notify(ch, syscall.SIGTERM, syscall.SIGINT)
	<-ch

	fmt.Println("Shutting down server...")

	// 取消配置监听
	if nacosManager != nil && localConfig.Nacos.DataId != "" {
		if err := nacosManager.CancelListenConfig(localConfig.Nacos.DataId); err != nil {
			logx.Errorf("Failed to cancel config listening: %v", err)
		}
	}

	// 关闭服务
	shutdownServices()

	fmt.Println("Server exited")
}

// initializeServices 初始化服务组件
func initializeServices(c config.Config) {
	ctxMutex.Lock()
	defer ctxMutex.Unlock()

	// 关闭旧的服务实例（如果存在）
	if serviceCtx != nil {
		shutdownServices()
	}

	// 创建新的服务实例
	serviceCtx = svc.NewServiceContext(c)
	consumeLogic = logic.NewConsumeLogic(context.Background(), serviceCtx)

	// 初始化重启通道
	restartChan = make(chan struct{}, 1)
}

// startConsumer 启动消费者
func startConsumer() {
	ctxMutex.RLock()
	logicInstance := consumeLogic
	ctxMutex.RUnlock()

	if logicInstance != nil {
		logicInstance.StartConsuming()
	}
}

// shutdownServices 关闭服务组件
func shutdownServices() {
	ctxMutex.Lock()
	defer ctxMutex.Unlock()

	if serviceCtx != nil {
		// 关闭 RabbitMQ 连接
		if serviceCtx.CommonChannel != nil {
			if err := serviceCtx.CommonChannel.Close(); err != nil {
				logx.Errorf("Error closing CommonChannel RabbitMQ connection: %v", err)
			}
		}
		if serviceCtx.RabbitMQ != nil {
			if err := serviceCtx.RabbitMQ.Close(); err != nil {
				logx.Errorf("Error closing RabbitMQ connection: %v", err)
			}
		}

		// 注销服务
		if serviceCtx.ServiceRegistry != nil {
			if err := serviceCtx.ServiceRegistry.Deregister(); err != nil {
				logx.Errorf("Failed to deregister service: %v", err)
			} else {
				fmt.Println("Service deregistered from Nacos")
			}
		}
	}

	// 关闭重启通道
	if restartChan != nil {
		close(restartChan)
		restartChan = nil
	}

	serviceCtx = nil
	consumeLogic = nil
}

// restartServices 重启服务
func restartServices(newConfig config.Config) {
	logx.Info("Restarting services with new configuration...")

	// 停止当前服务
	shutdownServices()

	// 使用新配置重新初始化服务
	initializeServices(newConfig)

	// 重新启动消费者
	go startConsumer()

	logx.Info("Services restarted successfully with new configuration")
}

// startConfigListener 启动配置监听
func startConfigListener(nacosManager *nacos.ConfigManager, currentConfig *config.Config) error {
	if nacosManager == nil || currentConfig.Nacos.DataId == "" {
		return fmt.Errorf("nacos manager or dataId is nil")
	}

	// 定义配置变化回调函数
	onConfigChange := func(newConfigContent string) {
		logx.Info("Received configuration change from Nacos")

		var newConfig config.Config
		if err := conf.LoadFromYamlBytes([]byte(newConfigContent), &newConfig); err != nil {
			logx.Errorf("Failed to parse updated nacos config: %v", err)
			return
		}

		logx.Infof("Configuration updated successfully, restarting services...")

		// 重启服务以应用新配置
		restartServices(newConfig)

		// 更新当前配置引用
		*currentConfig = newConfig
	}

	// 启动监听
	if err := nacosManager.ListenConfig(currentConfig.Nacos.DataId, onConfigChange); err != nil {
		return fmt.Errorf("failed to listen config changes: %v", err)
	}

	logx.Infof("Started listening for config changes, dataId: %s", currentConfig.Nacos.DataId)
	return nil
}

package app

import (
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/nacos"

	"github.com/zeromicro/go-zero/core/conf"
	"github.com/zeromicro/go-zero/core/logx"
)

// AppManager 应用管理器
type AppManager struct {
	configFile string
	useNacos   bool
	config     *config.Config
	nacosMgr   *nacos.ConfigManager
	lifecycle  *LifecycleManager
}

// NewAppManager 创建应用管理器
func NewAppManager(configFile string, useNacos bool) *AppManager {
	return &AppManager{
		configFile: configFile,
		useNacos:   useNacos,
		lifecycle:  NewLifecycleManager(),
	}
}

// Run 启动应用
func (am *AppManager) Run() error {
	// 加载配置
	if err := am.loadConfig(); err != nil {
		return fmt.Errorf("加载配置失败: %w", err)
	}

	// 初始化服务
	if err := am.lifecycle.InitializeServices(*am.config); err != nil {
		return fmt.Errorf("初始化服务失败: %w", err)
	}

	// 启动配置监听（如果使用 Nacos）
	if am.useNacos && am.nacosMgr != nil && am.config.Nacos.DataId != "" {
		if err := am.startConfigListener(); err != nil {
			logx.Errorf("启动配置监听失败: %v", err)
		}
	}

	// 启动消费者
	am.lifecycle.StartConsumer()

	// 等待退出信号
	am.waitForShutdown()

	return nil
}

// loadConfig 加载配置
func (am *AppManager) loadConfig() error {
	configLoader := NewConfigLoader(am.configFile, am.useNacos)

	config, nacosMgr, err := configLoader.Load()
	if err != nil {
		return err
	}

	am.config = config
	am.nacosMgr = nacosMgr
	return nil
}

// startConfigListener 启动配置监听
func (am *AppManager) startConfigListener() error {
	onConfigChange := func(newConfigContent string) {
		logx.Info("收到 Nacos 配置变更通知")

		var newConfig config.Config
		if err := conf.LoadFromYamlBytes([]byte(newConfigContent), &newConfig); err != nil {
			logx.Errorf("解析更新后的 Nacos 配置失败: %v", err)
			return
		}

		logx.Infof("配置更新成功，正在重启服务...")
		am.lifecycle.RestartServices(newConfig)

		// 更新当前配置
		am.config = &newConfig
	}

	if err := am.nacosMgr.ListenConfig(am.config.Nacos.DataId, onConfigChange); err != nil {
		return fmt.Errorf("监听配置变更失败: %w", err)
	}

	logx.Infof("已开始监听配置变更，数据ID: %s", am.config.Nacos.DataId)
	return nil
}

// waitForShutdown 等待关闭信号
func (am *AppManager) waitForShutdown() {
	signalHandler := NewSignalHandler()

	// 设置关闭回调
	signalHandler.SetShutdownCallback(func() {
		fmt.Println("正在关闭服务...")

		// 取消配置监听
		if am.nacosMgr != nil && am.config.Nacos.DataId != "" {
			if err := am.nacosMgr.CancelListenConfig(am.config.Nacos.DataId); err != nil {
				logx.Errorf("取消配置监听失败: %v", err)
			}
		}

		// 关闭服务
		am.lifecycle.ShutdownServices()
		fmt.Println("服务已退出")
	})

	// 等待信号
	signalHandler.WaitForSignal()
}

package app

import (
	"fmt"
	"judge-service/internal/config"
	"judge-service/internal/nacos"

	"github.com/zeromicro/go-zero/core/conf"
	"github.com/zeromicro/go-zero/core/logx"
)

// ConfigLoader 配置加载器
type ConfigLoader struct {
	configFile string
	useNacos   bool
}

// NewConfigLoader 创建配置加载器
func NewConfigLoader(configFile string, useNacos bool) *ConfigLoader {
	return &ConfigLoader{
		configFile: configFile,
		useNacos:   useNacos,
	}
}

// Load 加载配置
func (cl *ConfigLoader) Load() (*config.Config, *nacos.ConfigManager, error) {
	var localConfig config.Config

	if cl.useNacos {
		fmt.Println("正在使用 Nacos 配置中心启动应用...")

		config, manager, err := cl.loadFromNacos()
		if err != nil {
			logx.Errorf("从 Nacos 加载配置失败，回退到本地配置: %v", err)
			// 回退到本地配置
			conf.MustLoad(cl.configFile, &localConfig)
			return &localConfig, nil, nil
		}
		return config, manager, nil
	} else {
		// 使用本地配置
		conf.MustLoad(cl.configFile, &localConfig)
		fmt.Println("从本地文件加载配置")
		return &localConfig, nil, nil
	}
}

// loadFromNacos 从 Nacos 加载配置
func (cl *ConfigLoader) loadFromNacos() (*config.Config, *nacos.ConfigManager, error) {
	// 先加载本地配置获取 Nacos 连接信息
	var bootstrapConfig config.Config
	if err := conf.Load(cl.configFile, &bootstrapConfig); err != nil {
		return nil, nil, fmt.Errorf("加载本地配置文件失败: %w", err)
	}

	// 验证 Nacos 配置
	if err := cl.validateNacosConfig(bootstrapConfig.Nacos); err != nil {
		return nil, nil, err
	}

	// 初始化 Nacos 配置管理器
	nacosManager, err := nacos.NewNacosConfigManager(
		bootstrapConfig.Nacos.ServerAddr,
		bootstrapConfig.Nacos.Username,
		bootstrapConfig.Nacos.Password,
		bootstrapConfig.Nacos.Namespace,
		bootstrapConfig.Nacos.Group,
	)
	if err != nil {
		return nil, nil, fmt.Errorf("创建 Nacos 配置管理器失败: %w", err)
	}

	// 从 Nacos 获取配置
	configContent, err := nacosManager.GetConfig(bootstrapConfig.Nacos.DataId)
	if err != nil {
		return nil, nil, fmt.Errorf("从 Nacos 获取配置失败: %w", err)
	}

	// 解析 Nacos 配置
	var nacosConfig config.Config
	if err := conf.LoadFromYamlBytes([]byte(configContent), &nacosConfig); err != nil {
		return nil, nil, fmt.Errorf("解析 Nacos 配置失败: %w", err)
	}

	// 确保 Nacos 配置本身也包含正确的 Nacos 连接信息（用于后续监听）
	nacosConfig.Nacos = bootstrapConfig.Nacos

	fmt.Println("从 Nacos 成功加载配置")
	return &nacosConfig, nacosManager, nil
}

// validateNacosConfig 验证 Nacos 配置
func (cl *ConfigLoader) validateNacosConfig(nacosConfig config.NacosConfig) error {
	if nacosConfig.ServerAddr == "" {
		return fmt.Errorf("使用 nacos 时必须配置 Nacos 服务器地址")
	}

	if nacosConfig.DataId == "" {
		return fmt.Errorf("使用 nacos 时必须配置 Nacos dataId")
	}

	return nil
}

// LoadLocalConfig 加载本地配置（用于回退）
func (cl *ConfigLoader) LoadLocalConfig() (*config.Config, error) {
	var localConfig config.Config
	if err := conf.Load(cl.configFile, &localConfig); err != nil {
		return nil, fmt.Errorf("加载本地配置文件失败: %w", err)
	}
	return &localConfig, nil
}

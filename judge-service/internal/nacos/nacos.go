package nacos

import (
	"fmt"
	"strings"

	"github.com/nacos-group/nacos-sdk-go/v2/clients"
	"github.com/nacos-group/nacos-sdk-go/v2/clients/config_client"
	"github.com/nacos-group/nacos-sdk-go/v2/common/constant"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"github.com/zeromicro/go-zero/core/logx"
)

type ConfigManager struct {
	client config_client.IConfigClient
	group  string
}

func NewNacosConfigManager(serverAddr, username, password, namespace, group string) (*ConfigManager, error) {
	if serverAddr == "" {
		return nil, fmt.Errorf("Nacos服务器地址不能为空")
	}

	// 解析服务器地址，处理端口
	serverConfigs, err := parseServerConfigs(serverAddr)
	if err != nil {
		return nil, err
	}

	// 创建 clientConfig
	clientConfig := constant.ClientConfig{
		NamespaceId:         namespace,
		TimeoutMs:           10000, // 增加超时时间
		NotLoadCacheAtStart: true,
		LogDir:              "/tmp/nacos/log",
		CacheDir:            "/tmp/nacos/cache",
		LogLevel:            "error", // 降低日志级别
		Username:            username,
		Password:            password,
	}

	// 如果用户名和密码都为空，则不设置认证
	if username == "" && password == "" {
		clientConfig.Username = ""
		clientConfig.Password = ""
	}

	// 创建配置客户端
	configClient, err := clients.NewConfigClient(
		vo.NacosClientParam{
			ClientConfig:  &clientConfig,
			ServerConfigs: serverConfigs,
		},
	)
	if err != nil {
		return nil, fmt.Errorf("创建Nacos配置客户端失败: %v", err)
	}

	return &ConfigManager{
		client: configClient,
		group:  group,
	}, nil
}

// parseServerConfigs 解析服务器地址
func parseServerConfigs(serverAddr string) ([]constant.ServerConfig, error) {
	var serverConfigs []constant.ServerConfig

	// 支持多个服务器地址（逗号分隔）
	addrs := strings.Split(serverAddr, ",")
	for _, addr := range addrs {
		addr = strings.TrimSpace(addr)
		if addr == "" {
			continue
		}

		// 解析主机和端口
		var host string
		var port uint64 = 8848 // 默认端口

		if strings.Contains(addr, ":") {
			parts := strings.Split(addr, ":")
			if len(parts) != 2 {
				return nil, fmt.Errorf("服务器地址格式无效: %s", addr)
			}
			host = parts[0]
			// 这里可以解析端口，但 Nacos SDK 通常使用默认端口
			// portStr := parts[1]
			// 如果需要自定义端口，可以在这里解析
		} else {
			host = addr
		}

		serverConfigs = append(serverConfigs, constant.ServerConfig{
			IpAddr:      host,
			Port:        port,
			ContextPath: "/nacos",
			Scheme:      "http", // 明确指定协议
		})
	}

	if len(serverConfigs) == 0 {
		return nil, fmt.Errorf("未找到有效的服务器地址")
	}

	return serverConfigs, nil
}

// GetConfig 从 Nacos 获取配置
func (m *ConfigManager) GetConfig(dataId string) (string, error) {
	if dataId == "" {
		return "", fmt.Errorf("dataId不能为空")
	}

	content, err := m.client.GetConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  m.group,
	})
	if err != nil {
		return "", fmt.Errorf("从Nacos获取配置失败: %v", err)
	}

	if content == "" {
		return "", fmt.Errorf("配置内容为空, dataId: %s, group: %s", dataId, m.group)
	}

	return content, nil
}

// CheckConfigExists 检查配置是否存在
func (m *ConfigManager) CheckConfigExists(dataId string) (bool, error) {
	content, err := m.client.GetConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  m.group,
	})
	if err != nil {
		return false, err
	}
	return content != "", nil
}

func (m *ConfigManager) ListenConfig(dataId string, onConfigChange func(string)) error {
	err := m.client.ListenConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  m.group,
		OnChange: func(namespace, group, dataId, data string) {
			logx.Infof("配置已更新：命名空间=%s，组别=%s，数据ID=%s", namespace, group, dataId)
			onConfigChange(data)
		},
	})
	if err != nil {
		return fmt.Errorf("监听配置失败: %v", err)
	}
	logx.Infof("开始监听配置变更，数据ID：%s，组别：%s", dataId, m.group)
	return nil
}

// CancelListenConfig 取消监听配置
func (m *ConfigManager) CancelListenConfig(dataId string) error {
	err := m.client.CancelListenConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  m.group,
	})
	if err != nil {
		return fmt.Errorf("取消监听配置失败: %v", err)
	}
	logx.Infof("已取消监听配置变更，数据ID：%s，组别：%s", dataId, m.group)
	return nil
}

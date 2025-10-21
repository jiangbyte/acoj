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
		return nil, fmt.Errorf("nacos server address is required")
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
		LogLevel:            "info", // 降低日志级别
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
		return nil, fmt.Errorf("failed to create nacos config client: %v", err)
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
				return nil, fmt.Errorf("invalid server address format: %s", addr)
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
		return nil, fmt.Errorf("no valid server addresses found")
	}

	return serverConfigs, nil
}

// GetConfig 从 Nacos 获取配置
func (m *ConfigManager) GetConfig(dataId string) (string, error) {
	if dataId == "" {
		return "", fmt.Errorf("dataId is required")
	}

	content, err := m.client.GetConfig(vo.ConfigParam{
		DataId: dataId,
		Group:  m.group,
	})
	if err != nil {
		return "", fmt.Errorf("failed to get config from nacos: %v", err)
	}

	if content == "" {
		return "", fmt.Errorf("config content is empty, dataId: %s, group: %s", dataId, m.group)
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
// 	return m.client.ListenConfig(vo.ConfigParam{
// 		DataId: dataId,
// 		Group:  m.group,
// 		OnChange: func(namespace, group, dataId, data string) {
// 			logx.Infof("Config changed: namespace=%s, group=%s, dataId=%s", namespace, group, dataId)
// 			onConfigChange(data)
// 		},
// 	})

 err := m.client.ListenConfig(vo.ConfigParam{
        DataId: dataId,
        Group:  m.group,
        OnChange: func(namespace, group, dataId, data string) {
            logx.Infof("Config changed: namespace=%s, group=%s, dataId=%s", namespace, group, dataId)
            onConfigChange(data)
        },
    })
    if err != nil {
        return fmt.Errorf("failed to listen config: %v", err)
    }
    logx.Infof("Started listening config changes, dataId: %s, group: %s", dataId, m.group)
    return nil
}

// CancelListenConfig 取消监听配置
func (m *ConfigManager) CancelListenConfig(dataId string) error {
    err := m.client.CancelListenConfig(vo.ConfigParam{
        DataId: dataId,
        Group:  m.group,
    })
    if err != nil {
        return fmt.Errorf("failed to cancel listen config: %v", err)
    }
    logx.Infof("Canceled listening config changes, dataId: %s, group: %s", dataId, m.group)
    return nil
}
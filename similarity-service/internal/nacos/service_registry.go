package nacos

import (
	"judge-service/internal/config"

	"github.com/zeromicro/go-zero/core/logx"
)

type ServiceRegistryManager struct {
	registry *ServiceRegistry
	enabled  bool
}

func NewServiceRegistryManager(nacosConfig config.NacosConfig, appConfig config.Config) *ServiceRegistryManager {
	if nacosConfig.ServerAddr == "" {
		return &ServiceRegistryManager{enabled: false}
	}

	registry, err := NewServiceRegistry(nacosConfig, appConfig)
	if err != nil {
		logx.Errorf("创建服务注册器失败: %v", err)
		return &ServiceRegistryManager{enabled: false}
	}

	return &ServiceRegistryManager{
		registry: registry,
		enabled:  true,
	}
}

func (s *ServiceRegistryManager) Register() error {
	if !s.enabled || s.registry == nil {
		return nil
	}

	if err := s.registry.Register(); err != nil {
		logx.Errorf("服务注册失败: %v", err)
		return err
	}

	logx.Info("服务成功注册到 Nacos")

	// 启动健康检查
	go s.registry.HealthCheck()

	return nil
}

func (s *ServiceRegistryManager) Deregister() error {
	if !s.enabled || s.registry == nil {
		return nil
	}
	return s.registry.Deregister()
}

func (s *ServiceRegistryManager) IsEnabled() bool {
	return s.enabled
}

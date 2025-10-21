package nacos

import (
	"fmt"
	"judge-service/internal/config"
	"strconv"
	"time"

	"github.com/nacos-group/nacos-sdk-go/v2/clients"
	"github.com/nacos-group/nacos-sdk-go/v2/clients/naming_client"
	"github.com/nacos-group/nacos-sdk-go/v2/common/constant"
	"github.com/nacos-group/nacos-sdk-go/v2/model"
	"github.com/nacos-group/nacos-sdk-go/v2/vo"
	"github.com/zeromicro/go-zero/core/logx"
)

// ServiceInstance 服务实例信息
type ServiceInstance struct {
	InstanceId  string            `json:"instanceId"`
	Ip          string            `json:"ip"`
	Port        uint64            `json:"port"`
	ServiceName string            `json:"serviceName"`
	Weight      float64           `json:"weight"`
	Healthy     bool              `json:"healthy"`
	Enabled     bool              `json:"enabled"`
	Metadata    map[string]string `json:"metadata"`
	ClusterName string            `json:"clusterName"`
}

// ServiceRegistry 服务注册器
type ServiceRegistry struct {
	client      naming_client.INamingClient
	group       string
	serviceName string
	ip          string
	port        uint64
	metadata    map[string]string
	clusterName string
	instanceId  string
}

// NewServiceRegistry 创建服务注册器
func NewServiceRegistry(nacosConfig config.NacosConfig, serviceConfig config.Config) (*ServiceRegistry, error) {
	if nacosConfig.ServerAddr == "" {
		return nil, fmt.Errorf("nacos server address is required")
	}

	// 解析服务器地址
	serverConfigs, err := parseServerConfigs(nacosConfig.ServerAddr)
	if err != nil {
		return nil, err
	}

	// 创建 clientConfig
	clientConfig := constant.ClientConfig{
		NamespaceId:         nacosConfig.Namespace,
		TimeoutMs:           10000,
		NotLoadCacheAtStart: true,
		LogDir:              "/tmp/nacos/log",
		CacheDir:            "/tmp/nacos/cache",
		LogLevel:            "info",
		Username:            nacosConfig.Username,
		Password:            nacosConfig.Password,
	}

	// 如果用户名和密码都为空，则不设置认证
	if nacosConfig.Username == "" && nacosConfig.Password == "" {
		clientConfig.Username = ""
		clientConfig.Password = ""
	}

	// 创建命名客户端
	namingClient, err := clients.NewNamingClient(
		vo.NacosClientParam{
			ClientConfig:  &clientConfig,
			ServerConfigs: serverConfigs,
		},
	)
	if err != nil {
		return nil, fmt.Errorf("failed to create nacos naming client: %v", err)
	}

	// 解析服务端口
	port, err := strconv.ParseUint(strconv.Itoa(serviceConfig.Port), 10, 64)
	if err != nil {
		return nil, fmt.Errorf("invalid port: %v", err)
	}

	// 获取本机IP（这里简化处理，实际生产环境可能需要更复杂的IP获取逻辑）
	ip := "0.0.0.0"
	if serviceConfig.Host != "0.0.0.0" && serviceConfig.Host != "" {
		ip = serviceConfig.Host
	}

	// 生成实例ID
	instanceId := fmt.Sprintf("%s-%s-%d", serviceConfig.Name, ip, port)

	return &ServiceRegistry{
		client:      namingClient,
		group:       nacosConfig.Group,
		serviceName: serviceConfig.Name,
		ip:          ip,
		port:        port,
		metadata: map[string]string{
			"version":   "1.0.0",
			"workspace": serviceConfig.Workspace,
			"port":      strconv.Itoa(serviceConfig.Port),
			"startTime": time.Now().Format(time.RFC3339),
		},
		clusterName: "DEFAULT",
		instanceId:  instanceId,
	}, nil
}

// Register 注册服务
func (r *ServiceRegistry) Register() error {
	param := vo.RegisterInstanceParam{
		Ip:          r.ip,
		Port:        r.port,
		ServiceName: r.serviceName,
		Weight:      10,
		Enable:      true,
		Healthy:     true,
		Ephemeral:   true,
		Metadata:    r.metadata,
		ClusterName: r.clusterName,
		GroupName:   r.group,
	}

	success, err := r.client.RegisterInstance(param)
	if err != nil {
		return fmt.Errorf("failed to register service: %v", err)
	}

	if !success {
		return fmt.Errorf("service registration failed")
	}

	logx.Infof("Service registered successfully: %s:%d, instanceId: %s", r.ip, r.port, r.instanceId)
	return nil
}

// Deregister 注销服务
func (r *ServiceRegistry) Deregister() error {
	param := vo.DeregisterInstanceParam{
		Ip:          r.ip,
		Port:        r.port,
		ServiceName: r.serviceName,
		Cluster:     r.clusterName,
		GroupName:   r.group,
		Ephemeral:   true,
	}

	success, err := r.client.DeregisterInstance(param)
	if err != nil {
		return fmt.Errorf("failed to deregister service: %v", err)
	}

	if !success {
		return fmt.Errorf("service deregistration failed")
	}

	logx.Infof("Service deregistered successfully: %s:%d", r.ip, r.port)
	return nil
}

// SetMetadata 设置元数据
func (r *ServiceRegistry) SetMetadata(key, value string) {
	r.metadata[key] = value
}

// GetServiceInstances 获取服务实例列表
func (r *ServiceRegistry) GetServiceInstances(serviceName string) ([]ServiceInstance, error) {
	instances, err := r.client.SelectInstances(vo.SelectInstancesParam{
		ServiceName: serviceName,
		GroupName:   r.group,
		Clusters:    []string{r.clusterName},
		HealthyOnly: true,
	})
	if err != nil {
		return nil, fmt.Errorf("failed to get service instances: %v", err)
	}

	var result []ServiceInstance
	for _, instance := range instances {
		result = append(result, ServiceInstance{
			InstanceId:  instance.InstanceId,
			Ip:          instance.Ip,
			Port:        instance.Port,
			ServiceName: instance.ServiceName,
			Weight:      instance.Weight,
			Healthy:     instance.Healthy,
			Enabled:     instance.Enable,
			Metadata:    instance.Metadata,
			ClusterName: instance.ClusterName,
		})
	}
	return result, nil
}

// Subscribe 订阅服务变化
func (r *ServiceRegistry) Subscribe(serviceName string, callback func([]ServiceInstance)) error {
	err := r.client.Subscribe(&vo.SubscribeParam{
		ServiceName: serviceName,
		GroupName:   r.group,
		Clusters:    []string{r.clusterName},
		SubscribeCallback: func(services []model.Instance, err error) {
			if err != nil {
				logx.Errorf("Service subscription callback error: %v", err)
				return
			}

			var instances []ServiceInstance
			for _, service := range services {
				instances = append(instances, ServiceInstance{
					InstanceId:  service.InstanceId,
					Ip:          service.Ip,
					Port:        service.Port,
					ServiceName: service.ServiceName,
					Weight:      service.Weight,
					Healthy:     service.Healthy,
					Enabled:     service.Enable,
					Metadata:    service.Metadata,
					ClusterName: service.ClusterName,
				})
			}
			callback(instances)
		},
	})
	if err != nil {
		return fmt.Errorf("failed to subscribe service: %v", err)
	}
	return nil
}

// Unsubscribe 取消订阅
func (r *ServiceRegistry) Unsubscribe(serviceName string) error {
	err := r.client.Unsubscribe(&vo.SubscribeParam{
		ServiceName: serviceName,
		GroupName:   r.group,
		Clusters:    []string{r.clusterName},
	})
	if err != nil {
		return fmt.Errorf("failed to unsubscribe service: %v", err)
	}
	return nil
}

// HealthCheck 健康检查
func (r *ServiceRegistry) HealthCheck() {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()

	for range ticker.C {
		// 发送心跳，更新服务状态
		_, err := r.client.GetService(vo.GetServiceParam{
			ServiceName: r.serviceName,
			GroupName:   r.group,
		})
		if err != nil {
			logx.Errorf("Health check failed: %v", err)
			// 尝试重新注册
			if err := r.Register(); err != nil {
				logx.Errorf("Failed to re-register service: %v", err)
			} else {
				logx.Info("Service re-registered successfully after health check failure")
			}
		} else {
			logx.Debugf("Service health check passed: %s", r.serviceName)
		}
	}
}

// GetServiceInfo 获取服务信息
func (r *ServiceRegistry) GetServiceInfo() ServiceInstance {
	return ServiceInstance{
		InstanceId:  r.instanceId,
		Ip:          r.ip,
		Port:        r.port,
		ServiceName: r.serviceName,
		Weight:      10,
		Healthy:     true,
		Enabled:     true,
		Metadata:    r.metadata,
		ClusterName: r.clusterName,
	}
}

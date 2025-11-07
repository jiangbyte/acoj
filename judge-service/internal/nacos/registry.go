package nacos

import (
	"fmt"
	"judge-service/internal/config"
	"net"
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
		return nil, fmt.Errorf("Nacos服务器地址不能为空")
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
		LogLevel:            "error",
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
		return nil, fmt.Errorf("创建Nacos命名客户端失败: %v", err)
	}

	// 解析服务端口
	port, err := strconv.ParseUint(strconv.Itoa(serviceConfig.Port), 10, 64)
	if err != nil {
		return nil, fmt.Errorf("端口号无效: %v", err)
	}

	// 获取自动检测的IP
	autoIP, err := getLocalIP()
	if err != nil {
		logx.Errorf("自动获取IP失败: %v，使用默认配置", err)
		autoIP = "0.0.0.0"
	}

	// 确定最终使用的IP
	finalIP := autoIP
	if serviceConfig.Host != "0.0.0.0" && serviceConfig.Host != "" {
		finalIP = serviceConfig.Host
		logx.Infof("使用配置的IP: %s (自动检测的IP: %s)", finalIP, autoIP)
	} else {
		logx.Infof("使用自动检测的IP: %s", finalIP)
	}

	logx.Infof("服务注册 IP: %s", finalIP)

	// 生成实例ID
	instanceId := fmt.Sprintf("%s-%s-%d", serviceConfig.Name, finalIP, port)

	return &ServiceRegistry{
		client:      namingClient,
		group:       nacosConfig.Group,
		serviceName: serviceConfig.Name,
		ip:          finalIP,
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

// 获取本机第一个非回环的IPv4地址
func getLocalIP() (string, error) {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		return "", err
	}

	for _, addr := range addrs {
		if ipNet, ok := addr.(*net.IPNet); ok && !ipNet.IP.IsLoopback() {
			if ipNet.IP.To4() != nil {
				return ipNet.IP.String(), nil
			}
		}
	}
	return "", fmt.Errorf("不能获取 IP 地址")
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
		return fmt.Errorf("服务注册失败: %v", err)
	}

	if !success {
		return fmt.Errorf("服务注册未成功")
	}

	logx.Infof("服务注册成功: %s:%d, 实例ID: %s", r.ip, r.port, r.instanceId)
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
		return fmt.Errorf("服务注销失败: %v", err)
	}

	if !success {
		return fmt.Errorf("服务注销未成功")
	}

	logx.Infof("服务注销成功: %s:%d", r.ip, r.port)
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
		return nil, fmt.Errorf("获取服务实例列表失败: %v", err)
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
				logx.Errorf("服务订阅回调错误: %v", err)
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
		return fmt.Errorf("服务订阅失败: %v", err)
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
		return fmt.Errorf("取消服务订阅失败: %v", err)
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
			logx.Errorf("健康检查失败: %v", err)
			// 尝试重新注册
			if err := r.Register(); err != nil {
				logx.Errorf("健康检查失败后重新注册服务失败: %v", err)
			} else {
				logx.Info("健康检查失败后服务重新注册成功")
			}
		} else {
			logx.Debugf("服务健康检查通过: %s", r.serviceName)
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

package app

import (
	"context"
	"similarity-service/internal/config"
	"similarity-service/internal/logic"
	"similarity-service/internal/svc"
	"sync"

	"github.com/zeromicro/go-zero/core/logx"
)

// LifecycleManager 服务生命周期管理器
type LifecycleManager struct {
	serviceCtx   *svc.ServiceContext
	consumeLogic *logic.ConsumeLogic
	mutex        sync.RWMutex
	restartChan  chan struct{}
}

// NewLifecycleManager 创建生命周期管理器
func NewLifecycleManager() *LifecycleManager {
	return &LifecycleManager{
		restartChan: make(chan struct{}, 1),
	}
}

// InitializeServices 初始化服务组件
func (lm *LifecycleManager) InitializeServices(c config.Config) error {
	lm.mutex.Lock()
	defer lm.mutex.Unlock()

	// 关闭旧的服务实例（如果存在）
	if lm.serviceCtx != nil {
		lm.shutdownServices()
	}

	// 创建新的服务实例
	lm.serviceCtx = svc.NewServiceContext(c)
	lm.consumeLogic = logic.NewConsumeLogic(context.Background(), lm.serviceCtx)

	return nil
}

// StartConsumer 启动消费者
func (lm *LifecycleManager) StartConsumer() {
	lm.mutex.RLock()
	logicInstance := lm.consumeLogic
	lm.mutex.RUnlock()

	if logicInstance != nil {
		go logicInstance.StartConsuming()
	}
}

// ShutdownServices 关闭服务组件
func (lm *LifecycleManager) ShutdownServices() {
	lm.mutex.Lock()
	defer lm.mutex.Unlock()

	lm.shutdownServices()
}

// RestartServices 重启服务
func (lm *LifecycleManager) RestartServices(newConfig config.Config) {
	logx.Info("正在使用新配置重启服务...")

	// 停止当前服务
	lm.ShutdownServices()

	// 使用新配置重新初始化服务
	if err := lm.InitializeServices(newConfig); err != nil {
		logx.Errorf("重新初始化服务失败: %v", err)
		return
	}

	// 重新启动消费者
	lm.StartConsumer()

	logx.Info("服务使用新配置重启成功")
}

// shutdownServices 内部关闭服务方法
func (lm *LifecycleManager) shutdownServices() {
	if lm.serviceCtx != nil {
		// 关闭 RabbitMQ 连接
		if lm.serviceCtx.CommonChannel() != nil {
			if err := lm.serviceCtx.CommonChannel().Close(); err != nil {
				logx.Errorf("关闭 CommonChannel RabbitMQ 连接失败: %v", err)
			}
		}
		if lm.serviceCtx.RabbitMQ() != nil {
			if err := lm.serviceCtx.RabbitMQ().Close(); err != nil {
				logx.Errorf("关闭 RabbitMQ 连接失败: %v", err)
			}
		}

		// 注销服务
		if lm.serviceCtx.Initializer.GetServiceReRegistry() != nil {
			if err := lm.serviceCtx.Initializer.GetServiceReRegistry().Deregister(); err != nil {
				logx.Errorf("服务注销失败: %v", err)
			} else {
				logx.Info("服务已从 Nacos 注销")
			}
		}
	}

	// 关闭重启通道
	if lm.restartChan != nil {
		close(lm.restartChan)
		lm.restartChan = nil
	}

	lm.serviceCtx = nil
	lm.consumeLogic = nil
}

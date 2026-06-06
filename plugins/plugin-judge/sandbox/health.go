package sandbox

import (
	"context"
	"log"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/plugins/plugin-judge/judgetypes"
)

// HealthChecker 定期健康检查
type HealthChecker struct {
	interval         time.Duration
	maxRetry         int
	recoveryInterval time.Duration
	stopCh           chan struct{}
}

// NewHealthChecker 创建健康检查器
func NewHealthChecker(intervalSec, maxRetry, recoveryIntervalSec int) *HealthChecker {
	return &HealthChecker{
		interval:         time.Duration(intervalSec) * time.Second,
		maxRetry:         maxRetry,
		recoveryInterval: time.Duration(recoveryIntervalSec) * time.Second,
		stopCh:           make(chan struct{}),
	}
}

// Start 启动健康检查循环
func (hc *HealthChecker) Start() {
	go hc.loop()
}

// Stop 停止健康检查
func (hc *HealthChecker) Stop() {
	close(hc.stopCh)
}

func (hc *HealthChecker) loop() {
	ticker := time.NewTicker(hc.interval)
	defer ticker.Stop()

	hc.checkAll()

	for {
		select {
		case <-ticker.C:
			hc.checkAll()
		case <-hc.stopCh:
			return
		}
	}
}

func (hc *HealthChecker) checkAll() {
	var instances []JudgeSandbox
	ctx := context.Background()
	if err := db.DB.WithContext(ctx).
		Where("status IN ?", []string{"active", "offline"}).
		Find(&instances).Error; err != nil {
		log.Printf("[sandbox] health check query error: %v", err)
		return
	}

	var activeBackends []judgetypes.SandboxBackend

	for _, inst := range instances {
		if inst.Status == "offline" {
			if inst.UpdatedAt != nil {
				since := time.Since(*inst.UpdatedAt)
				if since < hc.recoveryInterval {
					continue
				}
			}
		}

		backend, err := NewBackend(inst.Endpoint, inst.Timeout)
		if err != nil {
			log.Printf("[sandbox] failed to create backend %s (%s): %v", inst.Name, inst.Endpoint, err)
			if inst.Status == "active" {
				db.DB.WithContext(ctx).Model(&JudgeSandbox{}).
					Where("id = ?", inst.ID).
					Updates(map[string]any{
						"status":     "offline",
						"updated_at": time.Now(),
					})
			}
			continue
		}

		health := backend.Health()
		if health != nil && health.Alive {
			log.Printf("[sandbox] backend %s (%s) is alive", inst.Name, inst.Endpoint)
			activeBackends = append(activeBackends, backend)
			if inst.Status != "active" {
				db.DB.WithContext(ctx).Model(&inst).Update("status", "active")
			}
		} else {
			errMsg := ""
			if health != nil {
				errMsg = health.Error
			}
			log.Printf("[sandbox] backend %s (%s) is unhealthy: %s", inst.Name, inst.Endpoint, errMsg)
			if inst.Status == "active" {
				db.DB.WithContext(ctx).Model(&JudgeSandbox{}).
					Where("id = ?", inst.ID).
					Updates(map[string]any{
						"status":     "offline",
						"updated_at": time.Now(),
					})
			}
		}
	}

	DefaultPool.ReplaceAll(activeBackends)
}

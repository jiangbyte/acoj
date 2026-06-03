package registry

import (
	"context"
	"log"
	"sync"
	"time"

	"gorm.io/gorm"

	"hei-gin/config"
	"hei-gin/core/db"
	sandboxModel "hei-gin/modules/judge/sandbox/model"
)

type Registry struct {
	mu        sync.RWMutex
	pool      []sandboxModel.JudgeSandboxInstance
	prober   *Prober
	scheduler *Scheduler
	stopCh    chan struct{}
}

var globalRegistry *Registry
var once sync.Once

func Global() *Registry {
	once.Do(func() {
		globalRegistry = &Registry{stopCh: make(chan struct{})}
	})
	return globalRegistry
}

func (r *Registry) Start() {
	log.Println("[Judge Registry] Starting...")
	r.Refresh()
	r.scheduler = NewScheduler(r)
	r.scheduler.Start()
	r.prober = NewProber(r)
	r.prober.Start()
	log.Println("[Judge Registry] Started successfully")
}

func (r *Registry) Stop() {
	log.Println("[Judge Registry] Stopping...")
	close(r.stopCh)
	if r.prober != nil {
		r.prober.Stop()
	}
	if r.scheduler != nil {
		r.scheduler.Stop()
	}
	log.Println("[Judge Registry] Stopped")
}

func (r *Registry) Register(inst *sandboxModel.JudgeSandboxInstance) error {
	ctx := context.Background()
	var existing sandboxModel.JudgeSandboxInstance
	err := db.DB.WithContext(ctx).Where("addr = ?", inst.Addr).First(&existing).Error

	if err == gorm.ErrRecordNotFound {
		inst.Status = sandboxModel.StatusActive
		inst.LastHeartbeat = time.Now()
		if err := db.DB.WithContext(ctx).Create(inst).Error; err != nil {
			return err
		}
		log.Printf("[Judge Registry] Registered sandbox %s (%s)", inst.ID, inst.Addr)
	} else if err == nil {
		now := time.Now()
		updates := map[string]interface{}{
			"status":         sandboxModel.StatusActive,
			"version":        inst.Version,
			"cpu_cores":      inst.CPUCores,
			"memory_total":   inst.MemoryTotal,
			"weight":         inst.Weight,
			"last_heartbeat": now,
			"failure_count":  0,
			"updated_at":     now,
		}
		db.DB.WithContext(ctx).Model(&sandboxModel.JudgeSandboxInstance{}).Where("addr = ?", inst.Addr).Updates(updates)
		inst.ID = existing.ID
		log.Printf("[Judge Registry] Reactivated sandbox %s (%s)", existing.ID, inst.Addr)
	} else {
		return err
	}
	r.Refresh()
	return nil
}

func (r *Registry) Heartbeat(addr string) error {
	ctx := context.Background()
	now := time.Now()
	result := db.DB.WithContext(ctx).Model(&sandboxModel.JudgeSandboxInstance{}).
		Where("addr = ?", addr).
		Updates(map[string]interface{}{
			"last_heartbeat": now,
			"failure_count":  0,
			"status":         sandboxModel.StatusActive,
			"updated_at":     now,
		})
	if result.RowsAffected == 0 {
		log.Printf("[Judge Registry] Heartbeat for unknown sandbox %s", addr)
	}
	return result.Error
}

func (r *Registry) Unregister(addr string) error {
	ctx := context.Background()
	now := time.Now()
	result := db.DB.WithContext(ctx).Model(&sandboxModel.JudgeSandboxInstance{}).
		Where("addr = ?", addr).
		Updates(map[string]interface{}{"status": sandboxModel.StatusRemoved, "updated_at": now})
	if result.RowsAffected > 0 {
		log.Printf("[Judge Registry] Unregistered sandbox %s", addr)
	}
	r.Refresh()
	return result.Error
}

func (r *Registry) Refresh() {
	var instances []sandboxModel.JudgeSandboxInstance
	db.DB.Where("status = ?", sandboxModel.StatusActive).Order("weight DESC").Find(&instances)
	r.mu.Lock()
	r.pool = instances
	r.mu.Unlock()
	if r.scheduler != nil {
		r.scheduler.Refresh()
	}
}

func (r *Registry) GetPool() []sandboxModel.JudgeSandboxInstance {
	r.mu.RLock()
	defer r.mu.RUnlock()
	cp := make([]sandboxModel.JudgeSandboxInstance, len(r.pool))
	copy(cp, r.pool)
	return cp
}

// UpdateHeartbeat refreshes the heartbeat timestamp for a sandbox instance.
func (r *Registry) UpdateHeartbeat(addr string) {
	ctx := context.Background()
	now := time.Now()
	db.DB.WithContext(ctx).Model(&sandboxModel.JudgeSandboxInstance{}).
		Where("addr = ?", addr).
		Updates(map[string]interface{}{
			"last_heartbeat": now,
			"failure_count":  0,
			"updated_at":     now,
		})
}

func (r *Registry) PoolSize() int {
	r.mu.RLock()
	defer r.mu.RUnlock()
	return len(r.pool)
}

func (r *Registry) MarkUnhealthy(addr string) {
	ctx := context.Background()
	now := time.Now()

	var inst sandboxModel.JudgeSandboxInstance
	if err := db.DB.WithContext(ctx).Where("addr = ?", addr).First(&inst).Error; err != nil {
		return
	}

	failureCount := inst.FailureCount + 1
	status := sandboxModel.StatusActive
	if failureCount >= config.C.Judge.Registry.MaxFailures {
		status = sandboxModel.StatusUnhealthy
		log.Printf("[Judge Registry] Sandbox %s (%s) marked UNHEALTHY (failures=%d)", inst.ID, addr, failureCount)
	}

	db.DB.WithContext(ctx).Model(&sandboxModel.JudgeSandboxInstance{}).
		Where("addr = ?", addr).
		Updates(map[string]interface{}{
			"status":        status,
			"failure_count": failureCount,
			"updated_at":    now,
		})
	r.Refresh()
}

func (r *Registry) RemoveStale() {
	ctx := context.Background()
	removeAfter, err := time.ParseDuration(config.C.Judge.Registry.RemoveAfter)
	if err != nil {
		removeAfter = 120 * time.Second
	}
	cutoff := time.Now().Add(-removeAfter)

	var stale []sandboxModel.JudgeSandboxInstance
	db.DB.WithContext(ctx).
		Where("status IN ? AND last_heartbeat < ?", []string{sandboxModel.StatusActive, sandboxModel.StatusUnhealthy}, cutoff).
		Find(&stale)

	for _, s := range stale {
		log.Printf("[Judge Registry] Removing stale sandbox %s (%s) - last heartbeat %v", s.ID, s.Addr, s.LastHeartbeat)
	}

	db.DB.WithContext(ctx).Model(&sandboxModel.JudgeSandboxInstance{}).
		Where("status IN ? AND last_heartbeat < ?", []string{sandboxModel.StatusActive, sandboxModel.StatusUnhealthy}, cutoff).
		Update("status", sandboxModel.StatusRemoved)

	if len(stale) > 0 {
		r.Refresh()
	}
}

func (r *Registry) CheckHeartbeatTimeout() {
	ctx := context.Background()
	heartbeatTimeout, err := time.ParseDuration(config.C.Judge.Registry.HeartbeatTimeout)
	if err != nil {
		heartbeatTimeout = 30 * time.Second
	}
	cutoff := time.Now().Add(-heartbeatTimeout)

	result := db.DB.WithContext(ctx).Model(&sandboxModel.JudgeSandboxInstance{}).
		Where("status = ? AND last_heartbeat < ?", sandboxModel.StatusActive, cutoff).
		Update("status", sandboxModel.StatusUnhealthy)

	if result.RowsAffected > 0 {
		log.Printf("[Judge Registry] Marked %d instances UNHEALTHY due to heartbeat timeout", result.RowsAffected)
		r.Refresh()
	}
}

func (r *Registry) Scheduler() *Scheduler {
	return r.scheduler
}

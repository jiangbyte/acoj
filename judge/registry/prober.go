package registry

import (
	"context"
	"log"
	"sync"
	"time"

	"hei-gin/config"
	"hei-gin/core/db"
	sandboxModel "hei-gin/modules/judge/sandbox/model"
)

// Prober periodically probes all sandbox instances for health.
type Prober struct {
	registry *Registry
	stopCh   chan struct{}
	wg       sync.WaitGroup
}

// NewProber creates a new Prober.
func NewProber(r *Registry) *Prober {
	return &Prober{
		registry: r,
		stopCh:   make(chan struct{}),
	}
}

// Start begins the health probe loop.
func (p *Prober) Start() {
	interval, err := time.ParseDuration(config.C.Judge.Registry.ProbeInterval)
	if err != nil {
		interval = 10 * time.Second
	}

	p.wg.Add(1)
	go func() {
		defer p.wg.Done()
		p.probeAll()

		ticker := time.NewTicker(interval)
		defer ticker.Stop()

		for {
			select {
			case <-ticker.C:
				p.probeAll()
				p.registry.CheckHeartbeatTimeout()
				p.registry.RemoveStale()
			case <-p.stopCh:
				return
			}
		}
	}()
	log.Printf("[Prober] Started (interval=%v)", interval)
}

// Stop stops the prober.
func (p *Prober) Stop() {
	close(p.stopCh)
	p.wg.Wait()
	log.Println("[Prober] Stopped")
}

// probeAll probes all known instances.
func (p *Prober) probeAll() {
	// Probe active pool instances
	instances := p.registry.GetPool()
	for _, inst := range instances {
		p.probeInstance(inst.Addr)
	}

	// Also probe non-ACTIVE instances from DB to support auto-recovery
	p.probeInactiveInstances()
}

// probeInactiveInstances loads UNHEALTHY and REMOVED instances from DB
// and attempts to reconnect.
func (p *Prober) probeInactiveInstances() {
	ctx := context.Background()
	var inactive []sandboxModel.JudgeSandboxInstance
	db.DB.WithContext(ctx).
		Where("status IN ?", []string{sandboxModel.StatusUnhealthy, sandboxModel.StatusRemoved}).
		Find(&inactive)

	for _, inst := range inactive {
		if tryConnect(inst.Addr) {
			log.Printf("[Prober] Inactive sandbox %s (%s) is reachable, re-registering", inst.ID, inst.Addr)
			recoveryInst := &sandboxModel.JudgeSandboxInstance{
				Addr:        inst.Addr,
				Version:     inst.Version,
				CPUCores:    inst.CPUCores,
				MemoryTotal: inst.MemoryTotal,
				Weight:      inst.Weight,
			}
			if err := p.registry.Register(recoveryInst); err != nil {
				log.Printf("[Prober] Failed to re-register sandbox %s (%s): %v", inst.ID, inst.Addr, err)
			}
		}
	}
}

// tryConnect attempts a TCP dial to the given address.
func tryConnect(addr string) bool {
	return globalConnPool.Ping(addr)
}

// probeInstance probes a single instance via TCP.
func (p *Prober) probeInstance(addr string) {
	if !globalConnPool.Ping(addr) {
		log.Printf("[Prober] Sandbox %s unreachable", addr)
		p.registry.MarkUnhealthy(addr)
		return
	}
	p.registry.UpdateHeartbeat(addr)
	log.Printf("[Prober] Sandbox %s health check passed", addr)
}

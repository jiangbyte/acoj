package sandbox

import (
	"sync"
	"sync/atomic"

	"hei-gin/plugins/plugin-judge/judgetypes"
)

// Pool 沙箱后端连接池（并发安全）
type Pool struct {
	mu         sync.RWMutex
	backends   []*poolEntry
	roundRobin uint64
}

type poolEntry struct {
	backend judgetypes.SandboxBackend
	weight  int
}

var DefaultPool = &Pool{}

// Get 获取一个可用的后端（轮询调度）
func (p *Pool) Get() judgetypes.SandboxBackend {
	p.mu.RLock()
	defer p.mu.RUnlock()
	n := len(p.backends)
	if n == 0 {
		return nil
	}
	if n == 1 {
		return p.backends[0].backend
	}
	idx := atomic.AddUint64(&p.roundRobin, 1) % uint64(n)
	return p.backends[idx].backend
}

// GetAll 返回所有可用后端（健康检查使用）
func (p *Pool) GetAll() []judgetypes.SandboxBackend {
	p.mu.RLock()
	defer p.mu.RUnlock()
	result := make([]judgetypes.SandboxBackend, len(p.backends))
	for i, e := range p.backends {
		result[i] = e.backend
	}
	return result
}

// ReplaceAll 原子替换整个后端列表（健康检查完成后调用）
func (p *Pool) ReplaceAll(backends []judgetypes.SandboxBackend) {
	p.mu.Lock()
	defer p.mu.Unlock()
	entries := make([]*poolEntry, len(backends))
	for i, b := range backends {
		entries[i] = &poolEntry{backend: b, weight: 1}
	}
	p.backends = entries
	atomic.StoreUint64(&p.roundRobin, 0)
}

// Count 当前活跃后端数
func (p *Pool) Count() int {
	p.mu.RLock()
	defer p.mu.RUnlock()
	return len(p.backends)
}

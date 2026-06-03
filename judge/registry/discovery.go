package registry

import (
	"log"
	"net"
	"sync"
	"time"
)

// Discovery handles sandbox instance discovery (passive, agent push-based).
type Discovery struct {
	registry *Registry
	stopCh   chan struct{}
	wg       sync.WaitGroup
}

func NewDiscovery(r *Registry) *Discovery {
	return &Discovery{registry: r, stopCh: make(chan struct{})}
}

func (d *Discovery) Start() {
	log.Println("[Discovery] Passive discovery mode active (agent push-based)")
}

func (d *Discovery) Stop() {
	close(d.stopCh)
	d.wg.Wait()
}

// connPool manages TCP connections to go-judge instances.
type connPool struct {
	addrs map[string]struct{}
	mu    sync.RWMutex
}

var globalConnPool = &connPool{addrs: make(map[string]struct{})}

func (p *connPool) Add(addr string) {
	p.mu.Lock()
	defer p.mu.Unlock()
	p.addrs[addr] = struct{}{}
}

func (p *connPool) Remove(addr string) {
	p.mu.Lock()
	defer p.mu.Unlock()
	delete(p.addrs, addr)
}

func (p *connPool) Has(addr string) bool {
	p.mu.RLock()
	defer p.mu.RUnlock()
	_, ok := p.addrs[addr]
	return ok
}

func (p *connPool) List() []string {
	p.mu.RLock()
	defer p.mu.RUnlock()
	addrs := make([]string, 0, len(p.addrs))
	for addr := range p.addrs {
		addrs = append(addrs, addr)
	}
	return addrs
}

// Ping checks if a go-judge instance is reachable via TCP.
func (p *connPool) Ping(addr string) bool {
	conn, err := net.DialTimeout("tcp", addr, 5*time.Second)
	if err != nil {
		return false
	}
	conn.Close()
	return true
}

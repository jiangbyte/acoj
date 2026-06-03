package registry

import (
	"crypto/md5"
	"encoding/binary"
	"log"
	"sync"
	"time"

	sandboxModel "hei-gin/modules/judge/sandbox/model"
)

type Scheduler struct {
	registry *Registry
	pool     []sandboxModel.JudgeSandboxInstance
	mu       sync.RWMutex
	stopCh   chan struct{}
	wg       sync.WaitGroup
}

func NewScheduler(r *Registry) *Scheduler {
	return &Scheduler{
		registry: r,
		pool:     r.GetPool(),
		stopCh:   make(chan struct{}),
	}
}

func (s *Scheduler) Start() {
	s.wg.Add(1)
	go func() {
		defer s.wg.Done()
		ticker := time.NewTicker(30 * time.Second)
		defer ticker.Stop()
		for {
			select {
			case <-ticker.C:
				s.Refresh()
			case <-s.stopCh:
				return
			}
		}
	}()
	log.Println("[Scheduler] Started")
}

func (s *Scheduler) Stop() {
	close(s.stopCh)
	s.wg.Wait()
	// gRPC pool removed
	log.Println("[Scheduler] Stopped")
}

func (s *Scheduler) Refresh() {
	s.mu.Lock()
	s.pool = s.registry.GetPool()
	s.mu.Unlock()
}

func (s *Scheduler) Pick(submissionID string) *sandboxModel.JudgeSandboxInstance {
	s.mu.RLock()
	defer s.mu.RUnlock()
	if len(s.pool) == 0 {
		return nil
	}
	idx := int(hashSubmissionID(submissionID)) % len(s.pool)
	return &s.pool[idx]
}

func (s *Scheduler) PickWithRetry(submissionID string) []sandboxModel.JudgeSandboxInstance {
	s.mu.RLock()
	defer s.mu.RUnlock()
	if len(s.pool) == 0 {
		return nil
	}
	startIdx := int(hashSubmissionID(submissionID)) % len(s.pool)
	result := make([]sandboxModel.JudgeSandboxInstance, len(s.pool))
	for i := 0; i < len(s.pool); i++ {
		idx := (startIdx + i) % len(s.pool)
		result[i] = s.pool[idx]
	}
	return result
}

func (s *Scheduler) PoolSize() int {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return len(s.pool)
}

func hashSubmissionID(id string) uint64 {
	h := md5.Sum([]byte(id))
	return binary.LittleEndian.Uint64(h[:8])
}

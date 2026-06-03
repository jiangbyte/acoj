package rank

import (
	"log"
	"sync"
	"time"
)

type RankCache struct {
	mu     sync.RWMutex
	cache  map[string]*cachedRank
	ttl    time.Duration
}

type cachedRank struct {
	data      interface{}
	expireAt  time.Time
}

var GlobalRankCache = &RankCache{
	cache: make(map[string]*cachedRank),
	ttl:   30 * time.Second,
}

func (rc *RankCache) Get(contestID string) interface{} {
	rc.mu.RLock()
	defer rc.mu.RUnlock()
	cached, ok := rc.cache[contestID]
	if !ok || time.Now().After(cached.expireAt) {
		return nil
	}
	return cached.data
}

func (rc *RankCache) Set(contestID string, data interface{}) {
	rc.mu.Lock()
	defer rc.mu.Unlock()
	rc.cache[contestID] = &cachedRank{
		data:     data,
		expireAt: time.Now().Add(rc.ttl),
	}
}

func (rc *RankCache) Invalidate(contestID string) {
	rc.mu.Lock()
	defer rc.mu.Unlock()
	delete(rc.cache, contestID)
	log.Printf("[RankCache] Invalidated cache for contest %s", contestID)
}

package runner

import (
	"context"
	"fmt"
	"sync"

	judgeclient "hei-gin/judge/client"
)

type spjCache struct {
	mu    sync.RWMutex
	cache map[string]*cachedSPJ
}

type cachedSPJ struct {
	fileID    string
	compileOK bool
}

var globalSPJCache = &spjCache{cache: make(map[string]*cachedSPJ)}

func compileSPJ(ctx context.Context, client *judgeclient.SandboxClient, spjSource, spjLanguage string) (string, error) {
	cacheKey := fmt.Sprintf("%s:%s", spjLanguage, hashString(spjSource))

	globalSPJCache.mu.RLock()
	if cached, ok := globalSPJCache.cache[cacheKey]; ok && cached.compileOK {
		globalSPJCache.mu.RUnlock()
		return cached.fileID, nil
	}
	globalSPJCache.mu.RUnlock()

	compileResult, err := client.Compile([]byte(spjSource), spjLanguage)
	if err != nil {
		return "", fmt.Errorf("SPJ compile failed: %w", err)
	}
	if compileResult.Status != "Accepted" {
		return "", fmt.Errorf("SPJ compile error: %s", compileResult.Stderr)
	}

	globalSPJCache.mu.Lock()
	globalSPJCache.cache[cacheKey] = &cachedSPJ{
		fileID:    compileResult.FileID,
		compileOK: true,
	}
	globalSPJCache.mu.Unlock()

	return compileResult.FileID, nil
}

func hashString(s string) string {
	const fnvPrime = 16777619
	var hash uint32 = 2166136261
	for i := 0; i < len(s); i++ {
		hash *= fnvPrime
		hash ^= uint32(s[i])
	}
	return fmt.Sprintf("%x", hash)
}

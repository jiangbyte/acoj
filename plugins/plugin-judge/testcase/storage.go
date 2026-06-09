package testcase

import (
	"container/list"
	"fmt"
	"log"
	"sync"

	"hei-gin/sdk/storage"
)

// ─── 常量 ──────────────────────────────────────────────────────────────

const (
	StorageBucket = "judge-testcases"           // 文件存储 bucket 名称
	DefaultStorageType = "LOCAL"                // 默认存储类型
)

func testcasePath(problemID, testcaseID, suffix string) string {
	return fmt.Sprintf("%s/%s/%s", problemID, testcaseID, suffix)
}

// ─── 文件读写 ──────────────────────────────────────────────────────────

// getStorage 获取存储后端实例
func getStorage() storage.Engine {
	return storage.GetStorage(DefaultStorageType)
}

// SaveInput 保存测试用例输入文件，返回存储路径
func SaveInput(problemID, testcaseID string, data []byte) (string, error) {
	key := testcasePath(problemID, testcaseID, "in")
	eng := getStorage()
	if eng == nil {
		return "", fmt.Errorf("storage engine not available")
	}
	path, err := eng.Store(StorageBucket, key, data)
	if err != nil {
		return "", fmt.Errorf("save input file: %w", err)
	}
	// 写入后失效缓存
	testcaseCache.Del(cacheKey(problemID, testcaseID, "in"))
	return path, nil
}

// SaveOutput 保存测试用例输出文件，返回存储路径
func SaveOutput(problemID, testcaseID string, data []byte) (string, error) {
	key := testcasePath(problemID, testcaseID, "out")
	eng := getStorage()
	if eng == nil {
		return "", fmt.Errorf("storage engine not available")
	}
	path, err := eng.Store(StorageBucket, key, data)
	if err != nil {
		return "", fmt.Errorf("save output file: %w", err)
	}
	testcaseCache.Del(cacheKey(problemID, testcaseID, "out"))
	return path, nil
}

// GetInput 读取测试用例输入文件内容
func GetInput(problemID, testcaseID string) ([]byte, error) {
	ck := cacheKey(problemID, testcaseID, "in")
	if data, ok := testcaseCache.Get(ck); ok {
		return data, nil
	}
	key := testcasePath(problemID, testcaseID, "in")
	eng := getStorage()
	if eng == nil {
		return nil, fmt.Errorf("storage engine not available")
	}
	data, err := eng.GetBytes(StorageBucket, key)
	if err != nil {
		return nil, fmt.Errorf("get input file: %w", err)
	}
	testcaseCache.Set(ck, data)
	return data, nil
}

// GetOutput 读取测试用例输出文件内容
func GetOutput(problemID, testcaseID string) ([]byte, error) {
	ck := cacheKey(problemID, testcaseID, "out")
	if data, ok := testcaseCache.Get(ck); ok {
		return data, nil
	}
	key := testcasePath(problemID, testcaseID, "out")
	eng := getStorage()
	if eng == nil {
		return nil, fmt.Errorf("storage engine not available")
	}
	data, err := eng.GetBytes(StorageBucket, key)
	if err != nil {
		return nil, fmt.Errorf("get output file: %w", err)
	}
	testcaseCache.Set(ck, data)
	return data, nil
}

// DeleteTestcaseFiles 删除测试用例的所有文件
func DeleteTestcaseFiles(problemID, testcaseID string) error {
	eng := getStorage()
	if eng == nil {
		return fmt.Errorf("storage engine not available")
	}
	for _, suffix := range []string{"in", "out"} {
		key := testcasePath(problemID, testcaseID, suffix)
		if err := eng.Delete(StorageBucket, key); err != nil {
			log.Printf("[testcase] delete file error (non-fatal): %v", err)
		}
		testcaseCache.Del(cacheKey(problemID, testcaseID, suffix))
	}
	return nil
}

// BatchLoadFiles 批量加载测试用例的输入输出到内存
// 配合本地缓存，判题引擎在 processTask 中调用
func BatchLoadFiles(problemID string, testcases []JudgeTestcase) (inMap map[string][]byte, outMap map[string][]byte, err error) {
	inMap = make(map[string][]byte, len(testcases))
	outMap = make(map[string][]byte, len(testcases))

	for _, tc := range testcases {
		data, e := GetInput(problemID, tc.ID)
		if e != nil {
			return nil, nil, fmt.Errorf("load input for testcase %s: %w", tc.ID, e)
		}
		inMap[tc.ID] = data

		data, e = GetOutput(problemID, tc.ID)
		if e != nil {
			return nil, nil, fmt.Errorf("load output for testcase %s: %w", tc.ID, e)
		}
		outMap[tc.ID] = data
	}
	return inMap, outMap, nil
}

// ─── 本地 LRU 缓存 ─────────────────────────────────────────────────────

func cacheKey(problemID, testcaseID, direction string) string {
	return problemID + ":" + testcaseID + ":" + direction
}

// TestcaseCache 测试用例内容缓存，LRU 淘汰
type TestcaseCache struct {
	mu       sync.RWMutex
	capacity int64          // 最大缓存字节数
	used     int64          // 当前使用量
	items    map[string]*cacheEntry
	lru      *list.List     // front=最近使用, back=最久未使用
}

type cacheEntry struct {
	key  string
	data []byte
	size int64
	elem *list.Element
}

// NewTestcaseCache 创建缓存，capacity 为最大字节数（默认 512MB）
func NewTestcaseCache(capacity int64) *TestcaseCache {
	if capacity <= 0 {
		capacity = 512 * 1024 * 1024 // 512MB
	}
	return &TestcaseCache{
		capacity: capacity,
		items:    make(map[string]*cacheEntry),
		lru:      list.New(),
	}
}

func (c *TestcaseCache) Get(key string) ([]byte, bool) {
	c.mu.RLock()
	entry, ok := c.items[key]
	c.mu.RUnlock()
	if !ok {
		return nil, false
	}
	// 移到 LRU 前端（最近使用）
	c.mu.Lock()
	c.lru.MoveToFront(entry.elem)
	c.mu.Unlock()
	return entry.data, true
}

func (c *TestcaseCache) Set(key string, data []byte) {
	size := int64(len(data))
	if size > c.capacity {
		return // 单条超过总容量，不缓存
	}

	c.mu.Lock()
	defer c.mu.Unlock()

	// 如果已存在，先删除旧条目
	if old, ok := c.items[key]; ok {
		c.lru.Remove(old.elem)
		c.used -= old.size
		delete(c.items, key)
	}

	// LRU 淘汰
	for c.used+size > c.capacity && c.lru.Len() > 0 {
		back := c.lru.Back()
		if back == nil {
			break
		}
		entry := back.Value.(*cacheEntry)
		c.lru.Remove(back)
		c.used -= entry.size
		delete(c.items, entry.key)
	}

	elem := c.lru.PushFront(&cacheEntry{key: key, data: data, size: size})
	c.items[key] = &cacheEntry{key: key, data: data, size: size, elem: elem}
	c.used += size
}

func (c *TestcaseCache) Del(key string) {
	c.mu.Lock()
	defer c.mu.Unlock()
	if entry, ok := c.items[key]; ok {
		c.lru.Remove(entry.elem)
		c.used -= entry.size
		delete(c.items, key)
	}
}

func (c *TestcaseCache) Purge() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.items = make(map[string]*cacheEntry)
	c.lru = list.New()
	c.used = 0
}

func (c *TestcaseCache) Len() int {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return len(c.items)
}

func (c *TestcaseCache) UsedBytes() int64 {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.used
}

// 全局缓存实例（引擎启动时初始化）
var testcaseCache = NewTestcaseCache(512 * 1024 * 1024)

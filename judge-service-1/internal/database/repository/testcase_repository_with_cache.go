// database/repository/testcase_repository_with_cache.go
package repository

import (
	"context"
	"encoding/json"
	"fmt"
	"judge-service/internal/database"
	"judge-service/internal/database/model"
	"time"

	"github.com/zeromicro/go-zero/core/logx"
)

type TestCaseRepositoryWithCache struct {
	repo     TestCaseRepository
	redisMgr *database.RedisManager
	cacheTTL time.Duration
}

func NewTestCaseRepositoryWithCache(repo TestCaseRepository, redisMgr *database.RedisManager) TestCaseRepository {
	return &TestCaseRepositoryWithCache{
		repo:     repo,
		redisMgr: redisMgr,
		cacheTTL: 30 * time.Minute, // 缓存30分钟
	}
}

// 生成缓存键
func (r *TestCaseRepositoryWithCache) generateCacheKey(problemID string, isSample bool) string {
	sampleFlag := "all"
	if isSample {
		sampleFlag = "sample"
	}
	return fmt.Sprintf("testcase:%s:%s", problemID, sampleFlag)
}

func (r *TestCaseRepositoryWithCache) GetTestCasesByProblemID(ctx context.Context, problemID string) ([]model.DataTestCase, error) {
	cacheKey := r.generateCacheKey(problemID, false)

	// 尝试从缓存获取
	if r.redisMgr != nil && r.redisMgr.IsReady() {
		cached, err := r.redisMgr.Get(cacheKey)
		if err == nil && cached != "" {
			var testCases []model.DataTestCase
			if err := json.Unmarshal([]byte(cached), &testCases); err == nil {
				logx.Infof("从缓存获取题目 %s 的测试用例，共 %d 条", problemID, len(testCases))
				return testCases, nil
			}
		}
	}

	// 缓存未命中，从数据库获取
	testCases, err := r.repo.GetTestCasesByProblemID(ctx, problemID)
	if err != nil {
		return nil, err
	}

	// 异步更新缓存
	if r.redisMgr != nil && r.redisMgr.IsReady() && len(testCases) > 0 {
		go r.cacheTestCases(cacheKey, testCases)
	}

	return testCases, nil
}

func (r *TestCaseRepositoryWithCache) GetTestCasesByProblemIDWithSample(ctx context.Context, problemID string, submitType bool) ([]model.DataTestCase, error) {
	cacheKey := r.generateCacheKey(problemID, !submitType) // 正式提交缓存所有用例，测试提交缓存样例

	// 尝试从缓存获取
	if r.redisMgr != nil && r.redisMgr.IsReady() {
		cached, err := r.redisMgr.Get(cacheKey)
		if err == nil && cached != "" {
			var testCases []model.DataTestCase
			if err := json.Unmarshal([]byte(cached), &testCases); err == nil {
				logx.Infof("从缓存获取题目 %s 的测试用例，共 %d 条", problemID, len(testCases))
				return testCases, nil
			}
		}
	}

	// 缓存未命中，从数据库获取
	testCases, err := r.repo.GetTestCasesByProblemIDWithSample(ctx, problemID, submitType)
	if err != nil {
		return nil, err
	}

	// 异步更新缓存
	if r.redisMgr != nil && r.redisMgr.IsReady() && len(testCases) > 0 {
		go r.cacheTestCases(cacheKey, testCases)
	}

	return testCases, nil
}

// 缓存测试用例
func (r *TestCaseRepositoryWithCache) cacheTestCases(cacheKey string, testCases []model.DataTestCase) {
	defer func() {
		if r := recover(); r != nil {
			logx.Errorf("缓存测试用例时发生 panic: %v", r)
		}
	}()

	data, err := json.Marshal(testCases)
	if err != nil {
		logx.Errorf("序列化测试用例失败: %v", err)
		return
	}

	if err := r.redisMgr.Set(cacheKey, string(data), r.cacheTTL); err != nil {
		logx.Errorf("缓存测试用例失败: %v", err)
	} else {
		logx.Infof("成功缓存测试用例，键: %s，数量: %d", cacheKey, len(testCases))
	}
}

// 清除测试用例缓存
func (r *TestCaseRepositoryWithCache) ClearCache(problemID string) error {
	if r.redisMgr == nil || !r.redisMgr.IsReady() {
		return nil
	}

	keys := []string{
		r.generateCacheKey(problemID, false),
		r.generateCacheKey(problemID, true),
	}

	for _, key := range keys {
		if err := r.redisMgr.Delete(key); err != nil {
			logx.Errorf("清除缓存失败，键: %s, 错误: %v", key, err)
		}
	}

	logx.Infof("已清除题目 %s 的测试用例缓存", problemID)
	return nil
}

package repository

import (
	"context"
	"fmt"
	"judge-service/internal/database/model"

	"github.com/zeromicro/go-zero/core/logx"
	"gorm.io/gorm"
)

type TestCaseRepository interface {
	GetTestCasesByProblemID(ctx context.Context, problemID string) ([]model.DataTestCase, error)
	GetTestCasesByProblemIDWithSample(ctx context.Context, problemID string, isSample bool) ([]model.DataTestCase, error)
}

type testCaseRepository struct {
	db *gorm.DB
}

func NewTestCaseRepository(db *gorm.DB) TestCaseRepository {
	return &testCaseRepository{
		db: db,
	}
}

func (r *testCaseRepository) GetTestCasesByProblemID(ctx context.Context, problemID string) ([]model.DataTestCase, error) {
	if r.db == nil {
		return nil, fmt.Errorf("数据库未准备好")
	}

	// 添加调试日志
	logx.Infof("开始查询题目 %s 的测试用例", problemID)

	var testCases []model.DataTestCase
	err := r.db.
		Where("problem_id = ?", problemID).
		Order("create_time ASC").
		Find(&testCases).Error

	if err != nil {
		return nil, fmt.Errorf("获取测试用例失败: %v", err)
	}

	logx.Infof("成功获取题目 %s 的测试用例，共 %d 条", problemID, len(testCases))

	// 如果没有数据，记录警告
	if len(testCases) == 0 {
		logx.Infof("题目 %s 没有找到未删除的测试用例", problemID)
	}

	return testCases, nil
}

func (r *testCaseRepository) GetTestCasesByProblemIDWithSample(ctx context.Context, problemID string, submitType bool) ([]model.DataTestCase, error) {
	if r.db == nil {
		return nil, fmt.Errorf("数据库未准备好")
	}

	// 添加调试日志
	logx.Infof("开始查询题目 %s 的测试用例, submitType: %t", problemID, submitType)

	var testCases []model.DataTestCase
	var err error

	baseQuery := r.db.
		Where("problem_id = ?", problemID).
		Where("deleted = 0")

	// 正式提交
	if submitType {
		// 正式提交：获取所有非样例测试用例
		testCases, err = r.getTestCases(baseQuery, 0, "ASC")
	} else {
		// 测试提交：先获取样例，如果没有则回退
		testCases, err = r.getTestCases(baseQuery, 1, "ASC")
		if err == nil && len(testCases) == 0 {
			logx.Infof("题目 %s 没有找到样例测试用例，尝试获取1条测试用例", problemID)
			testCases, err = r.getFallbackTestCase(baseQuery)
		}
	}

	if err != nil {
		return nil, err
	}

	logx.Infof("成功获取题目 %s 的测试用例，共 %d 条", problemID, len(testCases))

	if len(testCases) == 0 {
		logx.Errorf("题目 %s 没有找到测试用例", problemID)
	}

	return testCases, nil
}
	
// 辅助方法
func (r *testCaseRepository) getTestCases(baseQuery *gorm.DB, isSample int, order string) ([]model.DataTestCase, error) {
	var testCases []model.DataTestCase
	err := baseQuery.
		Where("is_sample = ?", isSample).
		Order("create_time " + order).
		Find(&testCases).Error
	if err != nil {
		return nil, fmt.Errorf("获取测试用例失败: %v", err)
	}
	return testCases, nil
}

func (r *testCaseRepository) getFallbackTestCase(baseQuery *gorm.DB) ([]model.DataTestCase, error) {
	var testCases []model.DataTestCase
	err := baseQuery.
		Order("create_time DESC").
		Limit(1).
		Find(&testCases).Error
	if err != nil {
		return nil, fmt.Errorf("获取回退测试用例失败: %v", err)
	}
	return testCases, nil
}

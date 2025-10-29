package repository

import (
	"context"
	"fmt"
	"judge-service/internal/model"

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
	query := r.db.Where("problem_id = ?", problemID)

	if !submitType {
		// 先尝试获取is_sample=1的数据
		query = query.Where("is_sample = ?", 1).Where("deleted = 0")

		err := query.Order("create_time ASC").Find(&testCases).Error
		if err != nil {
			return nil, fmt.Errorf("获取测试用例失败: %v", err)
		}

		// 如果过滤后没有数据，则回退到获取1条测试用例（按时间降序获取最新的1条）
		if len(testCases) == 0 {
			logx.Infof("题目 %s 没有找到样例测试用例，尝试获取1条测试用例", problemID)

			err = r.db.
				Where("problem_id = ?", problemID).
				Order("create_time DESC"). // 按时间降序获取最新的
				Limit(1).                  // 只获取1条
				Find(&testCases).Error

			if err != nil {
				return nil, fmt.Errorf("获取回退测试用例失败: %v", err)
			}
		}
	} else {
		// submitType 为True时，过滤is_sample=0的数据
		err := query.
			Where("is_sample = ?", 0).
			Order("create_time ASC").
			Find(&testCases).Error

		if err != nil {
			return nil, fmt.Errorf("获取测试用例失败: %v", err)
		}
	}

	logx.Infof("成功获取题目 %s 的测试用例，共 %d 条", problemID, len(testCases))

	// 如果没有数据，记录警告
	if len(testCases) == 0 {
		logx.Infof("题目 %s 没有找到未删除的测试用例", problemID)
	}

	return testCases, nil
}

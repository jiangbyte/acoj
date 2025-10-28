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
		return nil, fmt.Errorf("database is not ready")
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

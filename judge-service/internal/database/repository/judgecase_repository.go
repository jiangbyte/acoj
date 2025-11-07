package repository

import (
	"context"
	"fmt"
	"github.com/zeromicro/go-zero/core/logx"
	"gorm.io/gorm"
	"judge-service/internal/database/model"
)

type JudgeCaseRepository interface {
	// 单个操作
	CreateJudgeCase(ctx context.Context, judgeCase *model.DataJudgeCase) error

	// 批量操作
	BatchCreateJudgeCases(judgeCases []*model.DataJudgeCase) error
}

type judgeCaseRepository struct {
	db *gorm.DB
}

func NewJudgeCaseRepository(db *gorm.DB) JudgeCaseRepository {
	return &judgeCaseRepository{
		db: db,
	}
}

// CreateJudgeCase 创建单个判题用例
func (r *judgeCaseRepository) CreateJudgeCase(ctx context.Context, judgeCase *model.DataJudgeCase) error {
	if r.db == nil {
		return fmt.Errorf("数据库未准备好")
	}

	logx.Infof("开始创建判题用例，问题ID: %s", judgeCase.SubmitID)

	err := r.db.WithContext(ctx).Create(judgeCase).Error
	if err != nil {
		logx.Errorf("创建判题用例失败: %v", err)
		return fmt.Errorf("创建判题用例失败: %v", err)
	}

	logx.Infof("成功创建判题用例，ID: %s", judgeCase.ID)
	return nil
}

// BatchCreateJudgeCases 批量创建判题用例
func (r *judgeCaseRepository) BatchCreateJudgeCases(judgeCases []*model.DataJudgeCase) error {
	if r.db == nil {
		return fmt.Errorf("数据库未准备好")
	}

	if len(judgeCases) == 0 {
		logx.Info("判题用例列表为空，跳过批量创建")
		return nil
	}

	// 获取第一个用例的问题ID用于日志
	submitID := ""
	if len(judgeCases) > 0 && judgeCases[0] != nil {
		submitID = judgeCases[0].SubmitID
	}

	logx.Infof("开始批量创建判题用例，问题ID: %s，数量: %d", submitID, len(judgeCases))

	// 使用事务确保批量操作的原子性
	err := r.db.Transaction(func(tx *gorm.DB) error {
		if err := tx.CreateInBatches(judgeCases, 100).Error; err != nil {
			return fmt.Errorf("批量创建判题用例失败: %v", err)
		}
		return nil
	})

	if err != nil {
		logx.Errorf("批量创建判题用例失败: %v", err)
		return err
	}

	logx.Infof("成功批量创建判题用例，问题ID: %s，数量: %d", submitID, len(judgeCases))
	return nil
}

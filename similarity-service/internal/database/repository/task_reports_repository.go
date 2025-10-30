package repository

import (
	"similarity-service/internal/database/model"

	"gorm.io/gorm"
)

type TaskReportsRepository interface {
	// 单个创建
	Create(taskReport *model.TaskReports) error
}

type taskReportsRepository struct {
	db *gorm.DB
}

func NewTaskReportsRepository(db *gorm.DB) TaskReportsRepository {
	return &taskReportsRepository{
		db: db,
	}
}

// Create 单个创建
func (r *taskReportsRepository) Create(taskReport *model.TaskReports) error {
	if taskReport == nil {
		return nil
	}
	return r.db.Create(taskReport).Error
}

package sandbox

import (
	"context"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/utils"
)

// CreateSandbox 创建沙箱实例
func CreateSandbox(sb *JudgeSandbox) error {
	if sb.ID == "" {
		sb.ID = utils.GenerateID()
	}
	now := time.Now()
	sb.CreatedAt = &now
	sb.CreatedBy = nil
	sb.UpdatedAt = &now
	sb.UpdatedBy = nil
	return db.DB.WithContext(context.Background()).Create(sb).Error
}

// ModifySandbox 编辑沙箱实例
func ModifySandbox(id, name, endpoint string, timeout *int) error {
	updates := map[string]any{}
	if name != "" {
		updates["name"] = name
	}
	if endpoint != "" {
		updates["endpoint"] = endpoint
	}
	if timeout != nil {
		updates["timeout"] = *timeout
	}
	updates["updated_at"] = time.Now()
	return db.DB.WithContext(context.Background()).
		Model(&JudgeSandbox{}).
		Where("id = ?", id).
		Updates(updates).Error
}

// RemoveSandboxes 软删除沙箱实例
func RemoveSandboxes(ids []string) error {
	return db.DB.WithContext(context.Background()).
		Model(&JudgeSandbox{}).
		Where("id IN ?", ids).
		Updates(map[string]any{
			"status":     "removed",
			"updated_at": time.Now(),
		}).Error
}

// ListActiveSandboxes 查询所有活跃沙箱
func ListActiveSandboxes() ([]JudgeSandbox, error) {
	var instances []JudgeSandbox
	err := db.DB.WithContext(context.Background()).
		Where("status = ?", "active").
		Find(&instances).Error
	return instances, err
}

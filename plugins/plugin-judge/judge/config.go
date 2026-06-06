package judge

import (
	"context"
	"time"
	"strconv"

	"hei-gin/sdk/db"
)

// GetAllConfigs 获取所有判题配置
func GetAllConfigs() ([]JudgeConfigVO, error) {
	ctx := context.Background()
	var configs []JudgeConfig
	if err := db.DB.WithContext(ctx).Find(&configs).Error; err != nil {
		return nil, err
	}
	voList := make([]JudgeConfigVO, len(configs))
	for i, c := range configs {
		voList[i] = JudgeConfigVO{
			ID:    c.ID,
			Key:   c.Key,
			Value: c.Value,
			Desc:  c.Desc,
		}
	}
	return voList, nil
}

// BatchUpdateConfig 批量更新判题配置
func BatchUpdateConfig(params JudgeConfigBatchUpdateParam) error {
	ctx := context.Background()
	now := time.Now()
	for _, p := range params {
		if err := db.DB.WithContext(ctx).
			Model(&JudgeConfig{}).
			Where("`key` = ?", p.Key).
			Updates(map[string]any{
				"value":      p.Value,
				"updated_at": now,
			}).Error; err != nil {
			return err
		}
	}
	return nil
}

// GetConfigInt 获取整数配置
func GetConfigInt(key string, defaultVal int) int {
	ctx := context.Background()
	var cfg JudgeConfig
	if err := db.DB.WithContext(ctx).Where("`key` = ?", key).First(&cfg).Error; err != nil {
		return defaultVal
	}
	val, err := strconv.Atoi(cfg.Value)
	if err != nil {
		return defaultVal
	}
	return val
}

// GetConfigString 获取字符串配置
func GetConfigString(key string, defaultVal string) string {
	ctx := context.Background()
	var cfg JudgeConfig
	if err := db.DB.WithContext(ctx).Where("`key` = ?", key).First(&cfg).Error; err != nil {
		return defaultVal
	}
	return cfg.Value
}

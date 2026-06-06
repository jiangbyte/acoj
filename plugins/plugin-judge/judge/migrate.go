package judge

import (
	"hei-gin/sdk/db"
)

func init() {
	db.RegisterModel(&JudgeConfig{})
	db.RegisterSeed("default judge config", seedJudgeConfig)
}

func seedJudgeConfig() error {
	var count int64
	db.DB.Model(&JudgeConfig{}).Count(&count)
	if count > 0 {
		return nil
	}

	defaults := map[string]string{
		"worker_count":            "4",
		"health_check_interval":   "30",
		"max_retry":               "3",
		"recovery_interval":       "120",
		"default_time_limit":      "1000",
		"default_memory_limit":    "262144",
		"default_stack_limit":     "65536",
		"default_output_limit":    "65536",
		"compile_time_limit":      "10000",
		"compile_memory_limit":    "524288",
	}

	for k, v := range defaults {
		cfg := JudgeConfig{
			ID:    k,
			Key:   k,
			Value: v,
			Desc:  "Auto seeded",
		}
		if err := db.DB.Create(&cfg).Error; err != nil {
			return err
		}
	}

	return nil
}

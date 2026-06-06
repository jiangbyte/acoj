package problem

import (
	"context"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/utils"
)

// JudgeProblemLanguageLimit 题目各语言资源限制与代码模板
type JudgeProblemLanguageLimit struct {
	ID          string     `gorm:"primaryKey;size:32" json:"id"`
	ProblemID   string     `gorm:"size:32;uniqueIndex:idx_problem_lang;index" json:"problem_id"`
	Language    string     `gorm:"size:32;uniqueIndex:idx_problem_lang" json:"language"` // c / cpp / python3 / go / java / rust / node ...
	TimeLimit   int64      `gorm:"default:0" json:"time_limit"`                          // 0=使用题目默认值 (ms)
	MemoryLimit int64      `gorm:"default:0" json:"memory_limit"`                        // 0=使用题目默认值 (KB)
	StackLimit  int64      `gorm:"default:0" json:"stack_limit"`                         // 0=使用题目默认值 (KB)
	OutputLimit int64      `gorm:"default:0" json:"output_limit"`                        // 0=使用题目默认值 (KB)
	Template    string     `gorm:"type:text" json:"template"`                            // 代码模板
	CreatedAt   *time.Time `json:"created_at"`
	UpdatedAt   *time.Time `json:"updated_at"`
}

func (JudgeProblemLanguageLimit) TableName() string { return "judge_problem_language_limit" }

// LanguageLimitVO 语言限制视图
type LanguageLimitVO struct {
	ID          string `json:"id"`
	ProblemID   string `json:"problem_id"`
	Language    string `json:"language"`
	TimeLimit   int64  `json:"time_limit"`
	MemoryLimit int64  `json:"memory_limit"`
	StackLimit  int64  `json:"stack_limit"`
	OutputLimit int64  `json:"output_limit"`
	Template    string `json:"template"`
}

// LanguageLimitInput 创建/修改语言限制参数
type LanguageLimitInput struct {
	Language    string `json:"language" binding:"required"`
	TimeLimit   *int64 `json:"time_limit"`   // nil = 使用默认值
	MemoryLimit *int64 `json:"memory_limit"` // nil = 使用默认值
	StackLimit  *int64 `json:"stack_limit"`  // nil = 使用默认值
	OutputLimit *int64 `json:"output_limit"` // nil = 使用默认值
	Template    string `json:"template"`
}

// GetLanguageLimits 获取题目的所有语言限制
func GetLanguageLimits(problemID string) ([]LanguageLimitVO, error) {
	ctx := context.Background()
	var limits []JudgeProblemLanguageLimit
	if err := db.DB.WithContext(ctx).
		Where("problem_id = ?", problemID).
		Find(&limits).Error; err != nil {
		return nil, err
	}
	vos := make([]LanguageLimitVO, len(limits))
	for i, l := range limits {
		vos[i] = LanguageLimitVO{
			ID:          l.ID,
			ProblemID:   l.ProblemID,
			Language:    l.Language,
			TimeLimit:   l.TimeLimit,
			MemoryLimit: l.MemoryLimit,
			StackLimit:  l.StackLimit,
			OutputLimit: l.OutputLimit,
			Template:    l.Template,
		}
	}
	return vos, nil
}

// GetEffectiveLimits 获取某语言的有效限制（如语言限制为0则回退到题目默认值）
func GetEffectiveLimits(problemID, language string, defaults ProblemLimits) ProblemLimits {
	ctx := context.Background()
	var ll JudgeProblemLanguageLimit
	err := db.DB.WithContext(ctx).
		Where("problem_id = ? AND language = ?", problemID, language).
		First(&ll).Error
	if err != nil {
		return defaults
	}

	result := defaults
	if ll.TimeLimit > 0 {
		result.TimeLimit = ll.TimeLimit
	}
	if ll.MemoryLimit > 0 {
		result.MemoryLimit = ll.MemoryLimit
	}
	if ll.StackLimit > 0 {
		result.StackLimit = ll.StackLimit
	}
	if ll.OutputLimit > 0 {
		result.OutputLimit = ll.OutputLimit
	}
	return result
}

// GetLanguageTemplate 获取某语言的代码模板（空串表示无模板）
func GetLanguageTemplate(problemID, language string) string {
	ctx := context.Background()
	var ll JudgeProblemLanguageLimit
	err := db.DB.WithContext(ctx).
		Where("problem_id = ? AND language = ?", problemID, language).
		First(&ll).Error
	if err != nil {
		return ""
	}
	return ll.Template
}

// UpsertLanguageLimit 创建或更新语言限制
func UpsertLanguageLimit(problemID string, input LanguageLimitInput) error {
	ctx := context.Background()
	now := time.Now()

	var existing JudgeProblemLanguageLimit
	err := db.DB.WithContext(ctx).
		Where("problem_id = ? AND language = ?", problemID, input.Language).
		First(&existing).Error

	if err != nil {
		// 创建
		ll := JudgeProblemLanguageLimit{
			ID:        utils.GenerateID(),
			ProblemID: problemID,
			Language:  input.Language,
			Template:  input.Template,
			CreatedAt: &now,
			UpdatedAt: &now,
		}
		if input.TimeLimit != nil {
			ll.TimeLimit = *input.TimeLimit
		}
		if input.MemoryLimit != nil {
			ll.MemoryLimit = *input.MemoryLimit
		}
		if input.StackLimit != nil {
			ll.StackLimit = *input.StackLimit
		}
		if input.OutputLimit != nil {
			ll.OutputLimit = *input.OutputLimit
		}
		return db.DB.WithContext(ctx).Create(&ll).Error
	}

	// 更新
	updates := map[string]any{"updated_at": now}
	if input.TimeLimit != nil {
		updates["time_limit"] = *input.TimeLimit
	}
	if input.MemoryLimit != nil {
		updates["memory_limit"] = *input.MemoryLimit
	}
	if input.StackLimit != nil {
		updates["stack_limit"] = *input.StackLimit
	}
	if input.OutputLimit != nil {
		updates["output_limit"] = *input.OutputLimit
	}
	if input.Template != "" {
		updates["template"] = input.Template
	}
	return db.DB.WithContext(ctx).
		Model(&JudgeProblemLanguageLimit{}).
		Where("id = ?", existing.ID).
		Updates(updates).Error
}

// DeleteLanguageLimit 删除语言限制
func DeleteLanguageLimit(problemID, language string) error {
	ctx := context.Background()
	return db.DB.WithContext(ctx).
		Where("problem_id = ? AND language = ?", problemID, language).
		Delete(&JudgeProblemLanguageLimit{}).Error
}

func init() {
	db.RegisterModel(&JudgeProblemLanguageLimit{})
}

// ProblemLimits 资源限制集合
type ProblemLimits struct {
	TimeLimit   int64
	MemoryLimit int64
	StackLimit  int64
	OutputLimit int64
}

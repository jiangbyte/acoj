package problem

import (
	"context"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/utils"
	"hei-gin/plugins/plugin-judge/judge"
)

// JudgeProblemLanguageLimit 题目各语言资源限制与代码模板
type JudgeProblemLanguageLimit struct {
	ID          string     `gorm:"primaryKey;size:32" json:"id"`
	ProblemID   string     `gorm:"size:32;uniqueIndex:idx_problem_lang;index" json:"problem_id"`
	Language    string     `gorm:"size:32;uniqueIndex:idx_problem_lang" json:"language"` // c / cpp / python3 / go / java / rust / node ...
	TimeLimit   int64      `gorm:"default:1000" json:"time_limit"`                       // ms
	MemoryLimit int64      `gorm:"default:262144" json:"memory_limit"`                    // KB
	StackLimit  int64      `gorm:"default:65536" json:"stack_limit"`                      // KB
	OutputLimit int64      `gorm:"default:65536" json:"output_limit"`                     // KB
	Template    string     `gorm:"type:text" json:"template"`                             // 代码模板
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
	TimeLimit   int64  `json:"time_limit"`
	MemoryLimit int64  `json:"memory_limit"`
	StackLimit  int64  `json:"stack_limit"`
	OutputLimit int64  `json:"output_limit"`
	Template    string `json:"template"`
}

// ResolvedLimits 解析后的资源限制（一定有值）
type ResolvedLimits struct {
	TimeLimit   int64 // ms
	MemoryLimit int64 // KB
	StackLimit  int64 // KB
	OutputLimit int64 // KB
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

// GetLanguageLimit 获取某语言的资源限制记录
func GetLanguageLimit(problemID, language string) (LanguageLimitVO, error) {
	ctx := context.Background()
	var ll JudgeProblemLanguageLimit
	err := db.DB.WithContext(ctx).
		Where("problem_id = ? AND language = ?", problemID, language).
		First(&ll).Error
	if err != nil {
		return LanguageLimitVO{}, err
	}
	return LanguageLimitVO{
		ID:          ll.ID,
		ProblemID:   ll.ProblemID,
		Language:    ll.Language,
		TimeLimit:   ll.TimeLimit,
		MemoryLimit: ll.MemoryLimit,
		StackLimit:  ll.StackLimit,
		OutputLimit: ll.OutputLimit,
		Template:    ll.Template,
	}, nil
}

// GetEffectiveLimits 获取某语言的有效限制
// 如果该语言没有配置, 则使用系统默认值
func GetEffectiveLimits(problemID, language string) ResolvedLimits {
	ll, err := GetLanguageLimit(problemID, language)
	if err != nil {
		// 未配置 → 使用系统默认值
		return ResolvedLimits{
			TimeLimit:   int64(judge.GetConfigInt("default_time_limit", 1000)),
			MemoryLimit: int64(judge.GetConfigInt("default_memory_limit", 262144)),
			StackLimit:  int64(judge.GetConfigInt("default_stack_limit", 65536)),
			OutputLimit: int64(judge.GetConfigInt("default_output_limit", 65536)),
		}
	}
	return ResolvedLimits{
		TimeLimit:   ll.TimeLimit,
		MemoryLimit: ll.MemoryLimit,
		StackLimit:  ll.StackLimit,
		OutputLimit: ll.OutputLimit,
	}
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
		ll := JudgeProblemLanguageLimit{
			ID:          utils.GenerateID(),
			ProblemID:   problemID,
			Language:    input.Language,
			TimeLimit:   input.TimeLimit,
			MemoryLimit: input.MemoryLimit,
			StackLimit:  input.StackLimit,
			OutputLimit: input.OutputLimit,
			Template:    input.Template,
			CreatedAt:   &now,
			UpdatedAt:   &now,
		}
		if ll.TimeLimit <= 0 {
			ll.TimeLimit = 1000
		}
		if ll.MemoryLimit <= 0 {
			ll.MemoryLimit = 262144
		}
		if ll.StackLimit <= 0 {
			ll.StackLimit = 65536
		}
		if ll.OutputLimit <= 0 {
			ll.OutputLimit = 65536
		}
		return db.DB.WithContext(ctx).Create(&ll).Error
	}

	updates := map[string]any{"updated_at": now}
	updates["time_limit"] = input.TimeLimit
	updates["memory_limit"] = input.MemoryLimit
	updates["stack_limit"] = input.StackLimit
	updates["output_limit"] = input.OutputLimit
	updates["template"] = input.Template
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

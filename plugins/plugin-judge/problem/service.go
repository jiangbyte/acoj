package problem

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/sdk/auth"
	"hei-gin/sdk/db"
	"hei-gin/sdk/exception"
	"hei-gin/sdk/pojo"
	"hei-gin/sdk/result"
	"hei-gin/sdk/utils"

	"github.com/gin-gonic/gin"
)

// RelProblemTag 题目-标签关联
type RelProblemTag struct {
	ID        string `gorm:"primaryKey;size:32" json:"id"`
	ProblemID string `gorm:"size:32;uniqueIndex:idx_problem_tag" json:"problem_id"`
	TagID     string `gorm:"size:32;uniqueIndex:idx_problem_tag;index" json:"tag_id"`
}

func (RelProblemTag) TableName() string { return "rel_problem_tag" }

func init() {
	db.RegisterModel(&RelProblemTag{})
}

// PageService 题目分页查询
func PageService(c *gin.Context, param *ProblemPageParam) gin.H {
	ctx := context.Background()
	tx := db.DB.WithContext(ctx).Model(&JudgeProblem{})

	if param.Keyword != "" {
		tx = tx.Where("title LIKE ? OR source LIKE ?", "%"+param.Keyword+"%", "%"+param.Keyword+"%")
	}
	if param.Difficulty != "" {
		tx = tx.Where("difficulty = ?", param.Difficulty)
	}
	if param.Status != "" {
		tx = tx.Where("status = ?", param.Status)
	}
	if param.TagID != "" {
		tx = tx.Where("id IN (SELECT problem_id FROM rel_problem_tag WHERE tag_id = ?)", param.TagID)
	}
	if param.JudgeType != "" {
		tx = tx.Where("judge_type = ?", param.JudgeType)
	}

	var total int64
	tx.Count(&total)

	page := param.Current
	size := param.Size
	if page < 1 {
		page = 1
	}
	if size < 1 || size > 100 {
		size = 10
	}

	var problems []JudgeProblem
	tx.Offset((page - 1) * size).Limit(size).Order("created_at DESC").Find(&problems)

	problemIDs := make([]string, len(problems))
	for i, p := range problems {
		problemIDs[i] = p.ID
	}

	voList := make([]ProblemVO, 0)
	if len(problems) > 0 {
		var rels []RelProblemTag
		db.DB.WithContext(ctx).Where("problem_id IN ?", problemIDs).Find(&rels)
		tagMap := make(map[string][]string)
		for _, rel := range rels {
			tagMap[rel.ProblemID] = append(tagMap[rel.ProblemID], rel.TagID)
		}

		for _, p := range problems {
			vo := modelToVO(&p)
			vo.Tags = tagMap[p.ID]
			langLimits, _ := GetLanguageLimits(p.ID)
			if len(langLimits) > 0 {
				vo.LanguageLimits = langLimits
			}
			voList = append(voList, vo)
		}
	}

	return result.PageDataResult(c, voList, total, page, size)
}

// CreateService 创建题目
func CreateService(c *gin.Context, param *ProblemCreateParam) error {
	ctx := context.Background()
	now := time.Now()
	userID := auth.GetLoginID(c)

	problem := JudgeProblem{
		ID:              utils.GenerateID(),
		Title:           param.Title,
		Description:     param.Description,
		InputDesc:       param.InputDesc,
		OutputDesc:      param.OutputDesc,
		SampleInput:     param.SampleInput,
		SampleOutput:    param.SampleOutput,
		Hint:            param.Hint,
		Source:          param.Source,
		JudgeType:       param.JudgeType,
		SpjCode:         param.SpjCode,
		SpjLanguage:     param.SpjLanguage,
		InteractiveCode: param.InteractiveCode,
		InteractiveLang: param.InteractiveLang,
		Difficulty:      param.Difficulty,
		Status:          param.Status,
		CreatedBy:       userID,
		CreatedAt:       &now,
		UpdatedAt:       &now,
	}

	if problem.JudgeType == "" {
		problem.JudgeType = "default"
	}
	if problem.Difficulty == "" {
		problem.Difficulty = "EASY"
	}
	if problem.Status == "" {
		problem.Status = "ACTIVE"
	}

	return db.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Create(&problem).Error; err != nil {
			return err
		}
		if len(param.TagIDs) > 0 {
			for _, tagID := range param.TagIDs {
				rel := RelProblemTag{
					ID:        utils.GenerateID(),
					ProblemID: problem.ID,
					TagID:     tagID,
				}
				if err := tx.Create(&rel).Error; err != nil {
					return err
				}
			}
		}
		for _, ll := range param.LanguageLimits {
			if err := UpsertLanguageLimit(problem.ID, ll); err != nil {
				return err
			}
		}
		return nil
	})
}

// ModifyService 编辑题目
func ModifyService(c *gin.Context, param *ProblemModifyParam) error {
	ctx := context.Background()

	updates := map[string]any{}
	if param.Title != "" {
		updates["title"] = param.Title
	}
	if param.Description != "" {
		updates["description"] = param.Description
	}
	if param.InputDesc != "" {
		updates["input_desc"] = param.InputDesc
	}
	if param.OutputDesc != "" {
		updates["output_desc"] = param.OutputDesc
	}
	if param.SampleInput != "" {
		updates["sample_input"] = param.SampleInput
	}
	if param.SampleOutput != "" {
		updates["sample_output"] = param.SampleOutput
	}
	if param.Hint != "" {
		updates["hint"] = param.Hint
	}
	if param.Source != "" {
		updates["source"] = param.Source
	}
	if param.JudgeType != "" {
		updates["judge_type"] = param.JudgeType
	}
	if param.SpjCode != "" {
		updates["spj_code"] = param.SpjCode
	}
	if param.SpjLanguage != "" {
		updates["spj_language"] = param.SpjLanguage
	}
	if param.InteractiveCode != "" {
		updates["interactive_code"] = param.InteractiveCode
	}
	if param.InteractiveLang != "" {
		updates["interactive_lang"] = param.InteractiveLang
	}
	if param.Difficulty != "" {
		updates["difficulty"] = param.Difficulty
	}
	if param.Status != "" {
		updates["status"] = param.Status
	}
	updates["updated_at"] = time.Now()

	return db.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Model(&JudgeProblem{}).Where("id = ?", param.ID).Updates(updates).Error; err != nil {
			return err
		}
		if param.TagIDs != nil {
			tx.Where("problem_id = ?", param.ID).Delete(&RelProblemTag{})
			for _, tagID := range param.TagIDs {
				rel := RelProblemTag{
					ID:        utils.GenerateID(),
					ProblemID: param.ID,
					TagID:     tagID,
				}
				if err := tx.Create(&rel).Error; err != nil {
					return err
				}
			}
		}
		if param.LanguageLimits != nil {
			tx.Where("problem_id = ?", param.ID).Delete(&JudgeProblemLanguageLimit{})
			for _, ll := range param.LanguageLimits {
				if err := UpsertLanguageLimit(param.ID, ll); err != nil {
					return err
				}
			}
		}
		return nil
	})
}

// RemoveService 删除题目
func RemoveService(c *gin.Context, param pojo.IdsParam) error {
	ctx := context.Background()
	return db.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Where("id IN ?", param.IDs).Delete(&JudgeProblem{}).Error; err != nil {
			return err
		}
		tx.Where("problem_id IN ?", param.IDs).Delete(&RelProblemTag{})
		tx.Where("problem_id IN ?", param.IDs).Delete(&JudgeProblemLanguageLimit{})
		return nil
	})
}

// DetailService 题目详情
func DetailService(c *gin.Context, id string) (*ProblemVO, error) {
	ctx := context.Background()
	var problem JudgeProblem
	if err := db.DB.WithContext(ctx).Where("id = ?", id).First(&problem).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, exception.NewBusinessError("题目不存在", 400)
		}
		return nil, err
	}

	vo := modelToVO(&problem)

	var rels []RelProblemTag
	db.DB.WithContext(ctx).Where("problem_id = ?", id).Find(&rels)
	for _, rel := range rels {
		vo.Tags = append(vo.Tags, rel.TagID)
	}

	langLimits, _ := GetLanguageLimits(id)
	if len(langLimits) > 0 {
		vo.LanguageLimits = langLimits
	}

	return &vo, nil
}

func modelToVO(p *JudgeProblem) ProblemVO {
	createdAt := ""
	if p.CreatedAt != nil {
		createdAt = p.CreatedAt.Format("2006-01-02 15:04:05")
	}
	updatedAt := ""
	if p.UpdatedAt != nil {
		updatedAt = p.UpdatedAt.Format("2006-01-02 15:04:05")
	}

	return ProblemVO{
		ID:              p.ID,
		Title:           p.Title,
		Description:     p.Description,
		InputDesc:       p.InputDesc,
		OutputDesc:      p.OutputDesc,
		SampleInput:     p.SampleInput,
		SampleOutput:    p.SampleOutput,
		Hint:            p.Hint,
		Source:          p.Source,
		JudgeType:       p.JudgeType,
		SpjCode:         p.SpjCode,
		SpjLanguage:     p.SpjLanguage,
		InteractiveCode: p.InteractiveCode,
		InteractiveLang: p.InteractiveLang,
		Difficulty:      p.Difficulty,
		Status:          p.Status,
		SubmitCount:     p.SubmitCount,
		AcceptCount:     p.AcceptCount,
		CreatedBy:       p.CreatedBy,
		CreatedAt:       createdAt,
		UpdatedAt:       updatedAt,
	}
}

// PublicDetailService 对外公开的题目详情（隐藏判题配置等敏感信息）
func PublicDetailService(c *gin.Context, id string) (*PublicProblemVO, error) {
	vo, err := DetailService(c, id)
	if err != nil {
		return nil, err
	}
	publicVO := &PublicProblemVO{
		ID:          vo.ID,
		Title:       vo.Title,
		Description: vo.Description,
		InputDesc:   vo.InputDesc,
		OutputDesc:  vo.OutputDesc,
		SampleInput: vo.SampleInput,
		SampleOutput: vo.SampleOutput,
		Hint:        vo.Hint,
		Source:      vo.Source,
		JudgeType:   vo.JudgeType,
		Difficulty:  vo.Difficulty,
		SubmitCount: vo.SubmitCount,
		AcceptCount: vo.AcceptCount,
		CreatedAt:   vo.CreatedAt,
		UpdatedAt:   vo.UpdatedAt,
		Tags:        vo.Tags,
	}
	if len(vo.LanguageLimits) > 0 {
		publicVO.LanguageLimits = vo.LanguageLimits
	}
	return publicVO, nil
}

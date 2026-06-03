package submission

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	"hei-gin/judge/queue"
	judgeProblem "hei-gin/modules/judge/problem"

	"github.com/gin-gonic/gin"
)

func Page(c *gin.Context, param *SubmissionPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	query := db.DB.WithContext(ctx).Model(&JudgeSubmission{})
	if param.ProblemID != "" {
		query = query.Where("problem_id = ?", param.ProblemID)
	}
	if param.UserID != "" {
		query = query.Where("user_id = ?", param.UserID)
	}
	if param.Status != "" {
		query = query.Where("status = ?", param.Status)
	}
	if param.Language != "" {
		query = query.Where("language = ?", param.Language)
	}

	var total int64
	query.Count(&total)

	var records []JudgeSubmission
	offset := (param.Current - 1) * param.Size
	query.Order("created_at DESC").Limit(param.Size).Offset(offset).Find(&records)

	vos := make([]*SubmissionVO, 0, len(records))
	for _, r := range records {
		vos = append(vos, entToVO(&r))
	}
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func Detail(c *gin.Context, id string) *SubmissionVO {
	ctx := context.Background()
	var entity JudgeSubmission
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil
		}
		panic(exception.NewBusinessError("查询提交详情失败: "+err.Error(), 500))
	}
	return entToVOWithDetail(c, &entity)
}

func Create(c *gin.Context, param *SubmissionCreateParam, userID string) *SubmissionVO {
	ctx := context.Background()

	// Validate problem exists
	var problem judgeProblem.JudgeProblem
	if err := db.DB.WithContext(ctx).First(&problem, "id = ?", param.ProblemID).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			panic(exception.NewBusinessError("题目不存在", 404))
		}
		panic(exception.NewBusinessError("查询题目失败: "+err.Error(), 500))
	}

	// Create submission
	now := time.Now()
	entity := JudgeSubmission{
		ID:        utils.GenerateID(),
		UserID:    userID,
		ProblemID: param.ProblemID,
		ContestID: param.ContestID,
		ContestMode: param.ContestMode,
		IsPretest: param.IsPretest,
		Language:  param.Language,
		Code:      param.Code,
		Status:    StatusPending,
		CreatedAt: &now,
		UpdatedAt: &now,
	}
	if userID != "" {
		entity.CreatedBy = &userID
		entity.UpdatedBy = &userID
	}

	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("创建提交失败: "+err.Error(), 500))
	}

	// Enqueue
	msg := &queue.Message{
		SubmissionID: entity.ID,
		ProblemID:    entity.ProblemID,
		UserID:       entity.UserID,
		Language:     entity.Language,
		JudgeMethod:  problem.JudgeMethod,
		ContestID:    derefStr(entity.ContestID),
		ContestMode:  entity.ContestMode,
		IsPretest:    entity.IsPretest,
		Code:         entity.Code,
	}
	if err := queue.EnqueueSubmission(msg); err != nil {
		// Log error but don't fail - submission is already created
		panic(exception.NewBusinessError("提交已创建，但入队失败: "+err.Error(), 500))
	}

	return entToVOWithDetail(c, &entity)
}

func GetTestcaseResults(c *gin.Context, submissionID string) gin.H {
	ctx := context.Background()
	var results []JudgeTestcaseResult
	db.DB.WithContext(ctx).Where("submission_id = ?", submissionID).Order("`index` ASC").Find(&results)
	return result.Success(c, results)
}

func entToVO(entity *JudgeSubmission) *SubmissionVO {
	vo := &SubmissionVO{
		ID:            entity.ID,
		UserID:        entity.UserID,
		ProblemID:     entity.ProblemID,
		ContestID:     entity.ContestID,
		ContestMode:   entity.ContestMode,
		IsPretest:     entity.IsPretest,
		Language:      entity.Language,
		Code:          entity.Code,
		Status:        entity.Status,
		Score:         entity.Score,
		TimeUsed:      entity.TimeUsed,
		MemoryUsed:    entity.MemoryUsed,
		TestcasePass:  entity.TestcasePass,
		TestcaseTotal: entity.TestcaseTotal,
		ErrorInfo:     entity.ErrorInfo,
	}
	if entity.CreatedAt != nil {
		s := entity.CreatedAt.Format("2006-01-02 15:04:05")
		vo.CreatedAt = &s
	}
	if entity.UpdatedAt != nil {
		s := entity.UpdatedAt.Format("2006-01-02 15:04:05")
		vo.UpdatedAt = &s
	}
	return vo
}

func derefStr(s *string) string {
	if s == nil { return "" }
	return *s
}

// Rejudge resets a submission and re-queues it for judging.
func Rejudge(c *gin.Context, id string) {
	ctx := context.Background()
	var entity JudgeSubmission
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		panic(exception.NewBusinessError("提交不存在", 404))
	}

	// Reset submission status
	now := time.Now()
	db.DB.WithContext(ctx).Model(&JudgeSubmission{}).Where("id = ?", id).Updates(map[string]interface{}{
		"status":         StatusPending,
		"score":          0,
		"time_used":      0,
		"memory_used":    0,
		"testcase_pass":  0,
		"testcase_total": 0,
		"error_info":     "",
		"updated_at":     now,
	})

	// Delete old testcase results
	db.DB.WithContext(ctx).Where("submission_id = ?", id).Delete(&JudgeTestcaseResult{})

	// Get problem for judge method
	var problem struct{ JudgeMethod string }
	db.DB.WithContext(ctx).Table("judge_problem").Select("judge_method").Where("id = ?", entity.ProblemID).Scan(&problem)

	// Re-enqueue
	msg := &queue.Message{
		SubmissionID: entity.ID,
		ProblemID:    entity.ProblemID,
		UserID:       entity.UserID,
		Language:     entity.Language,
		JudgeMethod:  problem.JudgeMethod,
		ContestID:    derefStr(entity.ContestID),
		ContestMode:  entity.ContestMode,
		IsPretest:    entity.IsPretest,
		Code:         entity.Code,
	}
	if err := queue.EnqueueSubmission(msg); err != nil {
		panic(exception.NewBusinessError("重判入队失败: "+err.Error(), 500))
	}
}

// entToVOWithDetail extends entToVO with problem info (judge_method, title).
func entToVOWithDetail(c *gin.Context, entity *JudgeSubmission) *SubmissionVO {
	vo := entToVO(entity)
	// Load problem info
	ctx := context.Background()
	var problem struct {
		Title       string
		JudgeMethod string
	}
	db.DB.WithContext(ctx).Table("judge_problem").
		Select("title, judge_method").
		Where("id = ?", entity.ProblemID).
		Scan(&problem)
	vo.ProblemTitle = problem.Title
	vo.JudgeMethod = problem.JudgeMethod
	return vo
}

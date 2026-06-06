package submission

import (
	"context"
	"errors"
	"time"

	"gorm.io/gorm"

	"hei-gin/sdk/auth"
	"hei-gin/sdk/db"
	"hei-gin/sdk/result"
	"hei-gin/sdk/utils"

	"hei-gin/plugins/plugin-judge/judge"
	"hei-gin/plugins/plugin-judge/problem"
	"hei-gin/plugins/plugin-judge/sandbox"

	"github.com/gin-gonic/gin"
)

// PageService 提交记录分页查询
func PageService(c *gin.Context, param *SubmissionPageParam) gin.H {
	ctx := context.Background()
	tx := db.DB.WithContext(ctx).Model(&JudgeSubmission{})

	if param.ProblemID != "" {
		tx = tx.Where("problem_id = ?", param.ProblemID)
	}
	if param.UserID != "" {
		tx = tx.Where("user_id = ?", param.UserID)
	}
	if param.Status != "" {
		tx = tx.Where("status = ?", param.Status)
	}
	if param.Language != "" {
		tx = tx.Where("language = ?", param.Language)
	}
	if param.ContestID != "" {
		tx = tx.Where("contest_id = ?", param.ContestID)
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

	var submissions []JudgeSubmission
	tx.Offset((page - 1) * size).Limit(size).Order("created_at DESC").Find(&submissions)

	problemIDs := make([]string, 0)
	userIDs := make([]string, 0)
	problemMap := make(map[string]string)
	userMap := make(map[string]string)

	for _, s := range submissions {
		problemIDs = append(problemIDs, s.ProblemID)
		userIDs = append(userIDs, s.UserID)
	}

	if len(problemIDs) > 0 {
		var problems []problem.JudgeProblem
		db.DB.WithContext(ctx).Where("id IN ?", problemIDs).Find(&problems)
		for _, p := range problems {
			problemMap[p.ID] = p.Title
		}
	}

	if len(userIDs) > 0 {
		type UserBrief struct {
			ID       string `gorm:"column:id"`
			Username string `gorm:"column:username"`
		}
		var users []UserBrief
		db.DB.WithContext(ctx).Table("sys_user").Where("id IN ?", userIDs).Find(&users)
		for _, u := range users {
			userMap[u.ID] = u.Username
		}
	}

	voList := make([]SubmissionVO, len(submissions))
	for i, s := range submissions {
		vo := modelToVO(&s)
		vo.ProblemTitle = problemMap[s.ProblemID]
		vo.Username = userMap[s.UserID]
		voList[i] = vo
	}

	return result.PageDataResult(c, voList, total, page, size)
}

// CreateService 创建提交并入队判题
func CreateService(c *gin.Context, param *SubmissionCreateParam, judgeEngine *judge.JudgeEngine) error {
	ctx := context.Background()
	userID := auth.GetLoginID(c)
	now := time.Now()

	var prob problem.JudgeProblem
	if err := db.DB.WithContext(ctx).Where("id = ? AND status = ?", param.ProblemID, "ACTIVE").First(&prob).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return errors.New("题目不存在或未激活")
		}
		return err
	}

	if sandbox.DefaultPool.Get() == nil {
		return errors.New("无可用判题节点")
	}

	submission := JudgeSubmission{
		ID:           utils.GenerateID(),
		ProblemID:    param.ProblemID,
		UserID:       userID,
		ContestID:    param.ContestID,
		Language:     param.Language,
		Code:         param.Code,
		Status:       "PENDING",
		Score:        0,
		ErrorMessage: "",
		CreatedAt:    &now,
		UpdatedAt:    &now,
	}

	if err := db.DB.WithContext(ctx).Create(&submission).Error; err != nil {
		return err
	}

	judgeEngine.Submit(&judge.JudgeTask{
		SubmissionID:    submission.ID,
		ProblemID:       prob.ID,
		UserID:          userID,
		Language:        param.Language,
		Code:            param.Code,
		JudgeType:       prob.JudgeType,
		TimeLimit:       prob.TimeLimit,
		MemoryLimit:     prob.MemoryLimit,
		StackLimit:      prob.StackLimit,
		OutputLimit:     prob.OutputLimit,
		SpjCode:         prob.SpjCode,
		SpjLanguage:     prob.SpjLanguage,
		InteractiveCode: prob.InteractiveCode,
		InteractiveLang: prob.InteractiveLang,
	})

	return nil
}

// DetailService 提交详情
func DetailService(c *gin.Context, id string) (*SubmissionVO, error) {
	ctx := context.Background()
	var sub JudgeSubmission
	if err := db.DB.WithContext(ctx).Where("id = ?", id).First(&sub).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, errors.New("提交记录不存在")
		}
		return nil, err
	}

	vo := modelToVO(&sub)

	var prob problem.JudgeProblem
	db.DB.WithContext(ctx).Where("id = ?", sub.ProblemID).First(&prob)
	vo.ProblemTitle = prob.Title

	type UserBrief struct {
		Username string `gorm:"column:username"`
	}
	var user UserBrief
	db.DB.WithContext(ctx).Table("sys_user").Where("id = ?", sub.UserID).First(&user)
	vo.Username = user.Username

	return &vo, nil
}

// RejudgeService 重新判题
func RejudgeService(c *gin.Context, param SubmissionRejudgeParam, judgeEngine *judge.JudgeEngine) error {
	ctx := context.Background()
	var submissions []JudgeSubmission
	if err := db.DB.WithContext(ctx).Where("id IN ?", param.IDs).Find(&submissions).Error; err != nil {
		return err
	}

	for _, sub := range submissions {
		var prob problem.JudgeProblem
		if err := db.DB.WithContext(ctx).Where("id = ?", sub.ProblemID).First(&prob).Error; err != nil {
			continue
		}

		db.DB.WithContext(ctx).Model(&sub).Updates(map[string]any{
			"status":        "PENDING",
			"score":         0,
			"time_used":     0,
			"memory_used":   0,
			"error_message": "",
		})

		judgeEngine.Submit(&judge.JudgeTask{
			SubmissionID:    sub.ID,
			ProblemID:       prob.ID,
			UserID:          sub.UserID,
			Language:        sub.Language,
			Code:            sub.Code,
			JudgeType:       prob.JudgeType,
			TimeLimit:       prob.TimeLimit,
			MemoryLimit:     prob.MemoryLimit,
			StackLimit:      prob.StackLimit,
			OutputLimit:     prob.OutputLimit,
			SpjCode:         prob.SpjCode,
			SpjLanguage:     prob.SpjLanguage,
			InteractiveCode: prob.InteractiveCode,
			InteractiveLang: prob.InteractiveLang,
		})
	}

	return nil
}

func modelToVO(s *JudgeSubmission) SubmissionVO {
	createdAt := ""
	if s.CreatedAt != nil {
		createdAt = s.CreatedAt.Format("2006-01-02 15:04:05")
	}
	return SubmissionVO{
		ID:           s.ID,
		ProblemID:    s.ProblemID,
		UserID:       s.UserID,
		ContestID:    s.ContestID,
		Language:     s.Language,
		Code:         s.Code,
		Status:       s.Status,
		Score:        s.Score,
		TimeUsed:     s.TimeUsed,
		MemoryUsed:   s.MemoryUsed,
		ErrorMessage: s.ErrorMessage,
		CreatedAt:    createdAt,
	}
}

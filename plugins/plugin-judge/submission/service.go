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
	if param.SubmissionType != "" {
		tx = tx.Where("submission_type = ?", param.SubmissionType)
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
		type ClientUserBrief struct {
			ID       string `gorm:"column:id"`
			Nickname string `gorm:"column:nickname"`
		}
		var clientUsers []ClientUserBrief
		db.DB.WithContext(ctx).Table("client_user").Where("id IN ?", userIDs).Find(&clientUsers)
		for _, u := range clientUsers {
			userMap[u.ID] = u.Nickname
		}
		// 未找到的用户显示 ID
		for _, uid := range userIDs {
			if _, ok := userMap[uid]; !ok {
				userMap[uid] = uid
			}
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

// CreateService 创建提交并入队判题（B端管理员）
func CreateService(c *gin.Context, param *SubmissionCreateParam, judgeEngine *judge.JudgeEngine) error {
	userID := auth.GetLoginID(c)
	if param.SubmissionType == "" {
		param.SubmissionType = "test"
	}
	return createSubmission(c, param, judgeEngine, userID)
}

// ClientCreateService 创建提交并入队判题（C端用户），返回 submissionID
func ClientCreateService(c *gin.Context, param *SubmissionCreateParam, judgeEngine *judge.JudgeEngine) (string, error) {
	userID := auth.Consumer.GetLoginID(c)
	if param.SubmissionType == "" {
		param.SubmissionType = "contest"
	}
	return createSubmissionWithID(c, param, judgeEngine, userID)
}

// buildJudgeTask 构建判题任务
func buildJudgeTask(prob *problem.JudgeProblem, submissionID, userID, language, code string, contestID ...string) *judge.JudgeTask {
	submissionType := getSubmissionType(submissionID)
	limits := problem.GetEffectiveLimits(prob.ID, language)

	finalCode := code
	if finalCode == "" {
		tpl := problem.GetLanguageTemplate(prob.ID, language)
		if tpl != "" {
			finalCode = tpl
		}
	}

	task := &judge.JudgeTask{
		SubmissionID:    submissionID,
		ProblemID:       prob.ID,
		UserID:          userID,
		Language:        language,
		Code:            finalCode,
		JudgeType:       prob.JudgeType,
		TimeLimit:       limits.TimeLimit,
		MemoryLimit:     limits.MemoryLimit,
		StackLimit:      limits.StackLimit,
		OutputLimit:     limits.OutputLimit,
		StrictCompare:   prob.StrictCompare,
		SubmissionType:  submissionType,
		SpjCode:         prob.SpjCode,
		SpjLanguage:     prob.SpjLanguage,
		InteractiveCode: prob.InteractiveCode,
		InteractiveLang: prob.InteractiveLang,
	}

	if len(contestID) > 0 && contestID[0] != "" {
		task.ContestID = contestID[0]
		type ContestBrief struct {
			Type string `gorm:"column:type"`
		}
		var cb ContestBrief
		if err := db.DB.Table("judge_contest").Select("`type`").Where("id = ?", contestID[0]).First(&cb).Error; err == nil {
			task.ContestType = cb.Type
		}
	}

	return task
}

func createSubmissionWithID(c *gin.Context, param *SubmissionCreateParam, judgeEngine *judge.JudgeEngine, userID string) (string, error) {
	ctx := context.Background()
	now := time.Now()

	var prob problem.JudgeProblem
	if err := db.DB.WithContext(ctx).Where("id = ? AND status = ?", param.ProblemID, "ACTIVE").First(&prob).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return "", errors.New("题目不存在或未激活")
		}
		return "", err
	}

	if sandbox.DefaultPool.Get() == nil {
		return "", errors.New("无可用判题节点")
	}

	submission := JudgeSubmission{
		ID:             utils.GenerateID(),
		ProblemID:      param.ProblemID,
		UserID:         userID,
		ContestID:      param.ContestID,
		Language:       param.Language,
		Code:           param.Code,
		Status:         "PENDING",
		SubmissionType: param.SubmissionType,
		Score:          0,
		ErrorMessage:   "",
		CreatedAt:      &now,
		UpdatedAt:      &now,
	}

	if err := db.DB.WithContext(ctx).Create(&submission).Error; err != nil {
		return "", err
	}

	judgeEngine.Submit(buildJudgeTask(&prob, submission.ID, userID, param.Language, param.Code, param.ContestID))

	return submission.ID, nil
}

func createSubmission(c *gin.Context, param *SubmissionCreateParam, judgeEngine *judge.JudgeEngine, userID string) error {
	ctx := context.Background()
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
		ID:             utils.GenerateID(),
		ProblemID:      param.ProblemID,
		UserID:         userID,
		ContestID:      param.ContestID,
		Language:       param.Language,
		Code:           param.Code,
		Status:         "PENDING",
		SubmissionType: param.SubmissionType,
		Score:          0,
		ErrorMessage:   "",
		CreatedAt:      &now,
		UpdatedAt:      &now,
	}

	if err := db.DB.WithContext(ctx).Create(&submission).Error; err != nil {
		return err
	}

	judgeEngine.Submit(buildJudgeTask(&prob, submission.ID, userID, param.Language, param.Code, param.ContestID))

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

	// 只查 client_user（C端用户），不 fallback 到 sys_user
	type ClientUserBrief struct {
		Nickname string `gorm:"column:nickname"`
	}
	var cu ClientUserBrief
	if err := db.DB.WithContext(ctx).Table("client_user").Where("id = ?", sub.UserID).First(&cu).Error; err == nil && cu.Nickname != "" {
		vo.Username = cu.Nickname
	} else {
		vo.Username = sub.UserID
	}

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

		judgeEngine.Submit(buildJudgeTask(&prob, sub.ID, sub.UserID, sub.Language, sub.Code, sub.ContestID))
	}

	return nil
}

func modelToVO(s *JudgeSubmission) SubmissionVO {
	createdAt := ""
	if s.CreatedAt != nil {
		createdAt = s.CreatedAt.Format("2006-01-02 15:04:05")
	}
	return SubmissionVO{
		ID:             s.ID,
		ProblemID:      s.ProblemID,
		UserID:         s.UserID,
		ContestID:      s.ContestID,
		Language:       s.Language,
		Code:           s.Code,
		Status:         s.Status,
		SubmissionType: s.SubmissionType,
		Score:          s.Score,
		TimeUsed:       s.TimeUsed,
		MemoryUsed:     s.MemoryUsed,
		ErrorMessage:   s.ErrorMessage,
		CreatedAt:      createdAt,
	}
}

func getSubmissionType(submissionID string) string {
	ctx := context.Background()
	var sub JudgeSubmission
	if err := db.DB.WithContext(ctx).Select("submission_type").Where("id = ?", submissionID).First(&sub).Error; err != nil {
		return "contest"
	}
	if sub.SubmissionType == "" {
		return "contest"
	}
	return sub.SubmissionType
}
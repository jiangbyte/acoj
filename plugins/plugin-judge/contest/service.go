package contest

import (
	"context"
	"errors"
	"time"

	"gorm.io/gorm"

	"hei-gin/sdk/auth"
	"hei-gin/sdk/db"
	"hei-gin/sdk/result"
	"hei-gin/sdk/utils"

	"hei-gin/plugins/plugin-judge/problem"

	"github.com/gin-gonic/gin"
)

// PageService 竞赛分页
func PageService(c *gin.Context, param *ContestPageParam) gin.H {
	ctx := context.Background()
	tx := db.DB.WithContext(ctx).Model(&JudgeContest{})

	if param.Keyword != "" {
		tx = tx.Where("title LIKE ?", "%"+param.Keyword+"%")
	}
	if param.Type != "" {
		tx = tx.Where("type = ?", param.Type)
	}
	if param.Status != "" {
		tx = tx.Where("status = ?", param.Status)
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

	var contests []JudgeContest
	tx.Offset((page - 1) * size).Limit(size).Order("start_time DESC").Find(&contests)

	contestIDs := make([]string, len(contests))
	for i, c := range contests {
		contestIDs[i] = c.ID
	}

	type CountResult struct {
		ContestID string `gorm:"column:contest_id"`
		Count     int    `gorm:"column:cnt"`
	}

	problemCounts := make(map[string]int)
	if len(contestIDs) > 0 {
		var counts []CountResult
		db.DB.WithContext(ctx).Table("rel_contest_problem").
			Select("contest_id, COUNT(*) as cnt").
			Where("contest_id IN ?", contestIDs).
			Group("contest_id").
			Find(&counts)
		for _, c := range counts {
			problemCounts[c.ContestID] = c.Count
		}
	}

	userCounts := make(map[string]int)
	if len(contestIDs) > 0 {
		var counts []CountResult
		db.DB.WithContext(ctx).Table("rel_contest_user").
			Select("contest_id, COUNT(*) as cnt").
			Where("contest_id IN ?", contestIDs).
			Group("contest_id").
			Find(&counts)
		for _, c := range counts {
			userCounts[c.ContestID] = c.Count
		}
	}

	voList := make([]ContestVO, len(contests))
	for i, c := range contests {
		vo := modelToVO(&c)
		vo.ProblemCount = problemCounts[c.ID]
		vo.UserCount = userCounts[c.ID]
		voList[i] = vo
	}

	return result.PageDataResult(c, voList, total, page, size)
}

// CreateService 创建竞赛
func CreateService(c *gin.Context, param *ContestCreateParam) error {
	ctx := context.Background()
	now := time.Now()
	userID := auth.GetLoginID(c)

	startTime, err := time.Parse("2006-01-02 15:04:05", param.StartTime)
	if err != nil {
		return err
	}
	endTime, err := time.Parse("2006-01-02 15:04:05", param.EndTime)
	if err != nil {
		return err
	}

	contest := JudgeContest{
		ID:          utils.GenerateID(),
		Title:       param.Title,
		Description: param.Description,
		Type:        param.Type,
		Rule:        param.Rule,
		Password:    param.Password,
		StartTime:   &startTime,
		EndTime:     &endTime,
		Status:      "PENDING",
		CreatedBy:   userID,
		CreatedAt:   &now,
		UpdatedAt:   &now,
	}
	if contest.Type == "" {
		contest.Type = "ACM"
	}
	if contest.Rule == "" {
		contest.Rule = "PRIVATE"
	}

	return db.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Create(&contest).Error; err != nil {
			return err
		}
		for i, pid := range param.ProblemIDs {
			rel := RelContestProblem{
				ID:        utils.GenerateID(),
				ContestID: contest.ID,
				ProblemID: pid,
				Sort:      i,
				Score:     100,
			}
			if err := tx.Create(&rel).Error; err != nil {
				return err
			}
		}
		return nil
	})
}

// ModifyService 编辑竞赛
func ModifyService(c *gin.Context, param *ContestModifyParam) error {
	ctx := context.Background()
	updates := map[string]any{}
	if param.Title != "" {
		updates["title"] = param.Title
	}
	if param.Description != "" {
		updates["description"] = param.Description
	}
	if param.Type != "" {
		updates["type"] = param.Type
	}
	if param.Rule != "" {
		updates["rule"] = param.Rule
	}
	if param.Password != "" {
		updates["password"] = param.Password
	}
	if param.StartTime != "" {
		t, err := time.Parse("2006-01-02 15:04:05", param.StartTime)
		if err == nil {
			updates["start_time"] = t
		}
	}
	if param.EndTime != "" {
		t, err := time.Parse("2006-01-02 15:04:05", param.EndTime)
		if err == nil {
			updates["end_time"] = t
		}
	}
	updates["updated_at"] = time.Now()

	return db.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Model(&JudgeContest{}).Where("id = ?", param.ID).Updates(updates).Error; err != nil {
			return err
		}
		if param.ProblemIDs != nil {
			tx.Where("contest_id = ?", param.ID).Delete(&RelContestProblem{})
			for i, pid := range param.ProblemIDs {
				rel := RelContestProblem{
					ID:        utils.GenerateID(),
					ContestID: param.ID,
					ProblemID: pid,
					Sort:      i,
					Score:     100,
				}
				if err := tx.Create(&rel).Error; err != nil {
					return err
				}
			}
		}
		return nil
	})
}

// RemoveService 删除竞赛
func RemoveService(c *gin.Context, param ContestRemoveParam) error {
	ctx := context.Background()
	return db.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Where("id IN ?", param.IDs).Delete(&JudgeContest{}).Error; err != nil {
			return err
		}
		tx.Where("contest_id IN ?", param.IDs).Delete(&RelContestProblem{})
		tx.Where("contest_id IN ?", param.IDs).Delete(&RelContestUser{})
		return nil
	})
}

// DetailService 竞赛详情（B端，不检测报名状态）
func DetailService(c *gin.Context, id string) (*ContestDetailVO, error) {
	return getContestDetail(c, id, "")
}

// PublicDetailService 公开竞赛详情（C端，检测用户登录状态并返回 is_registered）
func PublicDetailService(c *gin.Context, id string) (*ContestDetailVO, error) {
	// 可选地检测C端用户是否已登录，不强制
	userID := auth.Consumer.GetLoginIDDefaultNull(c)
	return getContestDetail(c, id, userID)
}

// getContestDetail 内部详情查询
func getContestDetail(c *gin.Context, id, userID string) (*ContestDetailVO, error) {
	ctx := context.Background()
	var contest JudgeContest
	if err := db.DB.WithContext(ctx).Where("id = ?", id).First(&contest).Error; err != nil {
		return nil, err
	}

	vo := &ContestDetailVO{}
	vo.ContestVO = modelToVO(&contest)
	vo.IsRegistered = false

	// 检测当前用户是否已报名
	if userID != "" {
		var count int64
		db.DB.WithContext(ctx).Model(&RelContestUser{}).
			Where("contest_id = ? AND user_id = ?", id, userID).
			Count(&count)
		vo.IsRegistered = count > 0
	}

	var rels []RelContestProblem
	db.DB.WithContext(ctx).Where("contest_id = ?", id).Order("sort ASC").Find(&rels)

	var problemIDs []string
	for _, r := range rels {
		problemIDs = append(problemIDs, r.ProblemID)
	}

	titleMap := make(map[string]string)
	if len(problemIDs) > 0 {
		var problems []problem.JudgeProblem
		db.DB.WithContext(ctx).Where("id IN ?", problemIDs).Find(&problems)
		for _, p := range problems {
			titleMap[p.ID] = p.Title
		}
	}

	for _, r := range rels {
		vo.Problems = append(vo.Problems, ContestProblemItem{
			ProblemID: r.ProblemID,
			Title:     titleMap[r.ProblemID],
			Sort:      r.Sort,
			Score:     r.Score,
		})
	}

	var count int64
	db.DB.WithContext(ctx).Model(&RelContestUser{}).Where("contest_id = ?", id).Count(&count)
	vo.UserCount = int(count)
	vo.ProblemCount = len(vo.Problems)

	return vo, nil
}

// RegisterService 报名竞赛（B端管理员）
func RegisterService(c *gin.Context, param *ContestRegisterParam) error {
	return doRegister(c, param, auth.GetLoginID(c))
}

// ClientRegisterService 报名竞赛（C端用户）
func ClientRegisterService(c *gin.Context, param *ContestRegisterParam) error {
	return doRegister(c, param, auth.Consumer.GetLoginID(c))
}

func doRegister(c *gin.Context, param *ContestRegisterParam, userID string) error {
	ctx := context.Background()

	var contest JudgeContest
	if err := db.DB.WithContext(ctx).Where("id = ?", param.ContestID).First(&contest).Error; err != nil {
		return errors.New("竞赛不存在")
	}

	if contest.Rule == "PASSWORD" && contest.Password != param.Password {
		return errors.New("密码错误")
	}

	var count int64
	db.DB.WithContext(ctx).Model(&RelContestUser{}).
		Where("contest_id = ? AND user_id = ?", param.ContestID, userID).
		Count(&count)
	if count > 0 {
		return nil
	}

	now := time.Now()
	rel := RelContestUser{
		ID:        utils.GenerateID(),
		ContestID: param.ContestID,
		UserID:    userID,
		CreatedAt: &now,
	}
	return db.DB.WithContext(ctx).Create(&rel).Error
}

func modelToVO(c *JudgeContest) ContestVO {
	startTime := ""
	if c.StartTime != nil {
		startTime = c.StartTime.Format("2006-01-02 15:04:05")
	}
	endTime := ""
	if c.EndTime != nil {
		endTime = c.EndTime.Format("2006-01-02 15:04:05")
	}
	createdAt := ""
	if c.CreatedAt != nil {
		createdAt = c.CreatedAt.Format("2006-01-02 15:04:05")
	}
	return ContestVO{
		ID:          c.ID,
		Title:       c.Title,
		Description: c.Description,
		Type:        c.Type,
		Rule:        c.Rule,
		StartTime:   startTime,
		EndTime:     endTime,
		Status:      c.Status,
		CreatedBy:   c.CreatedBy,
		CreatedAt:   createdAt,
	}
}

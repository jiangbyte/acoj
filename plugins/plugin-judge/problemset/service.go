package problemset

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

// PageService 题单分页
func PageService(c *gin.Context, param *ProblemsetPageParam) gin.H {
	ctx := context.Background()
	tx := db.DB.WithContext(ctx).Model(&JudgeProblemset{})

	if param.Keyword != "" {
		tx = tx.Where("title LIKE ?", "%"+param.Keyword+"%")
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

	var sets []JudgeProblemset
	tx.Offset((page - 1) * size).Limit(size).Order("sort ASC, created_at DESC").Find(&sets)

	// 查询每个题单的题目数量
	setIDs := make([]string, len(sets))
	for i, s := range sets {
		setIDs[i] = s.ID
	}

	type CountResult struct {
		ProblemsetID string `gorm:"column:problemset_id"`
		Count        int    `gorm:"column:cnt"`
	}
	var counts []CountResult
	if len(setIDs) > 0 {
		db.DB.WithContext(ctx).Table("rel_problemset_problem").
			Select("problemset_id, COUNT(*) as cnt").
			Where("problemset_id IN ?", setIDs).
			Group("problemset_id").
			Find(&counts)
	}
	countMap := make(map[string]int)
	for _, c := range counts {
		countMap[c.ProblemsetID] = c.Count
	}

	voList := make([]ProblemsetVO, len(sets))
	for i, s := range sets {
		vo := modelToVO(&s)
		vo.ProblemCount = countMap[s.ID]
		voList[i] = vo
	}

	return result.PageDataResult(c, voList, total, page, size)
}

// CreateService 创建题单（B端管理员）
func CreateService(c *gin.Context, param *ProblemsetCreateParam) error {
	return createProblemset(c, param, "BUSINESS", func() string { return auth.GetLoginID(c) })
}

// ClientCreateService 创建题单（C端用户）
func ClientCreateService(c *gin.Context, param *ProblemsetCreateParam) error {
	return createProblemset(c, param, "CONSUMER", func() string { return auth.Consumer.GetLoginID(c) })
}

func createProblemset(c *gin.Context, param *ProblemsetCreateParam, createdByType string, getLoginID func() string) error {
	ctx := context.Background()
	now := time.Now()

	loginID := getLoginID()
	ps := JudgeProblemset{
		ID:            utils.GenerateID(),
		Title:         param.Title,
		Description:   param.Description,
		Status:        param.Status,
		Sort:          param.Sort,
		CreatedBy:     &loginID,
		CreatedByType: createdByType,
		CreatedAt:     &now,
		UpdatedAt:     &now,
	}
	if ps.Status == "" {
		ps.Status = "ACTIVE"
	}

	return db.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Create(&ps).Error; err != nil {
			return err
		}
		for i, pid := range param.ProblemIDs {
			rel := RelProblemsetProblem{
				ID:           utils.GenerateID(),
				ProblemsetID: ps.ID,
				ProblemID:    pid,
				Sort:         i,
			}
			if err := tx.Create(&rel).Error; err != nil {
				return err
			}
		}
		return nil
	})
}

// ModifyService 编辑题单
func ModifyService(c *gin.Context, param *ProblemsetModifyParam) error {
	ctx := context.Background()
	updates := map[string]any{}
	if param.Title != "" {
		updates["title"] = param.Title
	}
	if param.Description != "" {
		updates["description"] = param.Description
	}
	if param.Status != "" {
		updates["status"] = param.Status
	}
	if param.Sort != nil {
		updates["sort"] = *param.Sort
	}
	updates["updated_at"] = time.Now()

	return db.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Model(&JudgeProblemset{}).Where("id = ?", param.ID).Updates(updates).Error; err != nil {
			return err
		}
		if param.ProblemIDs != nil {
			tx.Where("problemset_id = ?", param.ID).Delete(&RelProblemsetProblem{})
			for i, pid := range param.ProblemIDs {
				rel := RelProblemsetProblem{
					ID:           utils.GenerateID(),
					ProblemsetID: param.ID,
					ProblemID:    pid,
					Sort:         i,
				}
				if err := tx.Create(&rel).Error; err != nil {
					return err
				}
			}
		}
		return nil
	})
}

// RemoveService 删除题单
func RemoveService(c *gin.Context, param ProblemsetRemoveParam) error {
	ctx := context.Background()
	return db.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Where("id IN ?", param.IDs).Delete(&JudgeProblemset{}).Error; err != nil {
			return err
		}
		tx.Where("problemset_id IN ?", param.IDs).Delete(&RelProblemsetProblem{})
		return nil
	})
}

// DetailService 题单详情（含题目列表）
func DetailService(c *gin.Context, id string) (*ProblemsetDetailVO, error) {
	ctx := context.Background()
	var ps JudgeProblemset
	if err := db.DB.WithContext(ctx).Where("id = ?", id).First(&ps).Error; err != nil {
		return nil, err
	}

	vo := &ProblemsetDetailVO{}
	vo.ProblemsetVO = modelToVO(&ps)

	// 查询关联题目
	var rels []RelProblemsetProblem
	db.DB.WithContext(ctx).Where("problemset_id = ?", id).Order("sort ASC").Find(&rels)

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
		vo.Problems = append(vo.Problems, ProblemsetProblemItem{
			ProblemID: r.ProblemID,
			Title:     titleMap[r.ProblemID],
			Sort:      r.Sort,
		})
	}
	vo.ProblemCount = len(vo.Problems)

	return vo, nil
}

// ClientMySetsService 获取当前C端用户的题单列表
func ClientMySetsService(c *gin.Context, param *ProblemsetPageParam) gin.H {
	ctx := context.Background()
	userID := auth.Consumer.GetLoginID(c)

	tx := db.DB.WithContext(ctx).Model(&JudgeProblemset{}).Where("created_by = ? AND created_by_type = ?", userID, "CONSUMER")

	if param.Keyword != "" {
		tx = tx.Where("title LIKE ?", "%"+param.Keyword+"%")
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

	var sets []JudgeProblemset
	tx.Offset((page - 1) * size).Limit(size).Order("updated_at DESC").Find(&sets)

	setIDs := make([]string, len(sets))
	for i, s := range sets {
		setIDs[i] = s.ID
	}

	type CountResult struct {
		ProblemsetID string `gorm:"column:problemset_id"`
		Count        int    `gorm:"column:cnt"`
	}
	var counts []CountResult
	if len(setIDs) > 0 {
		db.DB.WithContext(ctx).Table("rel_problemset_problem").
			Select("problemset_id, COUNT(*) as cnt").
			Where("problemset_id IN ?", setIDs).
			Group("problemset_id").
			Find(&counts)
	}
	countMap := make(map[string]int)
	for _, c := range counts {
		countMap[c.ProblemsetID] = c.Count
	}

	voList := make([]ProblemsetVO, len(sets))
	for i, s := range sets {
		vo := modelToVO(&s)
		vo.ProblemCount = countMap[s.ID]
		voList[i] = vo
	}

	return result.PageDataResult(c, voList, total, page, size)
}

func modelToVO(ps *JudgeProblemset) ProblemsetVO {
	createdAt := ""
	if ps.CreatedAt != nil {
		createdAt = ps.CreatedAt.Format("2006-01-02 15:04:05")
	}
	return ProblemsetVO{
		ID:          ps.ID,
		Title:       ps.Title,
		Description: ps.Description,
		Status:      ps.Status,
		Sort:        ps.Sort,
		CreatedBy:   strPtrToStr(ps.CreatedBy),
		CreatedAt:   createdAt,
	}
}

// ClientModifyService 编辑题单（C端用户，校验归属）
func ClientModifyService(c *gin.Context, param *ProblemsetModifyParam) error {
	ctx := context.Background()
	userID := auth.Consumer.GetLoginID(c)

	var ps JudgeProblemset
	if err := db.DB.WithContext(ctx).Where("id = ? AND created_by = ? AND created_by_type = ?", param.ID, userID, "CONSUMER").First(&ps).Error; err != nil {
		return errors.New("题单不存在或无权修改")
	}
	return ModifyService(c, param)
}

// ClientRemoveService 删除题单（C端用户，校验归属）
func ClientRemoveService(c *gin.Context, param ProblemsetRemoveParam) error {
	ctx := context.Background()
	userID := auth.Consumer.GetLoginID(c)

	for _, id := range param.IDs {
		var ps JudgeProblemset
		if err := db.DB.WithContext(ctx).Where("id = ? AND created_by = ? AND created_by_type = ?", id, userID, "CONSUMER").First(&ps).Error; err != nil {
			return errors.New("题单不存在或无权删除")
		}
	}
	return RemoveService(c, param)
}

// strPtrToStr 安全解引用 *string，nil 返回空串
func strPtrToStr(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

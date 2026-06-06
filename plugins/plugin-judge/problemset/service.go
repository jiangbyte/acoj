package problemset

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/sdk/auth"
	"hei-gin/sdk/db"
	"hei-gin/sdk/result"
	"hei-gin/sdk/utils"

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

// CreateService 创建题单
func CreateService(c *gin.Context, param *ProblemsetCreateParam) error {
	ctx := context.Background()
	now := time.Now()
	userID := auth.GetLoginID(c)

	ps := JudgeProblemset{
		ID:          utils.GenerateID(),
		Title:       param.Title,
		Description: param.Description,
		Status:      param.Status,
		Sort:        param.Sort,
		CreatedBy:   userID,
		CreatedAt:   &now,
		UpdatedAt:   &now,
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
func DetailService(c *gin.Context, id string) (*ProblemsetVO, error) {
	ctx := context.Background()
	var ps JudgeProblemset
	if err := db.DB.WithContext(ctx).Where("id = ?", id).First(&ps).Error; err != nil {
		return nil, err
	}

	vo := modelToVO(&ps)

	var count int64
	db.DB.WithContext(ctx).Model(&RelProblemsetProblem{}).Where("problemset_id = ?", id).Count(&count)
	vo.ProblemCount = int(count)

	return &vo, nil
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
		CreatedBy:   ps.CreatedBy,
		CreatedAt:   createdAt,
	}
}

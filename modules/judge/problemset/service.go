package problemset

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

func Page(c *gin.Context, param *ProblemSetPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 { param.Current = 1 }
	if param.Size < 1 { param.Size = 10 }

	query := db.DB.WithContext(ctx).Model(&JudgeProblemSet{})
	if param.Title != "" { query = query.Where("title LIKE ?", "%"+param.Title+"%") }
	if param.Type != "" { query = query.Where("type = ?", param.Type) }
	if param.Visibility != "" { query = query.Where("visibility = ?", param.Visibility) }

	var total int64
	query.Count(&total)

	var records []JudgeProblemSet
	offset := (param.Current - 1) * param.Size
	query.Order("created_at DESC").Limit(param.Size).Offset(offset).Find(&records)

	vos := make([]*ProblemSetVO, 0, len(records))
	for _, r := range records { vos = append(vos, entToVO(&r)) }
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func Detail(c *gin.Context, id string) *ProblemSetVO {
	ctx := context.Background()
	var entity JudgeProblemSet
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound { return nil }
		panic(exception.NewBusinessError("查询题单详情失败: "+err.Error(), 500))
	}
	vo := entToVO(&entity)

	var items []JudgeProblemSetItem
	db.DB.WithContext(ctx).Where("set_id = ?", id).Order("sort_order ASC").Find(&items)
	for _, item := range items {
		vo.Problems = append(vo.Problems, ProblemSetItemVO{
			ID: item.ID, ProblemID: item.ProblemID,
			SortOrder: item.SortOrder, Note: item.Note,
		})
	}
	return vo
}

func Create(c *gin.Context, param *ProblemSetCreateParam, userID string) {
	ctx := context.Background()
	now := time.Now()
	entity := JudgeProblemSet{
		ID: utils.GenerateID(), Title: param.Title, Description: param.Description,
		Type: param.Type, Visibility: param.Visibility, Status: "ENABLED",
		CreatedAt: &now, UpdatedAt: &now,
	}
	if param.Type == "" { entity.Type = "PERSONAL" }
	if param.Visibility == "" { entity.Visibility = "PUBLIC" }
	if userID != "" { entity.UserID = userID; entity.CreatedBy = &userID; entity.UpdatedBy = &userID }
	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("创建题单失败: "+err.Error(), 500))
	}
}

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	db.DB.WithContext(context.Background()).Where("id IN ?", ids).Delete(&JudgeProblemSet{})
}

func SyncProblems(c *gin.Context, setID string, problems []ProblemSetItemVO) {
	ctx := context.Background()
	db.DB.WithContext(ctx).Where("set_id = ?", setID).Delete(&JudgeProblemSetItem{})
	for i, p := range problems {
		entity := JudgeProblemSetItem{
			ID: utils.GenerateID(), SetID: setID, ProblemID: p.ProblemID,
			SortOrder: i, Note: p.Note,
		}
		db.DB.WithContext(ctx).Create(&entity)
	}
	db.DB.WithContext(ctx).Model(&JudgeProblemSet{}).Where("id = ?", setID).
		Update("problem_count", len(problems))
}

func GetProgress(c *gin.Context, setID, userID string) gin.H {
	ctx := context.Background()
	var progress []JudgeProblemSetProgress
	db.DB.WithContext(ctx).Where("set_id = ? AND user_id = ?", setID, userID).Find(&progress)
	return result.Success(c, progress)
}

func entToVO(entity *JudgeProblemSet) *ProblemSetVO {
	vo := &ProblemSetVO{
		ID: entity.ID, Title: entity.Title, Description: entity.Description,
		Type: entity.Type, Visibility: entity.Visibility, Status: entity.Status,
		ProblemCount: entity.ProblemCount,
	}
	if entity.CreatedAt != nil { s := entity.CreatedAt.Format("2006-01-02 15:04:05"); vo.CreatedAt = &s }
	if entity.UpdatedAt != nil { s := entity.UpdatedAt.Format("2006-01-02 15:04:05"); vo.UpdatedAt = &s }
	return vo
}

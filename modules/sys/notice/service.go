package notice

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysnotice"

	"entgo.io/ent/dialect/sql"
	"github.com/gin-gonic/gin"
)

// Page returns a paginated list of notices.
func Page(c *gin.Context, param *NoticePageParam) gin.H {
	ctx := context.Background()

	// Set defaults
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	offset := (param.Current - 1) * param.Size

	total, err := db.Client.SysNotice.Query().Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询通知列表失败: "+err.Error(), 500))
	}

	records, err := db.Client.SysNotice.Query().
		Order(sysnotice.ByCreatedAt(sql.OrderDesc())).
		Limit(param.Size).
		Offset(offset).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询通知列表失败: "+err.Error(), 500))
	}

	vos := make([]*NoticeVO, 0, len(records))
	for _, r := range records {
		vos = append(vos, entToVO(r))
	}

	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

// Detail returns a single notice by ID.
func Detail(c *gin.Context, id string) *NoticeVO {
	if id == "" {
		return nil
	}

	ctx := context.Background()
	entity, err := db.Client.SysNotice.Get(ctx, id)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil
		}
		panic(exception.NewBusinessError("查询通知详情失败: "+err.Error(), 500))
	}

	return entToVO(entity)
}

// Create creates a new notice.
func Create(c *gin.Context, vo *NoticeVO, userID string) {
	ctx := context.Background()
	now := time.Now()

	// Set defaults if not provided
	if vo.Level == "" {
		vo.Level = "NORMAL"
	}
	if vo.IsTop == "" {
		vo.IsTop = "NO"
	}
	if vo.Status == "" {
		vo.Status = "ENABLED"
	}

	builder := db.Client.SysNotice.Create().
		SetID(utils.GenerateID()).
		SetTitle(vo.Title).
		SetCategory(vo.Category).
		SetType(vo.Type).
		SetLevel(vo.Level).
		SetIsTop(vo.IsTop).
		SetStatus(vo.Status).
		SetViewCount(vo.ViewCount).
		SetSortCode(vo.SortCode).
		SetCreatedAt(now).
		SetUpdatedAt(now)

	if vo.Summary != nil {
		builder.SetNillableSummary(vo.Summary)
	}
	if vo.Content != nil {
		builder.SetNillableContent(vo.Content)
	}
	if vo.Cover != nil {
		builder.SetNillableCover(vo.Cover)
	}
	if vo.Position != nil {
		builder.SetNillablePosition(vo.Position)
	}
	if userID != "" {
		builder.SetCreatedBy(userID).SetUpdatedBy(userID)
	}

	_, err := builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("添加通知失败: "+err.Error(), 500))
	}
}

// Modify updates an existing notice.
func Modify(c *gin.Context, vo *NoticeVO, userID string) {
	ctx := context.Background()

	if vo.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}

	// Verify the notice exists
	_, err := db.Client.SysNotice.Get(ctx, vo.ID)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("数据不存在", 400))
		}
		panic(exception.NewBusinessError("查询通知失败: "+err.Error(), 500))
	}

	now := time.Now()
	builder := db.Client.SysNotice.UpdateOneID(vo.ID).
		SetTitle(vo.Title).
		SetCategory(vo.Category).
		SetType(vo.Type).
		SetLevel(vo.Level).
		SetIsTop(vo.IsTop).
		SetStatus(vo.Status).
		SetViewCount(vo.ViewCount).
		SetSortCode(vo.SortCode).
		SetUpdatedAt(now)

	if vo.Summary != nil {
		builder.SetNillableSummary(vo.Summary)
	}
	if vo.Content != nil {
		builder.SetNillableContent(vo.Content)
	}
	if vo.Cover != nil {
		builder.SetNillableCover(vo.Cover)
	}
	if vo.Position != nil {
		builder.SetNillablePosition(vo.Position)
	}
	if userID != "" {
		builder.SetUpdatedBy(userID)
	}

	_, err = builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("编辑通知失败: "+err.Error(), 500))
	}
}

// Remove deletes notices by IDs.
func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}

	ctx := context.Background()
	_, err := db.Client.SysNotice.Delete().Where(sysnotice.IDIn(ids...)).Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除通知失败: "+err.Error(), 500))
	}
}

// entToVO converts an ent SysNotice entity to a NoticeVO.
func entToVO(entity *ent.SysNotice) *NoticeVO {
	vo := &NoticeVO{
		ID:        entity.ID,
		Title:     entity.Title,
		Category:  entity.Category,
		Type:      entity.Type,
		Level:     entity.Level,
		ViewCount: entity.ViewCount,
		IsTop:     entity.IsTop,
		Status:    entity.Status,
		SortCode:  entity.SortCode,
	}

	if entity.Summary != nil {
		vo.Summary = entity.Summary
	}
	if entity.Content != nil {
		vo.Content = entity.Content
	}
	if entity.Cover != nil {
		vo.Cover = entity.Cover
	}
	if entity.Position != nil {
		vo.Position = entity.Position
	}
	if entity.CreatedAt != nil {
		vo.CreatedAt = entity.CreatedAt.Format("2006-01-02 15:04:05")
	}
	if entity.CreatedBy != nil {
		vo.CreatedBy = entity.CreatedBy
	}
	if entity.UpdatedAt != nil {
		vo.UpdatedAt = entity.UpdatedAt.Format("2006-01-02 15:04:05")
	}
	if entity.UpdatedBy != nil {
		vo.UpdatedBy = entity.UpdatedBy
	}

	return vo
}

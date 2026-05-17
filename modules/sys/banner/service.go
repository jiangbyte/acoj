package banner

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysbanner"

	"entgo.io/ent/dialect/sql"
	"github.com/gin-gonic/gin"
)

// Page returns a paginated list of banners.
func Page(c *gin.Context, param *BannerPageParam) gin.H {
	ctx := context.Background()

	// Set defaults
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	offset := (param.Current - 1) * param.Size

	total, err := db.Client.SysBanner.Query().Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询Banner列表失败: "+err.Error(), 500))
	}

	records, err := db.Client.SysBanner.Query().
		Order(sysbanner.ByCreatedAt(sql.OrderDesc())).
		Limit(param.Size).
		Offset(offset).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询Banner列表失败: "+err.Error(), 500))
	}

	vos := make([]*BannerVO, 0, len(records))
	for _, r := range records {
		vos = append(vos, entToVO(r))
	}

	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

// Detail returns a single banner by ID.
func Detail(c *gin.Context, id string) *BannerVO {
	if id == "" {
		return nil
	}

	ctx := context.Background()
	entity, err := db.Client.SysBanner.Get(ctx, id)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil
		}
		panic(exception.NewBusinessError("查询Banner详情失败: "+err.Error(), 500))
	}

	return entToVO(entity)
}

// Create creates a new banner.
func Create(c *gin.Context, vo *BannerVO, userID string) {
	ctx := context.Background()
	now := time.Now()

	builder := db.Client.SysBanner.Create().
		SetID(utils.GenerateID()).
		SetTitle(vo.Title).
		SetImage(vo.Image).
		SetCategory(vo.Category).
		SetType(vo.Type).
		SetPosition(vo.Position).
		SetSortCode(vo.SortCode).
		SetViewCount(vo.ViewCount).
		SetClickCount(vo.ClickCount).
		SetCreatedAt(now).
		SetUpdatedAt(now)

	if vo.LinkType != "" {
		builder.SetLinkType(vo.LinkType)
	}
	if vo.URL != nil {
		builder.SetNillableURL(vo.URL)
	}
	if vo.Summary != nil {
		builder.SetNillableSummary(vo.Summary)
	}
	if vo.Description != nil {
		builder.SetNillableDescription(vo.Description)
	}
	if userID != "" {
		builder.SetCreatedBy(userID).SetUpdatedBy(userID)
	}

	_, err := builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("添加Banner失败: "+err.Error(), 500))
	}
}

// Modify updates an existing banner.
func Modify(c *gin.Context, vo *BannerVO, userID string) {
	ctx := context.Background()

	if vo.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}

	// Verify the banner exists
	_, err := db.Client.SysBanner.Get(ctx, vo.ID)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("Banner不存在", 404))
		}
		panic(exception.NewBusinessError("查询Banner失败: "+err.Error(), 500))
	}

	now := time.Now()
	builder := db.Client.SysBanner.UpdateOneID(vo.ID).
		SetTitle(vo.Title).
		SetImage(vo.Image).
		SetCategory(vo.Category).
		SetType(vo.Type).
		SetPosition(vo.Position).
		SetSortCode(vo.SortCode).
		SetViewCount(vo.ViewCount).
		SetClickCount(vo.ClickCount).
		SetUpdatedAt(now)

	if vo.LinkType != "" {
		builder.SetLinkType(vo.LinkType)
	}
	if vo.URL != nil {
		builder.SetNillableURL(vo.URL)
	}
	if vo.Summary != nil {
		builder.SetNillableSummary(vo.Summary)
	}
	if vo.Description != nil {
		builder.SetNillableDescription(vo.Description)
	}
	if userID != "" {
		builder.SetUpdatedBy(userID)
	}

	_, err = builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("编辑Banner失败: "+err.Error(), 500))
	}
}

// Remove deletes banners by IDs.
func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}

	ctx := context.Background()
	_, err := db.Client.SysBanner.Delete().Where(sysbanner.IDIn(ids...)).Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除Banner失败: "+err.Error(), 500))
	}
}

// entToVO converts an ent SysBanner entity to a BannerVO.
func entToVO(entity *ent.SysBanner) *BannerVO {
	vo := &BannerVO{
		ID:         entity.ID,
		Title:      entity.Title,
		Image:      entity.Image,
		LinkType:   entity.LinkType,
		Category:   entity.Category,
		Type:       entity.Type,
		Position:   entity.Position,
		SortCode:   entity.SortCode,
		ViewCount:  entity.ViewCount,
		ClickCount: entity.ClickCount,
	}

	if entity.URL != nil {
		vo.URL = entity.URL
	}
	if entity.Summary != nil {
		vo.Summary = entity.Summary
	}
	if entity.Description != nil {
		vo.Description = entity.Description
	}
	if entity.CreatedAt != nil {
		s := entity.CreatedAt.Format("2006-01-02 15:04:05")
		vo.CreatedAt = &s
	}
	if entity.CreatedBy != nil {
		vo.CreatedBy = entity.CreatedBy
	}
	if entity.UpdatedAt != nil {
		s := entity.UpdatedAt.Format("2006-01-02 15:04:05")
		vo.UpdatedAt = &s
	}
	if entity.UpdatedBy != nil {
		vo.UpdatedBy = entity.UpdatedBy
	}

	return vo
}

package banner

import (
	"context"
	"time"

	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/pojo"
	"hei-gin/core/utils"
	"hei-gin/ent/gen"
)

type Service struct{}

func (s *Service) Create(ctx context.Context, vo *BannerVO) (*gen.SysBanner, error) {
	loginID := auth.GetLoginIDFromCtx(ctx)
	now := time.Now()
	id := utils.NextID()
	return db.Client.SysBanner.Create().
		SetID(id).
		SetTitle(vo.Title).
		SetImage(vo.Image).
		SetCategory(vo.Category).
		SetType(vo.Type).
		SetPosition(vo.Position).
		SetNillableURL(vo.URL).
		SetNillableSummary(vo.Summary).
		SetNillableDescription(vo.Description).
		SetLinkType(vo.LinkType).
		SetSortCode(vo.SortCode).
		SetViewCount(vo.ViewCount).
		SetClickCount(vo.ClickCount).
		SetCreatedAt(now).
		SetNillableCreatedBy(&loginID).
		SetUpdatedAt(now).
		SetNillableUpdatedBy(&loginID).
		Save(ctx)
}

func (s *Service) Modify(ctx context.Context, vo *BannerVO) error {
	loginID := auth.GetLoginIDFromCtx(ctx)
	now := time.Now()
	return db.Client.SysBanner.UpdateOneID(vo.ID).
		SetTitle(vo.Title).
		SetImage(vo.Image).
		SetCategory(vo.Category).
		SetType(vo.Type).
		SetPosition(vo.Position).
		SetNillableURL(vo.URL).
		SetNillableSummary(vo.Summary).
		SetNillableDescription(vo.Description).
		SetLinkType(vo.LinkType).
		SetSortCode(vo.SortCode).
		SetViewCount(vo.ViewCount).
		SetClickCount(vo.ClickCount).
		SetUpdatedAt(now).
		SetNillableUpdatedBy(&loginID).
		Exec(ctx)
}

func (s *Service) Remove(ctx context.Context, ids []string) error {
	return new(Dao).DeleteByIDs(ctx, ids)
}

func (s *Service) FindPage(ctx context.Context, bounds *pojo.PageBounds) ([]*gen.SysBanner, int, error) {
	return new(Dao).FindPage(ctx, bounds)
}

func (s *Service) FindByID(ctx context.Context, id string) (*gen.SysBanner, error) {
	return new(Dao).FindByID(ctx, id)
}

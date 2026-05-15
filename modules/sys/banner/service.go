package banner

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/sysbanner"
)

type PageParam struct {
	Page     int    `form:"page" json:"page"`
	Size     int    `form:"size" json:"size"`
	Keyword  string `form:"keyword" json:"keyword"`
	Category string `form:"category" json:"category"`
	Status   string `form:"status" json:"status"`
}

type BannerVO struct {
	ID          string `json:"id"`
	Title       string `json:"title"`
	Image       string `json:"image"`
	Category    string `json:"category"`
	Type        string `json:"type"`
	Position    string `json:"position"`
	URL         string `json:"url"`
	LinkType    string `json:"link_type"`
	Summary     string `json:"summary"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
	ViewCount   int    `json:"view_count"`
	ClickCount  int    `json:"click_count"`
	Status      string `json:"status"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type BannerCreateReq struct {
	Title       string `json:"title" binding:"required"`
	Image       string `json:"image" binding:"required"`
	Category    string `json:"category"`
	Type        string `json:"type"`
	Position    string `json:"position"`
	URL         string `json:"url"`
	LinkType    string `json:"link_type"`
	Summary     string `json:"summary"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
}

type BannerModifyReq struct {
	ID          string `json:"id" binding:"required"`
	Title       string `json:"title"`
	Image       string `json:"image"`
	Category    string `json:"category"`
	Type        string `json:"type"`
	Position    string `json:"position"`
	URL         string `json:"url"`
	LinkType    string `json:"link_type"`
	Summary     string `json:"summary"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

// BannerExportFieldNames maps db field names to display names
var BannerExportFieldNames = map[string]string{
	"title":      "轮播标题",
	"image":      "轮播图片",
	"category":   "轮播类别",
	"position":   "展示位置",
	"sort_code":  "排序",
	"created_at": "创建时间",
}

var BannerExportFields = []string{"title", "image", "category", "position", "sort_code", "created_at"}

func toVO(b *ent.SysBanner) BannerVO {
	vo := BannerVO{
		ID:          b.ID,
		Title:       b.Title,
		Image:       b.Image,
		Category:    b.Category,
		Type:        b.Type,
		Position:    b.Position,
		URL:         b.URL,
		LinkType:    b.LinkType,
		Summary:     b.Summary,
		Description: b.Description,
		SortCode:    b.SortCode,
		ViewCount:   b.ViewCount,
		ClickCount:  b.ClickCount,
		CreatedAt:   b.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   b.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   b.CreatedBy,
		UpdatedBy:   b.UpdatedBy,
	}
	return vo
}

func Page(page, size int, keyword, category string) (int, []*ent.SysBanner, error) {
	ctx := context.Background()
	q := db.Client.SysBanner.Query()

	if keyword != "" {
		q = q.Where(sysbanner.TitleContains(keyword))
	}
	if category != "" {
		q = q.Where(sysbanner.Category(category))
	}

	total, err := q.Count(ctx)
	if err != nil {
		return 0, nil, err
	}

	if size <= 0 {
		size = 10
	}
	if page <= 0 {
		page = 1
	}

	items, err := q.
		Order(ent.Desc(sysbanner.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Create(req *BannerCreateReq, loginID string) (*ent.SysBanner, error) {
	ctx := context.Background()
	now := time.Now()
	q := db.Client.SysBanner.Create().
		SetID(utils.NextID()).
		SetTitle(req.Title).
		SetImage(req.Image).
		SetCategory(req.Category).
		SetType(req.Type).
		SetPosition(req.Position).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)
	if req.URL != "" {
		q.SetURL(req.URL)
	}
	if req.LinkType != "" {
		q.SetLinkType(req.LinkType)
	}
	if req.Summary != "" {
		q.SetSummary(req.Summary)
	}
	if req.Description != "" {
		q.SetDescription(req.Description)
	}
	if req.SortCode > 0 {
		q.SetSortCode(req.SortCode)
	}
	return q.Save(ctx)
}

func Modify(req *BannerModifyReq, loginID string) (*ent.SysBanner, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysBanner.UpdateOneID(req.ID)

	if req.Title != "" {
		u.SetTitle(req.Title)
	}
	if req.Image != "" {
		u.SetImage(req.Image)
	}
	if req.Category != "" {
		u.SetCategory(req.Category)
	}
	if req.Type != "" {
		u.SetType(req.Type)
	}
	if req.Position != "" {
		u.SetPosition(req.Position)
	}
	if req.URL != "" {
		u.SetURL(req.URL)
	}
	if req.Summary != "" {
		u.SetSummary(req.Summary)
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}
	if req.SortCode > 0 {
		u.SetSortCode(req.SortCode)
	}
	if req.LinkType != "" {
		u.SetLinkType(req.LinkType)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()
	_, err := db.Client.SysBanner.Delete().Where(sysbanner.IDIn(ids...)).Exec(ctx)
	return err
}

func Detail(id string) (*ent.SysBanner, error) {
	ctx := context.Background()
	return db.Client.SysBanner.Get(ctx, id)
}

func QueryAll() ([]*ent.SysBanner, error) {
	ctx := context.Background()
	return db.Client.SysBanner.Query().Order(ent.Desc(sysbanner.FieldCreatedAt)).All(ctx)
}

func ifSet(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}

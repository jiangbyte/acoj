package position

import (
	"context"
	"errors"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysposition"
	"hei-gin/ent/gen/sysuser"
)

type PageParam struct {
	Page    int    `form:"page" json:"page"`
	Size    int    `form:"size" json:"size"`
	Keyword string `form:"keyword" json:"keyword"`
	Status  string `form:"status" json:"status"`
}

type PositionVO struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Code        string `json:"code"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Description string `json:"description"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type PositionCreateReq struct {
	Name        string `json:"name" binding:"required"`
	Code        string `json:"code"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Description string `json:"description"`
}

type PositionModifyReq struct {
	ID          string `json:"id" binding:"required"`
	Name        string `json:"name"`
	Code        string `json:"code"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Description string `json:"description"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

func toVO(p *ent.SysPosition) PositionVO {
	vo := PositionVO{
		ID:          p.ID,
		Name:        p.Name,
		Code:        p.Code,
		SortCode:    p.SortCode,
		Status:      p.Status,
		Description: p.Description,
		CreatedAt:   p.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   p.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   p.CreatedBy,
		UpdatedBy:   p.UpdatedBy,
	}
	return vo
}

func Page(page, size int, keyword, status string) (int, []*ent.SysPosition, error) {
	ctx := context.Background()
	q := db.Client.SysPosition.Query()

	if keyword != "" {
		q = q.Where(sysposition.Or(
			sysposition.NameContains(keyword),
			sysposition.CodeContains(keyword),
		))
	}
	if status != "" {
		q = q.Where(sysposition.Status(status))
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
		Order(ent.Desc(sysposition.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Create(req *PositionCreateReq, loginID string) (*ent.SysPosition, error) {
	ctx := context.Background()
	now := time.Now()
	q := db.Client.SysPosition.Create().
		SetID(utils.NextID()).
		SetName(req.Name).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)
	if req.Code != "" {
		q.SetCode(req.Code)
	}
	if req.SortCode > 0 {
		q.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		q.SetStatus(req.Status)
	}
	if req.Description != "" {
		q.SetDescription(req.Description)
	}
	return q.Save(ctx)
}

func Modify(req *PositionModifyReq, loginID string) (*ent.SysPosition, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysPosition.UpdateOneID(req.ID)

	if req.Name != "" {
		u.SetName(req.Name)
	}
	if req.Code != "" {
		u.SetCode(req.Code)
	}
	if req.SortCode > 0 {
		u.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()

	// Check sys_user for references
	userCount, err := db.Client.SysUser.Query().Where(sysuser.PositionIDIn(ids...)).Count(ctx)
	if err != nil {
		return err
	}
	if userCount > 0 {
		return errors.New("存在关联用户，无法删除")
	}

	// Delete positions
	_, err = db.Client.SysPosition.Delete().Where(sysposition.IDIn(ids...)).Exec(ctx)
	return err
}

func Detail(id string) (*ent.SysPosition, error) {
	ctx := context.Background()
	return db.Client.SysPosition.Get(ctx, id)
}

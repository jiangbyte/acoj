package notice

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/sysnotice"
)

type PageParam struct {
	Page    int    `form:"page" json:"page"`
	Size    int    `form:"size" json:"size"`
	Keyword string `form:"keyword" json:"keyword"`
	Status  string `form:"status" json:"status"`
}

type NoticeVO struct {
	ID          string `json:"id"`
	Title       string `json:"title"`
	Content     string `json:"content"`
	Category    string `json:"category"`
	Status      string `json:"status"`
	PublishTime string `json:"publish_time"`
	ExpireTime  string `json:"expire_time"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type NoticeCreateReq struct {
	Title       string `json:"title" binding:"required"`
	Content     string `json:"content"`
	Category    string `json:"category"`
	Status      string `json:"status"`
	PublishTime string `json:"publish_time"`
	ExpireTime  string `json:"expire_time"`
}

type NoticeModifyReq struct {
	ID          string `json:"id" binding:"required"`
	Title       string `json:"title"`
	Content     string `json:"content"`
	Category    string `json:"category"`
	Status      string `json:"status"`
	PublishTime string `json:"publish_time"`
	ExpireTime  string `json:"expire_time"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

var NoticeExportFieldNames = map[string]string{
	"title":        "公告标题",
	"category":     "公告类别",
	"status":       "状态",
	"publish_time": "发布时间",
	"created_at":   "创建时间",
}

var NoticeExportFields = []string{"title", "category", "status", "publish_time", "created_at"}

func toVO(n *ent.SysNotice) NoticeVO {
	vo := NoticeVO{
		ID:        n.ID,
		Title:     n.Title,
		Content:   n.Content,
		Category:  n.Category,
		Status:    n.Status,
		CreatedAt: n.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt: n.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy: n.CreatedBy,
		UpdatedBy: n.UpdatedBy,
	}
	if !n.PublishTime.IsZero() {
		vo.PublishTime = n.PublishTime.Format("2006-01-02 15:04:05")
	}
	if !n.ExpireTime.IsZero() {
		vo.ExpireTime = n.ExpireTime.Format("2006-01-02 15:04:05")
	}
	return vo
}

func Page(page, size int, keyword, status string) (int, []*ent.SysNotice, error) {
	ctx := context.Background()
	q := db.Client.SysNotice.Query()

	if keyword != "" {
		q = q.Where(sysnotice.TitleContains(keyword))
	}
	if status != "" {
		q = q.Where(sysnotice.Status(status))
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
		Order(ent.Desc(sysnotice.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Create(req *NoticeCreateReq, loginID string) (*ent.SysNotice, error) {
	ctx := context.Background()
	now := time.Now()
	q := db.Client.SysNotice.Create().
		SetID(utils.NextID()).
		SetTitle(req.Title).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)
	if req.Content != "" {
		q.SetContent(req.Content)
	}
	if req.Category != "" {
		q.SetCategory(req.Category)
	}
	if req.Status != "" {
		q.SetStatus(req.Status)
	}
	if req.PublishTime != "" {
		t, err := time.Parse("2006-01-02 15:04:05", req.PublishTime)
		if err == nil {
			q.SetPublishTime(t)
		}
	}
	if req.ExpireTime != "" {
		t, err := time.Parse("2006-01-02 15:04:05", req.ExpireTime)
		if err == nil {
			q.SetExpireTime(t)
		}
	}
	return q.Save(ctx)
}

func Modify(req *NoticeModifyReq, loginID string) (*ent.SysNotice, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysNotice.UpdateOneID(req.ID)

	if req.Title != "" {
		u.SetTitle(req.Title)
	}
	if req.Content != "" {
		u.SetContent(req.Content)
	}
	if req.Category != "" {
		u.SetCategory(req.Category)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	if req.PublishTime != "" {
		t, err := time.Parse("2006-01-02 15:04:05", req.PublishTime)
		if err == nil {
			u.SetPublishTime(t)
		}
	}
	if req.ExpireTime != "" {
		t, err := time.Parse("2006-01-02 15:04:05", req.ExpireTime)
		if err == nil {
			u.SetExpireTime(t)
		}
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()
	_, err := db.Client.SysNotice.Delete().Where(sysnotice.IDIn(ids...)).Exec(ctx)
	return err
}

func Detail(id string) (*ent.SysNotice, error) {
	ctx := context.Background()
	return db.Client.SysNotice.Get(ctx, id)
}

func QueryAll() ([]*ent.SysNotice, error) {
	ctx := context.Background()
	return db.Client.SysNotice.Query().Order(ent.Desc(sysnotice.FieldCreatedAt)).All(ctx)
}

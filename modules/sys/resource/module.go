package resource

import (
	"context"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysmodule"
)

// ---------- Types ----------

type ModulePageParam struct {
	Page     int    `form:"page" json:"page"`
	Size     int    `form:"size" json:"size"`
	Keyword  string `form:"keyword" json:"keyword"`
	Category string `form:"category" json:"category"`
	Status   string `form:"status" json:"status"`
}

type ModuleVO struct {
	ID          string `json:"id"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	Icon        string `json:"icon"`
	Color       string `json:"color"`
	Description string `json:"description"`
	IsVisible   bool   `json:"is_visible"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type ModuleCreateReq struct {
	Code        string `json:"code" binding:"required"`
	Name        string `json:"name" binding:"required"`
	Category    string `json:"category"`
	Icon        string `json:"icon"`
	Color       string `json:"color"`
	Description string `json:"description"`
	IsVisible   *bool  `json:"is_visible"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type ModuleModifyReq struct {
	ID          string `json:"id" binding:"required"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	Icon        string `json:"icon"`
	Color       string `json:"color"`
	Description string `json:"description"`
	IsVisible   *bool  `json:"is_visible"`
	Status      string `json:"status"`
	SortCode    int    `json:"sort_code"`
}

type ModuleRemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type ModuleDetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

// ---------- Helpers ----------

func toModuleVO(m *ent.SysModule) ModuleVO {
	return ModuleVO{
		ID:          m.ID,
		Code:        m.Code,
		Name:        m.Name,
		Category:    m.Category,
		Icon:        m.Icon,
		Color:       m.Color,
		Description: m.Description,
		IsVisible:   m.IsVisible,
		Status:      m.Status,
		SortCode:    m.SortCode,
		CreatedAt:   m.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   m.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   m.CreatedBy,
		UpdatedBy:   m.UpdatedBy,
	}
}

// ---------- Service Functions ----------

func ModulePage(page, size int, keyword, category, status string) (int, []*ent.SysModule, error) {
	ctx := context.Background()
	q := db.Client.SysModule.Query()

	if keyword != "" {
		q = q.Where(
			sysmodule.Or(
				sysmodule.NameContains(keyword),
				sysmodule.CodeContains(keyword),
			),
		)
	}
	if category != "" {
		q = q.Where(sysmodule.CategoryEQ(category))
	}
	if status != "" {
		q = q.Where(sysmodule.StatusEQ(status))
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
		Order(ent.Desc(sysmodule.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func ModuleCreate(req *ModuleCreateReq, loginID string) (*ent.SysModule, error) {
	ctx := context.Background()
	now := time.Now()

	isVisible := true
	if req.IsVisible != nil {
		isVisible = *req.IsVisible
	}
	status := "ENABLED"
	if req.Status != "" {
		status = req.Status
	}

	return db.Client.SysModule.Create().
		SetID(utils.NextID()).
		SetCode(req.Code).
		SetName(req.Name).
		SetCategory(req.Category).
		SetIcon(req.Icon).
		SetColor(req.Color).
		SetDescription(req.Description).
		SetIsVisible(isVisible).
		SetStatus(status).
		SetSortCode(req.SortCode).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID).
		Save(ctx)
}

func ModuleModify(req *ModuleModifyReq, loginID string) (*ent.SysModule, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysModule.UpdateOneID(req.ID)

	if req.Code != "" {
		u.SetCode(req.Code)
	}
	if req.Name != "" {
		u.SetName(req.Name)
	}
	if req.Category != "" {
		u.SetCategory(req.Category)
	}
	if req.Icon != "" {
		u.SetIcon(req.Icon)
	}
	if req.Color != "" {
		u.SetColor(req.Color)
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}
	if req.IsVisible != nil {
		u.SetIsVisible(*req.IsVisible)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	if req.SortCode > 0 {
		u.SetSortCode(req.SortCode)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func ModuleRemove(ids []string) error {
	ctx := context.Background()
	_, err := db.Client.SysModule.Delete().Where(sysmodule.IDIn(ids...)).Exec(ctx)
	return err
}

func ModuleDetail(id string) (*ent.SysModule, error) {
	ctx := context.Background()
	return db.Client.SysModule.Get(ctx, id)
}

// ---------- Handlers ----------

func ModulePageHandler(c *gin.Context) {
	var p ModulePageParam
	if err := c.ShouldBindQuery(&p); err != nil {
		result.ValidationError(c, err)
		return
	}
	if p.Page <= 0 {
		p.Page = 1
	}
	if p.Size <= 0 {
		p.Size = 10
	}

	total, items, err := ModulePage(p.Page, p.Size, p.Keyword, p.Category, p.Status)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []ModuleVO
	for _, item := range items {
		vos = append(vos, toModuleVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func ModuleCreateHandler(c *gin.Context) {
	var req ModuleCreateReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	item, err := ModuleCreate(&req, loginID)
	if err != nil {
		result.Failure(c, "创建失败", 500)
		return
	}
	result.Success(c, toModuleVO(item))
}

func ModuleModifyHandler(c *gin.Context) {
	var req ModuleModifyReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	item, err := ModuleModify(&req, loginID)
	if err != nil {
		result.Failure(c, "修改失败", 500)
		return
	}
	result.Success(c, toModuleVO(item))
}

func ModuleRemoveHandler(c *gin.Context) {
	var req ModuleRemoveReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	if err := ModuleRemove(req.IDs); err != nil {
		result.Failure(c, "删除失败", 500)
		return
	}
	result.Success(c, nil)
}

func ModuleDetailHandler(c *gin.Context) {
	var req ModuleDetailReq
	if err := c.ShouldBindQuery(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	item, err := ModuleDetail(req.ID)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, toModuleVO(item))
}

func RegisterModuleRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/module/page",
		auth.CheckPermission("sys:module:page"),
		ModulePageHandler,
	)
	r.POST("/api/v1/sys/module/create",
		log.SysLog("添加模块"),
		auth.CheckPermission("sys:module:create"),
		norepeat.NoRepeat(3000),
		ModuleCreateHandler,
	)
	r.POST("/api/v1/sys/module/modify",
		log.SysLog("编辑模块"),
		auth.CheckPermission("sys:module:modify"),
		ModuleModifyHandler,
	)
	r.POST("/api/v1/sys/module/remove",
		log.SysLog("删除模块"),
		auth.CheckPermission("sys:module:remove"),
		ModuleRemoveHandler,
	)
	r.GET("/api/v1/sys/module/detail",
		auth.CheckPermission("sys:module:detail"),
		ModuleDetailHandler,
	)
}

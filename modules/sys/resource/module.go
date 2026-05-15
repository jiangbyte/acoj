package resource

import (
	"context"
	"database/sql"
	"fmt"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
	"hei-gin/core/utils"
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

// moduleRow is used for scanning raw DB rows from the sys_module table.
type moduleRow struct {
	ID          string
	Code        string
	Name        string
	Category    sql.NullString
	Icon        sql.NullString
	Color       sql.NullString
	Description sql.NullString
	IsVisible   bool
	Status      sql.NullString
	SortCode    sql.NullInt64
	CreatedAt   time.Time
	CreatedBy   sql.NullString
	UpdatedAt   time.Time
	UpdatedBy   sql.NullString
}

// ---------- Export definitions ----------

var ModuleExportFieldNames = map[string]string{
	"code":       "模块编码",
	"name":       "模块名称",
	"category":   "模块类别",
	"icon":       "图标",
	"color":      "颜色",
	"status":     "状态",
	"sort_code":  "排序",
	"created_at": "创建时间",
}

var ModuleExportFields = []string{"code", "name", "category", "icon", "color", "status", "sort_code", "created_at"}

// ---------- Helpers ----------

func toModuleVO(row *moduleRow) ModuleVO {
	return ModuleVO{
		ID:          row.ID,
		Code:        row.Code,
		Name:        row.Name,
		Category:    row.Category.String,
		Icon:        row.Icon.String,
		Color:       row.Color.String,
		Description: row.Description.String,
		IsVisible:   row.IsVisible,
		Status:      row.Status.String,
		SortCode:    int(row.SortCode.Int64),
		CreatedAt:   row.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   row.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   row.CreatedBy.String,
		UpdatedBy:   row.UpdatedBy.String,
	}
}

// ---------- Service Functions ----------

func ModulePage(page, size int, keyword, category, status string) (int, []*moduleRow, error) {
	ctx := context.Background()

	// Build dynamic WHERE clause
	var where string
	var args []interface{}

	if keyword != "" {
		where = " WHERE (name LIKE ? OR code LIKE ?)"
		kw := "%" + keyword + "%"
		args = append(args, kw, kw)
	}
	if category != "" {
		if where == "" {
			where = " WHERE category = ?"
		} else {
			where += " AND category = ?"
		}
		args = append(args, category)
	}
	if status != "" {
		if where == "" {
			where = " WHERE status = ?"
		} else {
			where += " AND status = ?"
		}
		args = append(args, status)
	}

	// Count total
	countSQL := "SELECT COUNT(*) FROM sys_module" + where
	var total int
	if err := db.RawDB.QueryRowContext(ctx, countSQL, args...).Scan(&total); err != nil {
		return 0, nil, err
	}

	if size <= 0 {
		size = 10
	}
	if page <= 0 {
		page = 1
	}
	offset := (page - 1) * size

	// Query page data
	querySQL := fmt.Sprintf(
		"SELECT id, code, name, category, icon, color, description, is_visible, status, sort_code, created_at, created_by, updated_at, updated_by FROM sys_module%s ORDER BY created_at DESC LIMIT ? OFFSET ?",
		where,
	)
	queryArgs := append(args, size, offset)

	rows, err := db.RawDB.QueryContext(ctx, querySQL, queryArgs...)
	if err != nil {
		return 0, nil, err
	}
	defer rows.Close()

	var items []*moduleRow
	for rows.Next() {
		item := &moduleRow{}
		if err := rows.Scan(
			&item.ID, &item.Code, &item.Name, &item.Category, &item.Icon, &item.Color,
			&item.Description, &item.IsVisible, &item.Status, &item.SortCode,
			&item.CreatedAt, &item.CreatedBy, &item.UpdatedAt, &item.UpdatedBy,
		); err != nil {
			return 0, nil, err
		}
		items = append(items, item)
	}
	if err := rows.Err(); err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func ModuleCreate(req *ModuleCreateReq, loginID string) (*moduleRow, error) {
	ctx := context.Background()
	now := time.Now()
	id := utils.NextID()

	isVisible := true
	if req.IsVisible != nil {
		isVisible = *req.IsVisible
	}
	status := "ENABLED"
	if req.Status != "" {
		status = req.Status
	}

	_, err := db.RawDB.ExecContext(ctx,
		`INSERT INTO sys_module (id, code, name, category, icon, color, description, is_visible, status, sort_code, created_at, created_by, updated_at, updated_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
		id, req.Code, req.Name, req.Category, req.Icon, req.Color, req.Description,
		isVisible, status, req.SortCode, now, loginID, now, loginID,
	)
	if err != nil {
		return nil, err
	}

	return ModuleDetail(id)
}

func ModuleModify(req *ModuleModifyReq, loginID string) (*moduleRow, error) {
	ctx := context.Background()
	now := time.Now()

	var sets []string
	var args []interface{}

	if req.Code != "" {
		sets = append(sets, "code = ?")
		args = append(args, req.Code)
	}
	if req.Name != "" {
		sets = append(sets, "name = ?")
		args = append(args, req.Name)
	}
	if req.Category != "" {
		sets = append(sets, "category = ?")
		args = append(args, req.Category)
	}
	if req.Icon != "" {
		sets = append(sets, "icon = ?")
		args = append(args, req.Icon)
	}
	if req.Color != "" {
		sets = append(sets, "color = ?")
		args = append(args, req.Color)
	}
	if req.Description != "" {
		sets = append(sets, "description = ?")
		args = append(args, req.Description)
	}
	if req.IsVisible != nil {
		sets = append(sets, "is_visible = ?")
		args = append(args, *req.IsVisible)
	}
	if req.Status != "" {
		sets = append(sets, "status = ?")
		args = append(args, req.Status)
	}
	if req.SortCode > 0 {
		sets = append(sets, "sort_code = ?")
		args = append(args, req.SortCode)
	}

	if len(sets) == 0 {
		return nil, fmt.Errorf("no fields to update")
	}

	sets = append(sets, "updated_at = ?")
	args = append(args, now)
	sets = append(sets, "updated_by = ?")
	args = append(args, loginID)

	sqlStr := "UPDATE sys_module SET "
	for i, s := range sets {
		if i > 0 {
			sqlStr += ", "
		}
		sqlStr += s
	}
	sqlStr += " WHERE id = ?"
	args = append(args, req.ID)

	_, err := db.RawDB.ExecContext(ctx, sqlStr, args...)
	if err != nil {
		return nil, err
	}

	return ModuleDetail(req.ID)
}

func ModuleRemove(ids []string) error {
	ctx := context.Background()
	if len(ids) == 0 {
		return nil
	}

	sqlStr := "DELETE FROM sys_module WHERE id IN ("
	queryArgs := make([]interface{}, len(ids))
	for i, id := range ids {
		if i > 0 {
			sqlStr += ", "
		}
		sqlStr += "?"
		queryArgs[i] = id
	}
	sqlStr += ")"

	_, err := db.RawDB.ExecContext(ctx, sqlStr, queryArgs...)
	return err
}

func ModuleDetail(id string) (*moduleRow, error) {
	ctx := context.Background()
	row := &moduleRow{}
	err := db.RawDB.QueryRowContext(ctx,
		`SELECT id, code, name, category, icon, color, description, is_visible, status, sort_code, created_at, created_by, updated_at, updated_by FROM sys_module WHERE id = ?`,
		id,
	).Scan(
		&row.ID, &row.Code, &row.Name, &row.Category, &row.Icon, &row.Color,
		&row.Description, &row.IsVisible, &row.Status, &row.SortCode,
		&row.CreatedAt, &row.CreatedBy, &row.UpdatedAt, &row.UpdatedBy,
	)
	if err != nil {
		return nil, err
	}
	return row, nil
}

func ModuleQueryAll() ([]*moduleRow, error) {
	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT id, code, name, category, icon, color, description, is_visible, status, sort_code, created_at, created_by, updated_at, updated_by FROM sys_module ORDER BY created_at DESC`,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []*moduleRow
	for rows.Next() {
		item := &moduleRow{}
		if err := rows.Scan(
			&item.ID, &item.Code, &item.Name, &item.Category, &item.Icon, &item.Color,
			&item.Description, &item.IsVisible, &item.Status, &item.SortCode,
			&item.CreatedAt, &item.CreatedBy, &item.UpdatedAt, &item.UpdatedBy,
		); err != nil {
			return nil, err
		}
		items = append(items, item)
	}
	if err := rows.Err(); err != nil {
		return nil, err
	}

	return items, nil
}

// ---------- Handler Functions ----------

func ModulePageHandler(c *gin.Context) {
	var p ModulePageParam
	if err := c.ShouldBindQuery(&p); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	item, err := ModuleDetail(req.ID)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, toModuleVO(item))
}

func ModuleExportHandler(c *gin.Context) {
	items, err := ModuleQueryAll()
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	var data []map[string]interface{}
	for _, item := range items {
		row := map[string]interface{}{
			"code":       item.Code,
			"name":       item.Name,
			"category":   item.Category.String,
			"icon":       item.Icon.String,
			"color":      item.Color.String,
			"status":     item.Status.String,
			"sort_code":  item.SortCode.Int64,
			"created_at": item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders(ModuleExportFields, ModuleExportFieldNames)
	excelBytes, err := utils.ExportExcel(data, headers, "模块数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="module_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func ModuleTemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders(ModuleExportFields, ModuleExportFieldNames)
	excelBytes, err := utils.ExportExcel(nil, headers, "模块导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="module_template.xlsx"`)
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func ModuleImportHandler(c *gin.Context) {
	file, err := c.FormFile("file")
	if err != nil {
		result.Failure(c, "请上传文件", 400)
		return
	}

	src, err := file.Open()
	if err != nil {
		result.Failure(c, "文件读取失败", 500)
		return
	}
	defer src.Close()

	fileBytes := make([]byte, file.Size)
	if _, err := src.Read(fileBytes); err != nil {
		result.Failure(c, "文件读取失败", 500)
		return
	}

	rows, err := utils.ParseExcel(fileBytes, "模块导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := ModuleCreate(&ModuleCreateReq{
			Code:     row["模块编码"],
			Name:     row["模块名称"],
			Category: row["模块类别"],
			Icon:     row["图标"],
			Color:    row["颜色"],
		}, loginID)
		if err == nil {
			success++
		}
	}

	result.Success(c, map[string]int{"success": success, "total": len(rows)})
}

// ---------- Route Registration ----------

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
	r.GET("/api/v1/sys/module/export",
		log.SysLog("导出模块数据"),
		auth.CheckPermission("sys:module:export"),
		ModuleExportHandler,
	)
	r.GET("/api/v1/sys/module/template",
		auth.CheckPermission("sys:module:template"),
		ModuleTemplateHandler,
	)
	r.POST("/api/v1/sys/module/import",
		log.SysLog("导入模块数据"),
		auth.CheckPermission("sys:module:import"),
		norepeat.NoRepeat(5000),
		ModuleImportHandler,
	)
}

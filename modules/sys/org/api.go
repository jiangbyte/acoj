package org

import (
	"fmt"
	"strconv"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
	"hei-gin/core/utils"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/org/page",
		auth.CheckPermission("sys:org:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/org/create",
		log.SysLog("添加组织"),
		auth.CheckPermission("sys:org:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/org/modify",
		log.SysLog("编辑组织"),
		auth.CheckPermission("sys:org:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/org/remove",
		log.SysLog("删除组织"),
		auth.CheckPermission("sys:org:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/org/detail",
		auth.CheckPermission("sys:org:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/org/export",
		log.SysLog("导出组织数据"),
		auth.CheckPermission("sys:org:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/org/template",
		auth.CheckPermission("sys:org:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/org/import",
		log.SysLog("导入组织数据"),
		auth.CheckPermission("sys:org:import"),
		norepeat.NoRepeat(5000),
		ImportHandler,
	)
	r.GET("/api/v1/sys/org/treeselect",
		auth.CheckPermission("sys:org:page"),
		TreeSelectHandler,
	)
	r.GET("/api/v1/sys/org/own-roles",
		auth.CheckPermission("sys:org:grant"),
		OwnRolesHandler,
	)
	r.POST("/api/v1/sys/org/grant-role",
		log.SysLog("分配角色"),
		auth.CheckPermission("sys:org:grant"),
		GrantRoleHandler,
	)
}

func PageHandler(c *gin.Context) {
	var p PageParam
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

	total, items, err := Page(p.Page, p.Size, p.Keyword, p.Status)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []OrgVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req OrgCreateReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	item, err := Create(&req, loginID)
	if err != nil {
		result.Failure(c, "创建失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func ModifyHandler(c *gin.Context) {
	var req OrgModifyReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	item, err := Modify(&req, loginID)
	if err != nil {
		result.Failure(c, "修改失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func RemoveHandler(c *gin.Context) {
	var req RemoveReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	if err := Remove(req.IDs); err != nil {
		result.Failure(c, "删除失败", 500)
		return
	}
	result.Success(c, nil)
}

func DetailHandler(c *gin.Context) {
	var req DetailReq
	if err := c.ShouldBindQuery(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	item, err := Detail(req.ID)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, toVO(item))
}

func ExportHandler(c *gin.Context) {
	items, err := QueryAll()
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	var data []map[string]interface{}
	for _, item := range items {
		row := map[string]interface{}{
			"name":       item.Name,
			"code":       item.Code,
			"status":     item.Status,
			"leader":     item.Leader,
			"phone":      item.Phone,
			"email":      item.Email,
			"address":    item.Address,
			"sort_code":  item.SortCode,
			"created_at": item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders(OrgExportFields, OrgExportFieldNames)
	excelBytes, err := utils.ExportExcel(data, headers, "组织数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="org_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders(OrgExportFields, OrgExportFieldNames)
	excelBytes, err := utils.ExportExcel(nil, headers, "组织导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="org_template.xlsx"`)
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func ImportHandler(c *gin.Context) {
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

	rows, err := utils.ParseExcel(fileBytes, "组织导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := Create(&OrgCreateReq{
			Name:  row["组织名称"],
			Code:  row["组织编码"],
			Phone: row["联系电话"],
			Email: row["电子邮箱"],
		}, loginID)
		if err == nil {
			success++
		}
	}

	result.Success(c, map[string]int{"success": success, "total": len(rows)})
}

func TreeSelectHandler(c *gin.Context) {
	tree, err := TreeSelect()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, tree)
}

func OwnRolesHandler(c *gin.Context) {
	orgID := c.Query("org_id")
	if orgID == "" {
		result.Failure(c, "缺少org_id参数", 400)
		return
	}

	roleIDs, err := OwnRoles(orgID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, roleIDs)
}

func GrantRoleHandler(c *gin.Context) {
	var req OrgGrantRoleReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	if err := GrantRole(req.OrgID, req.RoleIDs); err != nil {
		result.Failure(c, "分配角色失败", 500)
		return
	}
	result.Success(c, nil)
}

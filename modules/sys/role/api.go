package role

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
	r.GET("/api/v1/sys/role/page",
		auth.CheckPermission("sys:role:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/role/create",
		log.SysLog("添加角色"),
		auth.CheckPermission("sys:role:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/role/modify",
		log.SysLog("编辑角色"),
		auth.CheckPermission("sys:role:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/role/remove",
		log.SysLog("删除角色"),
		auth.CheckPermission("sys:role:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/role/detail",
		auth.CheckPermission("sys:role:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/role/export",
		log.SysLog("导出角色数据"),
		auth.CheckPermission("sys:role:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/role/template",
		auth.CheckPermission("sys:role:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/role/import",
		log.SysLog("导入角色数据"),
		auth.CheckPermission("sys:role:import"),
		norepeat.NoRepeat(5000),
		ImportHandler,
	)
	r.GET("/api/v1/sys/role/own-resource",
		auth.CheckPermission("sys:role:own-resource"),
		OwnResourcesHandler,
	)
	r.POST("/api/v1/sys/role/grant-resource",
		log.SysLog("分配角色资源"),
		auth.CheckPermission("sys:role:grant-resource"),
		norepeat.NoRepeat(3000),
		GrantResourceHandler,
	)
	r.GET("/api/v1/sys/role/own-permission",
		auth.CheckPermission("sys:role:own-permission"),
		OwnPermissionsHandler,
	)
	r.GET("/api/v1/sys/role/own-permission-detail",
		auth.CheckPermission("sys:role:own-permission"),
		OwnPermissionDetailHandler,
	)
	r.POST("/api/v1/sys/role/grant-permission",
		log.SysLog("分配角色权限"),
		auth.CheckPermission("sys:role:grant-permission"),
		norepeat.NoRepeat(3000),
		GrantPermissionHandler,
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

	var vos []RoleVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req RoleCreateReq
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
	var req RoleModifyReq
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
			"name":        item.Name,
			"code":        item.Code,
			"data_scope":  item.DataScope,
			"sort_code":   item.SortCode,
			"status":      item.Status,
			"description": item.Description,
			"created_at":  item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders(RoleExportFields, RoleExportFieldNames)
	excelBytes, err := utils.ExportExcel(data, headers, "角色数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="role_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders(RoleExportFields, RoleExportFieldNames)
	excelBytes, err := utils.ExportExcel(nil, headers, "角色导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="role_template.xlsx"`)
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

	rows, err := utils.ParseExcel(fileBytes, "角色导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := Create(&RoleCreateReq{
			Name: row["角色名称"],
			Code: row["角色编码"],
		}, loginID)
		if err == nil {
			success++
		}
	}

	result.Success(c, map[string]int{"success": success, "total": len(rows)})
}

func OwnResourcesHandler(c *gin.Context) {
	roleID := c.Query("role_id")
	if roleID == "" {
		result.Failure(c, "缺少role_id参数", 400)
		return
	}

	ids, err := OwnResources(roleID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, ids)
}

func GrantResourceHandler(c *gin.Context) {
	var req GrantResourceReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	if err := GrantResource(req.RoleID, req.ResourceIDs); err != nil {
		result.Failure(c, "分配资源失败", 500)
		return
	}
	result.Success(c, nil)
}

func OwnPermissionsHandler(c *gin.Context) {
	roleID := c.Query("role_id")
	if roleID == "" {
		result.Failure(c, "缺少role_id参数", 400)
		return
	}

	codes, err := OwnPermissions(roleID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, codes)
}

func GrantPermissionHandler(c *gin.Context) {
	var req GrantPermissionReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	if err := GrantPermission(req.RoleID, req.PermissionCodes); err != nil {
		result.Failure(c, "分配权限失败", 500)
		return
	}
	result.Success(c, nil)
}

func OwnPermissionDetailHandler(c *gin.Context) {
	roleID := c.Query("role_id")
	if roleID == "" {
		result.Failure(c, "缺少role_id参数", 400)
		return
	}
	data, err := OwnPermissionDetail(roleID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}

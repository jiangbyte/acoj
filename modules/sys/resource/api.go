package resource

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
	r.GET("/api/v1/sys/resource/page",
		auth.CheckPermission("sys:resource:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/resource/create",
		log.SysLog("添加资源"),
		auth.CheckPermission("sys:resource:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/resource/modify",
		log.SysLog("编辑资源"),
		auth.CheckPermission("sys:resource:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/resource/remove",
		log.SysLog("删除资源"),
		auth.CheckPermission("sys:resource:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/resource/detail",
		auth.CheckPermission("sys:resource:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/resource/export",
		log.SysLog("导出资源数据"),
		auth.CheckPermission("sys:resource:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/resource/template",
		auth.CheckPermission("sys:resource:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/resource/import",
		log.SysLog("导入资源数据"),
		auth.CheckPermission("sys:resource:import"),
		norepeat.NoRepeat(5000),
		ImportHandler,
	)
	r.GET("/api/v1/sys/resource/treeselect",
		auth.CheckPermission("sys:resource:page"),
		TreeSelectHandler,
	)
	r.GET("/api/v1/sys/resource/build-bootstrap-menus",
		auth.CheckPermission("sys:resource:page"),
		BuildBootstrapMenusHandler,
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

	total, items, err := Page(p.Page, p.Size, p.Keyword, p.Type, p.Status)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []ResourceVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req ResourceCreateReq
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
	var req ResourceModifyReq
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
			"type":       item.Type,
			"category":   item.Category,
			"icon":       item.Icon,
			"path":       item.Path,
			"component":  item.Component,
			"sort_code":  item.SortCode,
			"status":     item.Status,
			"created_at": item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders(ResourceExportFields, ResourceExportFieldNames)
	excelBytes, err := utils.ExportExcel(data, headers, "资源数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="resource_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders(ResourceExportFields, ResourceExportFieldNames)
	excelBytes, err := utils.ExportExcel(nil, headers, "资源导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="resource_template.xlsx"`)
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

	rows, err := utils.ParseExcel(fileBytes, "资源导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := Create(&ResourceCreateReq{
			Name: row["资源名称"],
			Code: row["资源编码"],
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

func BuildBootstrapMenusHandler(c *gin.Context) {
	menus, err := BuildBootstrapMenus()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, menus)
}

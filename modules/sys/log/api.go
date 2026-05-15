package log

import (
	"fmt"
	"strconv"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	syslog "hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
	"hei-gin/core/utils"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/log/page",
		auth.CheckPermission("sys:log:page"),
		PageHandler,
	)
	r.GET("/api/v1/sys/log/detail",
		auth.CheckPermission("sys:log:detail"),
		DetailHandler,
	)
	r.POST("/api/v1/sys/log/remove",
		syslog.SysLog("删除操作日志"),
		auth.CheckPermission("sys:log:remove"),
		RemoveHandler,
	)
	r.POST("/api/v1/sys/log/delete-by-category",
		syslog.SysLog("按分类清空日志"),
		auth.CheckPermission("sys:log:remove"),
		norepeat.NoRepeat(5000),
		DeleteByCategoryHandler,
	)
	r.POST("/api/v1/sys/log/create",
		auth.CheckPermission("sys:log:create"),
		CreateHandler,
	)
	r.POST("/api/v1/sys/log/modify",
		auth.CheckPermission("sys:log:modify"),
		ModifyHandler,
	)
	r.GET("/api/v1/sys/log/export",
		syslog.SysLog("导出日志数据"),
		auth.CheckPermission("sys:log:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/log/template",
		auth.CheckPermission("sys:log:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/log/import",
		syslog.SysLog("导入日志数据"),
		auth.CheckPermission("sys:log:import"),
		norepeat.NoRepeat(5000),
		ImportHandler,
	)
	r.GET("/api/v1/sys/log/vis/line-chart-data",
		auth.CheckPermission("sys:log:page"),
		VisLineChartDataHandler,
	)
	r.GET("/api/v1/sys/log/vis/pie-chart-data",
		auth.CheckPermission("sys:log:page"),
		VisPieChartDataHandler,
	)
	r.GET("/api/v1/sys/log/op/bar-chart-data",
		auth.CheckPermission("sys:log:page"),
		OpBarChartDataHandler,
	)
	r.GET("/api/v1/sys/log/op/pie-chart-data",
		auth.CheckPermission("sys:log:page"),
		OpPieChartDataHandler,
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

	total, items, err := Page(p.Page, p.Size, p.Keyword, p.ExeStatus, p.ReqMethod)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []SysLogVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
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

func DeleteByCategoryHandler(c *gin.Context) {
	var req CleanReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}
	if err := DeleteByCategory(req.Category); err != nil {
		result.Failure(c, "删除失败", 500)
		return
	}
	result.Success(c, nil)
}

func CreateHandler(c *gin.Context) {
	var req SysLogVO
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}
	item, err := Create(req)
	if err != nil {
		result.Failure(c, "创建失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func ModifyHandler(c *gin.Context) {
	var req SysLogVO
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}
	if err := Modify(req); err != nil {
		result.Failure(c, "修改失败", 500)
		return
	}
	result.Success(c, nil)
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
			"category":   item.Category,
			"exe_status": item.ExeStatus,
			"op_ip":      item.OpIP,
			"req_method": item.ReqMethod,
			"req_url":    item.ReqURL,
			"op_time":    item.OpTime,
			"op_user":    item.OpUser,
			"created_at": item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders(LogExportFields, LogExportFieldNames)
	excelBytes, err := utils.ExportExcel(data, headers, "日志数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="log_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders(LogExportFields, LogExportFieldNames)
	excelBytes, err := utils.ExportExcel(nil, headers, "日志导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="log_template.xlsx"`)
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

	rows, err := utils.ParseExcel(fileBytes, "日志导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	success := 0
	for _, row := range rows {
		_, err := CreateFromImport(row)
		if err == nil {
			success++
		}
	}

	result.Success(c, map[string]int{"success": success, "total": len(rows)})
}

func VisLineChartDataHandler(c *gin.Context) {
	data, err := VisLineChartData()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}

func VisPieChartDataHandler(c *gin.Context) {
	data, err := VisPieChartData()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}

func OpBarChartDataHandler(c *gin.Context) {
	data, err := OpBarChartData()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}

func OpPieChartDataHandler(c *gin.Context) {
	data, err := OpPieChartData()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}

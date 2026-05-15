package notice

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
	r.GET("/api/v1/sys/notice/page",
		auth.CheckPermission("sys:notice:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/notice/create",
		log.SysLog("添加通知"),
		auth.CheckPermission("sys:notice:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/notice/modify",
		log.SysLog("编辑通知"),
		auth.CheckPermission("sys:notice:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/notice/remove",
		log.SysLog("删除通知"),
		auth.CheckPermission("sys:notice:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/notice/detail",
		auth.CheckPermission("sys:notice:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/notice/export",
		log.SysLog("导出通知数据"),
		auth.CheckPermission("sys:notice:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/notice/template",
		auth.CheckPermission("sys:notice:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/notice/import",
		log.SysLog("导入通知数据"),
		auth.CheckPermission("sys:notice:import"),
		norepeat.NoRepeat(5000),
		ImportHandler,
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

	var vos []NoticeVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req NoticeCreateReq
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
	var req NoticeModifyReq
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
		publishTime := ""
		if !item.PublishTime.IsZero() {
			publishTime = item.PublishTime.Format("2006-01-02 15:04:05")
		}
		row := map[string]interface{}{
			"title":        item.Title,
			"category":     item.Category,
			"status":       item.Status,
			"publish_time": publishTime,
			"created_at":   item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders(NoticeExportFields, NoticeExportFieldNames)
	excelBytes, err := utils.ExportExcel(data, headers, "公告数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="notice_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders(NoticeExportFields, NoticeExportFieldNames)
	excelBytes, err := utils.ExportExcel(nil, headers, "公告导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="notice_template.xlsx"`)
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

	rows, err := utils.ParseExcel(fileBytes, "公告导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := Create(&NoticeCreateReq{
			Title:    row["公告标题"],
			Category: row["公告类别"],
		}, loginID)
		if err == nil {
			success++
		}
	}

	result.Success(c, map[string]int{"success": success, "total": len(rows)})
}

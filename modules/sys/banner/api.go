package banner

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
	r.GET("/api/v1/sys/banner/page",
		auth.CheckPermission("sys:banner:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/banner/create",
		log.SysLog("添加Banner"),
		auth.CheckPermission("sys:banner:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/banner/modify",
		log.SysLog("编辑Banner"),
		auth.CheckPermission("sys:banner:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/banner/remove",
		log.SysLog("删除Banner"),
		auth.CheckPermission("sys:banner:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/banner/detail",
		auth.CheckPermission("sys:banner:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/banner/export",
		log.SysLog("导出Banner数据"),
		auth.CheckPermission("sys:banner:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/banner/template",
		auth.CheckPermission("sys:banner:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/banner/import",
		log.SysLog("导入Banner数据"),
		auth.CheckPermission("sys:banner:import"),
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

	total, items, err := Page(p.Page, p.Size, p.Keyword, p.Category)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []BannerVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req BannerCreateReq
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
	var req BannerModifyReq
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
			"title":      item.Title,
			"image":      item.Image,
			"category":   item.Category,
			"position":   item.Position,
			"sort_code":  item.SortCode,
			"created_at": item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders(BannerExportFields, BannerExportFieldNames)
	excelBytes, err := utils.ExportExcel(data, headers, "Banner数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="banner_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders(BannerExportFields, BannerExportFieldNames)
	excelBytes, err := utils.ExportExcel(nil, headers, "Banner导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="banner_template.xlsx"`)
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

	rows, err := utils.ParseExcel(fileBytes, "Banner导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := Create(&BannerCreateReq{
			Title:    row["轮播标题"],
			Image:    row["轮播图片"],
			Category: row["轮播类别"],
			Position: row["展示位置"],
		}, loginID)
		if err == nil {
			success++
		}
	}

	result.Success(c, map[string]int{"success": success, "total": len(rows)})
}

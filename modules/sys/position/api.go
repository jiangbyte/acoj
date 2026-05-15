package position

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
	r.GET("/api/v1/sys/position/page",
		log.SysLog("查看职位列表"),
		auth.CheckPermission("sys:position:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/position/create",
		log.SysLog("添加岗位"),
		auth.CheckPermission("sys:position:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/position/modify",
		log.SysLog("编辑岗位"),
		auth.CheckPermission("sys:position:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/position/remove",
		log.SysLog("删除岗位"),
		auth.CheckPermission("sys:position:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/position/detail",
		auth.CheckPermission("sys:position:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/position/export",
		auth.CheckPermission("sys:position:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/position/template",
		auth.CheckPermission("sys:position:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/position/import",
		auth.CheckPermission("sys:position:import"),
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

	var vos []PositionVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req PositionCreateReq
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
	var req PositionModifyReq
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
			"sort_code":   item.SortCode,
			"status":      item.Status,
			"description": item.Description,
			"created_at":  item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders([]string{"name", "code", "sort_code", "status", "description", "created_at"},
		map[string]string{
			"name":        "岗位名称",
			"code":        "岗位编码",
			"sort_code":   "排序",
			"status":      "状态",
			"description": "描述",
			"created_at":  "创建时间",
		})
	excelBytes, err := utils.ExportExcel(data, headers, "岗位数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="position_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders([]string{"name", "code", "sort_code", "status", "description", "created_at"},
		map[string]string{
			"name":        "岗位名称",
			"code":        "岗位编码",
			"sort_code":   "排序",
			"status":      "状态",
			"description": "描述",
			"created_at":  "创建时间",
		})
	excelBytes, err := utils.ExportExcel(nil, headers, "岗位导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="position_template.xlsx"`)
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

	rows, err := utils.ParseExcel(fileBytes, "岗位导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := Create(&PositionCreateReq{
			Name: row["岗位名称"],
			Code: row["岗位编码"],
		}, loginID)
		if err == nil {
			success++
		}
	}

	result.Success(c, map[string]int{"success": success, "total": len(rows)})
}

package dict

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
	r.GET("/api/v1/sys/dict/page",
		auth.CheckPermission("sys:dict:page"),
		PageHandler,
	)
	r.GET("/api/v1/sys/dict/list",
		auth.CheckPermission("sys:dict:list"),
		ListHandler,
	)
	r.GET("/api/v1/sys/dict/tree",
		auth.CheckPermission("sys:dict:tree"),
		TreeHandler,
	)
	r.POST("/api/v1/sys/dict/create",
		syslog.SysLog("添加字典"),
		auth.CheckPermission("sys:dict:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/dict/modify",
		syslog.SysLog("编辑字典"),
		auth.CheckPermission("sys:dict:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/dict/remove",
		syslog.SysLog("删除字典"),
		auth.CheckPermission("sys:dict:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/dict/detail",
		auth.CheckPermission("sys:dict:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/dict/get-label",
		auth.CheckPermission("sys:dict:get-label"),
		GetLabelHandler,
	)
	r.GET("/api/v1/sys/dict/get-children",
		auth.CheckPermission("sys:dict:get-children"),
		GetChildrenHandler,
	)
	r.GET("/api/v1/sys/dict/export",
		syslog.SysLog("导出字典数据"),
		auth.CheckPermission("sys:dict:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/dict/template",
		auth.CheckPermission("sys:dict:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/dict/import",
		syslog.SysLog("导入字典数据"),
		auth.CheckPermission("sys:dict:import"),
		norepeat.NoRepeat(5000),
		ImportHandler,
	)
}

// ========================================================================
//  Page
// ========================================================================

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

	var vos []DictVO
	for _, item := range items {
		vos = append(vos, dictToVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

// ========================================================================
//  List
// ========================================================================

func ListHandler(c *gin.Context) {
	var p PageParam
	if err := c.ShouldBindQuery(&p); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	items, err := List(p.Keyword, p.Category)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []DictVO
	for _, item := range items {
		vos = append(vos, dictToVO(item))
	}
	result.Success(c, vos)
}

// ========================================================================
//  Tree
// ========================================================================

func TreeHandler(c *gin.Context) {
	tree, err := Tree()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, tree)
}

// ========================================================================
//  Create
// ========================================================================

func CreateHandler(c *gin.Context) {
	var req DictCreateReq
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
	result.Success(c, toTreeNode(item))
}

// ========================================================================
//  Modify
// ========================================================================

func ModifyHandler(c *gin.Context) {
	var req DictModifyReq
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
	result.Success(c, toTreeNode(item))
}

// ========================================================================
//  Remove
// ========================================================================

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

// ========================================================================
//  Detail
// ========================================================================

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
	result.Success(c, toTreeNode(item))
}

// ========================================================================
//  GetLabel
// ========================================================================

func GetLabelHandler(c *gin.Context) {
	var req GetLabelReq
	if err := c.ShouldBindQuery(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	label, err := GetLabel(req.TypeCode, req.Value)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, gin.H{"label": label})
}

// ========================================================================
//  GetChildren
// ========================================================================

func GetChildrenHandler(c *gin.Context) {
	var req GetChildrenReq
	if err := c.ShouldBindQuery(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	node, err := GetChildren(req.TypeCode)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, node)
}

// ========================================================================
//  Export
// ========================================================================

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
			"category":    item.Category,
			"description": item.Description,
			"sort_code":   item.SortCode,
			"status":      item.Status,
			"created_at":  item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders(DictExportFields, DictExportFieldNames)
	excelBytes, err := utils.ExportExcel(data, headers, "字典数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="dict_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

// ========================================================================
//  Template
// ========================================================================

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders(DictExportFields, DictExportFieldNames)
	excelBytes, err := utils.ExportExcel(nil, headers, "字典导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="dict_template.xlsx"`)
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

// ========================================================================
//  Import
// ========================================================================

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

	rows, err := utils.ParseExcel(fileBytes, "字典导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := CreateFromImport(row, loginID)
		if err == nil {
			success++
		}
	}

	result.Success(c, map[string]int{"success": success, "total": len(rows)})
}

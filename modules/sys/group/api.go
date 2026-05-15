package group

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
	r.GET("/api/v1/sys/group/page",
		auth.CheckPermission("sys:group:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/group/create",
		log.SysLog("添加用户组"),
		auth.CheckPermission("sys:group:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/group/modify",
		log.SysLog("编辑用户组"),
		auth.CheckPermission("sys:group:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/group/remove",
		log.SysLog("删除用户组"),
		auth.CheckPermission("sys:group:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/group/detail",
		auth.CheckPermission("sys:group:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/group/export",
		log.SysLog("导出用户组数据"),
		auth.CheckPermission("sys:group:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/group/template",
		auth.CheckPermission("sys:group:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/group/import",
		log.SysLog("导入用户组数据"),
		auth.CheckPermission("sys:group:import"),
		norepeat.NoRepeat(5000),
		ImportHandler,
	)
	r.GET("/api/v1/sys/group/treeselect",
		auth.CheckPermission("sys:group:treeselect"),
		TreeSelectHandler,
	)
	r.GET("/api/v1/sys/group/tree",
		auth.CheckPermission("sys:group:tree"),
		TreeHandler,
	)
	r.GET("/api/v1/sys/group/union-tree",
		auth.CheckPermission("sys:group:tree"),
		UnionTreeHandler,
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

	var vos []GroupVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req GroupCreateReq
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
	var req GroupModifyReq
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
			"parent_id":  item.ParentID,
			"sort_code":  item.SortCode,
			"status":     item.Status,
			"created_at": item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}

	headers := utils.BuildHeaders([]string{"name", "code", "parent_id", "sort_code", "status", "created_at"},
		map[string]string{
			"name":       "组别名称",
			"code":       "组别编码",
			"parent_id":  "父级ID",
			"sort_code":  "排序",
			"status":     "状态",
			"created_at": "创建时间",
		})
	excelBytes, err := utils.ExportExcel(data, headers, "组别数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="group_export.xlsx"`))
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders([]string{"name", "code", "parent_id", "sort_code", "status", "created_at"},
		map[string]string{
			"name":       "组别名称",
			"code":       "组别编码",
			"parent_id":  "父级ID",
			"sort_code":  "排序",
			"status":     "状态",
			"created_at": "创建时间",
		})
	excelBytes, err := utils.ExportExcel(nil, headers, "组别导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}

	c.Header("Content-Disposition", `attachment; filename="group_template.xlsx"`)
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

	rows, err := utils.ParseExcel(fileBytes, "组别导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := Create(&GroupCreateReq{
			Name: row["组别名称"],
			Code: row["组别编码"],
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

func TreeHandler(c *gin.Context) {
	orgID := c.Query("org_id")
	keyword := c.Query("keyword")

	tree, err := Tree(orgID, keyword)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	if tree == nil {
		tree = []*TreeGroup{}
	}
	result.Success(c, tree)
}

func UnionTreeHandler(c *gin.Context) {
	tree, err := UnionTree()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	if tree == nil {
		tree = []*TreeGroup{}
	}
	result.Success(c, tree)
}

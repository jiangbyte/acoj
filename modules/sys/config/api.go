package config

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/config/page",
		auth.CheckPermission("sys:config:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/config/create",
		log.SysLog("添加配置"),
		auth.CheckPermission("sys:config:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/config/modify",
		log.SysLog("编辑配置"),
		auth.CheckPermission("sys:config:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/config/remove",
		log.SysLog("删除配置"),
		auth.CheckPermission("sys:config:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/config/detail",
		auth.CheckPermission("sys:config:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/config/list-by-category",
		auth.CheckPermission("sys:config:list"),
		ListByCategoryHandler,
	)
	r.POST("/api/v1/sys/config/edit-batch",
		log.SysLog("批量编辑配置"),
		auth.CheckPermission("sys:config:edit"),
		norepeat.NoRepeat(3000),
		EditBatchHandler,
	)
	r.POST("/api/v1/sys/config/edit-by-category",
		log.SysLog("按分类批量编辑配置"),
		auth.CheckPermission("sys:config:edit"),
		norepeat.NoRepeat(3000),
		EditByCategoryHandler,
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

	total, items, err := Page(p.Page, p.Size, p.Keyword, p.Category, p.Status)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []ConfigVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req ConfigCreateReq
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
	var req ConfigModifyReq
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

func ListByCategoryHandler(c *gin.Context) {
	category := c.Query("category")
	if category == "" {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	items, err := QueryByCategory(category)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []ConfigVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Success(c, vos)
}

func EditBatchHandler(c *gin.Context) {
	var req EditBatchReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	if err := EditBatch(req.Configs); err != nil {
		result.Failure(c, "编辑失败", 500)
		return
	}
	result.Success(c, nil)
}

func EditByCategoryHandler(c *gin.Context) {
	var req EditByCategoryReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	if err := EditByCategory(req.Category, req.Configs); err != nil {
		result.Failure(c, "编辑失败", 500)
		return
	}
	result.Success(c, nil)
}

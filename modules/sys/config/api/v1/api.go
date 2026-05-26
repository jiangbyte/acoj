package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/result"
	config "hei-gin/modules/sys/config"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/sys/config/page",
		middleware.HeiCheckPermission([]string{"sys:config:page"}),
		pageHandler,
	)

	r.GET("/api/v1/sys/config/list-by-category",
		middleware.HeiCheckPermission([]string{"sys:config:list"}),
		listByCategoryHandler,
	)

	r.POST("/api/v1/sys/config/create",
		middleware.HeiCheckPermission([]string{"sys:config:create"}),
		log.SysLog("添加配置"),
		middleware.NoRepeat(3000),
		createHandler,
	)

	r.POST("/api/v1/sys/config/modify",
		middleware.HeiCheckPermission([]string{"sys:config:modify"}),
		log.SysLog("编辑配置"),
		modifyHandler,
	)

	r.POST("/api/v1/sys/config/remove",
		middleware.HeiCheckPermission([]string{"sys:config:remove"}),
		log.SysLog("删除配置"),
		deleteHandler,
	)

	r.GET("/api/v1/sys/config/detail",
		middleware.HeiCheckPermission([]string{"sys:config:detail"}),
		detailHandler,
	)

	r.POST("/api/v1/sys/config/edit-batch",
		middleware.HeiCheckPermission([]string{"sys:config:edit"}),
		log.SysLog("批量编辑配置"),
		middleware.NoRepeat(3000),
		editBatchHandler,
	)

	r.POST("/api/v1/sys/config/edit-by-category",
		middleware.HeiCheckPermission([]string{"sys:config:edit"}),
		log.SysLog("按分类批量编辑配置"),
		middleware.NoRepeat(3000),
		editByCategoryHandler,
	)
}

func pageHandler(c *gin.Context) {
	var param config.ConfigPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := config.Page(c, &param)
	c.JSON(200, data)
}

func listByCategoryHandler(c *gin.Context) {
	var param config.ConfigListParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	vos := config.ListByCategory(c, param.Category)
	c.JSON(200, result.Success(c, vos))
}

func createHandler(c *gin.Context) {
	var vo config.ConfigVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	config.Create(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var vo config.ConfigVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	config.Modify(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

func deleteHandler(c *gin.Context) {
	var param struct {
		IDs []string `json:"ids"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	config.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := config.Detail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

func editBatchHandler(c *gin.Context) {
	var param config.ConfigBatchEditParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	config.EditBatch(c, &param, userID)
	c.JSON(200, result.Success(c, nil))
}

func editByCategoryHandler(c *gin.Context) {
	var param config.ConfigCategoryEditParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	config.EditByCategory(c, &param, userID)
	c.JSON(200, result.Success(c, nil))
}

package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/result"
	config "hei-gin/modules/sys/config"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all config routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/config/page
	r.GET("/api/v1/sys/config/page",
		middleware.HeiCheckPermission([]string{"sys:config:page"}),
		pageHandler,
	)

	// GET /api/v1/sys/config/list-by-category
	r.GET("/api/v1/sys/config/list-by-category",
		middleware.HeiCheckPermission([]string{"sys:config:list"}),
		listByCategoryHandler,
	)

	// POST /api/v1/sys/config/create
	r.POST("/api/v1/sys/config/create",
		middleware.HeiCheckPermission([]string{"sys:config:create"}),
		log.SysLog("添加配置"),
		middleware.NoRepeat(3000),
		createHandler,
	)

	// POST /api/v1/sys/config/modify
	r.POST("/api/v1/sys/config/modify",
		middleware.HeiCheckPermission([]string{"sys:config:modify"}),
		log.SysLog("编辑配置"),
		modifyHandler,
	)

	// POST /api/v1/sys/config/remove
	r.POST("/api/v1/sys/config/remove",
		middleware.HeiCheckPermission([]string{"sys:config:remove"}),
		log.SysLog("删除配置"),
		deleteHandler,
	)

	// GET /api/v1/sys/config/detail
	r.GET("/api/v1/sys/config/detail",
		middleware.HeiCheckPermission([]string{"sys:config:detail"}),
		detailHandler,
	)

	// POST /api/v1/sys/config/edit-batch
	r.POST("/api/v1/sys/config/edit-batch",
		middleware.HeiCheckPermission([]string{"sys:config:edit"}),
		log.SysLog("批量编辑配置"),
		middleware.NoRepeat(3000),
		editBatchHandler,
	)

	// POST /api/v1/sys/config/edit-by-category
	r.POST("/api/v1/sys/config/edit-by-category",
		middleware.HeiCheckPermission([]string{"sys:config:edit"}),
		log.SysLog("按分类批量编辑配置"),
		middleware.NoRepeat(3000),
		editByCategoryHandler,
	)
}

// pageHandler handles GET /api/v1/sys/config/page
func pageHandler(c *gin.Context) {
	var param config.ConfigPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := config.Page(c, &param)
	c.JSON(200, data)
}

// listByCategoryHandler handles GET /api/v1/sys/config/list-by-category
func listByCategoryHandler(c *gin.Context) {
	var param config.ConfigListParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	vos := config.ListByCategory(c, param.Category)
	c.JSON(200, result.Success(c, vos))
}

// createHandler handles POST /api/v1/sys/config/create
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

// modifyHandler handles POST /api/v1/sys/config/modify
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

// deleteHandler handles POST /api/v1/sys/config/remove
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

// detailHandler handles GET /api/v1/sys/config/detail
func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := config.Detail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

// editBatchHandler handles POST /api/v1/sys/config/edit-batch
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

// editByCategoryHandler handles POST /api/v1/sys/config/edit-by-category
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

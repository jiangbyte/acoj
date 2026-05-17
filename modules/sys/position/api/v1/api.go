package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/result"
	position "hei-gin/modules/sys/position"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all position routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/position/page
	r.GET("/api/v1/sys/position/page",
		middleware.HeiCheckPermission([]string{"sys:position:page"}),
		log.SysLog("查看职位列表"),
		pageHandler,
	)

	// POST /api/v1/sys/position/create
	r.POST("/api/v1/sys/position/create",
		middleware.HeiCheckPermission([]string{"sys:position:create"}),
		log.SysLog("添加职位"),
		middleware.NoRepeat(3000),
		createHandler,
	)

	// POST /api/v1/sys/position/modify
	r.POST("/api/v1/sys/position/modify",
		middleware.HeiCheckPermission([]string{"sys:position:modify"}),
		log.SysLog("编辑职位"),
		modifyHandler,
	)

	// POST /api/v1/sys/position/remove
	r.POST("/api/v1/sys/position/remove",
		middleware.HeiCheckPermission([]string{"sys:position:remove"}),
		log.SysLog("删除职位"),
		deleteHandler,
	)

	// GET /api/v1/sys/position/detail
	r.GET("/api/v1/sys/position/detail",
		middleware.HeiCheckPermission([]string{"sys:position:detail"}),
		detailHandler,
	)
}

// pageHandler handles GET /api/v1/sys/position/page
func pageHandler(c *gin.Context) {
	var param position.PositionPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := position.Page(c, &param)
	c.JSON(200, data)
}

// createHandler handles POST /api/v1/sys/position/create
func createHandler(c *gin.Context) {
	var vo position.PositionVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	position.Create(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// modifyHandler handles POST /api/v1/sys/position/modify
func modifyHandler(c *gin.Context) {
	var vo position.PositionVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	position.Modify(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// deleteHandler handles POST /api/v1/sys/position/remove
func deleteHandler(c *gin.Context) {
	var param struct {
		IDs []string `json:"ids"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	position.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// detailHandler handles GET /api/v1/sys/position/detail
func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := position.Detail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

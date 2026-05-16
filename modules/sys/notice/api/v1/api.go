package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/result"
	notice "hei-gin/modules/sys/notice"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all notice routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/notice/page
	r.GET("/api/v1/sys/notice/page",
		middleware.HeiCheckPermission([]string{"sys:notice:page"}),
		pageHandler,
	)

	// POST /api/v1/sys/notice/create
	r.POST("/api/v1/sys/notice/create",
		middleware.HeiCheckPermission([]string{"sys:notice:create"}),
		log.SysLog("添加通知"),
		middleware.NoRepeat(3000),
		createHandler,
	)

	// POST /api/v1/sys/notice/modify
	r.POST("/api/v1/sys/notice/modify",
		middleware.HeiCheckPermission([]string{"sys:notice:modify"}),
		log.SysLog("编辑通知"),
		modifyHandler,
	)

	// POST /api/v1/sys/notice/remove
	r.POST("/api/v1/sys/notice/remove",
		middleware.HeiCheckPermission([]string{"sys:notice:remove"}),
		log.SysLog("删除通知"),
		deleteHandler,
	)

	// GET /api/v1/sys/notice/detail
	r.GET("/api/v1/sys/notice/detail",
		middleware.HeiCheckPermission([]string{"sys:notice:detail"}),
		detailHandler,
	)
}

// pageHandler handles GET /api/v1/sys/notice/page
func pageHandler(c *gin.Context) {
	var param notice.NoticePageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := notice.Page(c, &param)
	c.JSON(200, data)
}

// createHandler handles POST /api/v1/sys/notice/create
func createHandler(c *gin.Context) {
	var vo notice.NoticeVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	notice.Create(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// modifyHandler handles POST /api/v1/sys/notice/modify
func modifyHandler(c *gin.Context) {
	var vo notice.NoticeVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	notice.Modify(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// deleteHandler handles POST /api/v1/sys/notice/remove
func deleteHandler(c *gin.Context) {
	var param struct {
		IDs []string `json:"ids"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	notice.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// detailHandler handles GET /api/v1/sys/notice/detail
func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := notice.Detail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

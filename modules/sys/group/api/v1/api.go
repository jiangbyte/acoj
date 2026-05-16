package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/result"
	group "hei-gin/modules/sys/group"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all group routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/group/page
	r.GET("/api/v1/sys/group/page",
		middleware.HeiCheckPermission([]string{"sys:group:page"}),
		pageHandler,
	)

	// GET /api/v1/sys/group/union-tree
	r.GET("/api/v1/sys/group/union-tree",
		middleware.HeiCheckPermission([]string{"sys:group:tree"}),
		unionTreeHandler,
	)

	// GET /api/v1/sys/group/tree
	r.GET("/api/v1/sys/group/tree",
		middleware.HeiCheckPermission([]string{"sys:group:tree"}),
		treeHandler,
	)

	// POST /api/v1/sys/group/create
	r.POST("/api/v1/sys/group/create",
		middleware.HeiCheckPermission([]string{"sys:group:create"}),
		log.SysLog("添加用户组"),
		middleware.NoRepeat(3000),
		createHandler,
	)

	// POST /api/v1/sys/group/modify
	r.POST("/api/v1/sys/group/modify",
		middleware.HeiCheckPermission([]string{"sys:group:modify"}),
		log.SysLog("编辑用户组"),
		modifyHandler,
	)

	// POST /api/v1/sys/group/remove
	r.POST("/api/v1/sys/group/remove",
		middleware.HeiCheckPermission([]string{"sys:group:remove"}),
		log.SysLog("删除用户组"),
		removeHandler,
	)

	// GET /api/v1/sys/group/detail
	r.GET("/api/v1/sys/group/detail",
		middleware.HeiCheckPermission([]string{"sys:group:detail"}),
		detailHandler,
	)
}

// pageHandler handles GET /api/v1/sys/group/page
func pageHandler(c *gin.Context) {
	var param group.GroupPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := group.GroupPage(c, &param)
	c.JSON(200, data)
}

// unionTreeHandler handles GET /api/v1/sys/group/union-tree
func unionTreeHandler(c *gin.Context) {
	data := group.GroupUnionTree(c)
	c.JSON(200, result.Success(c, data))
}

// treeHandler handles GET /api/v1/sys/group/tree
func treeHandler(c *gin.Context) {
	var param group.GroupTreeParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := group.GroupTree(c, &param)
	c.JSON(200, result.Success(c, data))
}

// createHandler handles POST /api/v1/sys/group/create
func createHandler(c *gin.Context) {
	var vo group.GroupVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	group.GroupCreate(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// modifyHandler handles POST /api/v1/sys/group/modify
func modifyHandler(c *gin.Context) {
	var vo group.GroupVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	group.GroupModify(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// removeHandler handles POST /api/v1/sys/group/remove
func removeHandler(c *gin.Context) {
	var param struct {
		IDs []string `json:"ids"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	group.GroupRemove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// detailHandler handles GET /api/v1/sys/group/detail
func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := group.GroupDetail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

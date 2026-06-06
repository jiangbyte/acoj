package v1

import (
	"hei-gin/sdk/auth"
	"hei-gin/sdk/auth/middleware"
	"hei-gin/sdk/log"
	"hei-gin/sdk/pojo"
	"hei-gin/sdk/result"
	"hei-gin/sdk/registry"
	group "hei-gin/plugins/plugin-sys/group"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/sys/group/page",
		registry.Perm("sys:group:page", "分组分页"),
		pageHandler,
	)

	r.GET("/api/v1/sys/group/union-tree",
		registry.Perm("sys:group:tree", "分组树"),
		unionTreeHandler,
	)

	r.GET("/api/v1/sys/group/tree",
		registry.Perm("sys:group:tree", "分组树"),
		treeHandler,
	)

	r.POST("/api/v1/sys/group/create",
		registry.Perm("sys:group:create", "添加分组"),
		log.SysLog("添加用户组"),
		middleware.NoRepeat(3000),
		createHandler,
	)

	r.POST("/api/v1/sys/group/modify",
		registry.Perm("sys:group:modify", "编辑分组"),
		log.SysLog("编辑用户组"),
		modifyHandler,
	)

	r.POST("/api/v1/sys/group/remove",
		registry.Perm("sys:group:remove", "删除分组"),
		log.SysLog("删除用户组"),
		removeHandler,
	)

	r.GET("/api/v1/sys/group/detail",
		registry.Perm("sys:group:detail", "分组详情"),
		detailHandler,
	)
}

func pageHandler(c *gin.Context) {
	var param group.GroupPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := group.Page(c, &param)
	c.JSON(200, data)
}

func unionTreeHandler(c *gin.Context) {
	data := group.Options(c)
	c.JSON(200, result.Success(c, data))
}

func treeHandler(c *gin.Context) {
	var param group.GroupTreeParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := group.Tree(c, &param)
	c.JSON(200, result.Success(c, data))
}

func createHandler(c *gin.Context) {
	var vo group.GroupVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	group.Create(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var vo group.GroupVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	group.Modify(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

func removeHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	group.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := group.Detail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}
func init() {
	registry.RegisterRoute(RegisterRoutes)
}

package v1

import (
	"net/http"

	"hei-gin/sdk/pojo"
	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	problemset "hei-gin/plugins/plugin-judge/problemset"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/judge/problemset/page",
		registry.Perm("judge:problemset:page", "题单分页"),
		pageHandler,
	)
	r.POST("/api/v1/judge/problemset/create",
		registry.Perm("judge:problemset:create", "创建题单"),
		createHandler,
	)
	r.POST("/api/v1/judge/problemset/modify",
		registry.Perm("judge:problemset:modify", "编辑题单"),
		modifyHandler,
	)
	r.POST("/api/v1/judge/problemset/remove",
		registry.Perm("judge:problemset:remove", "删除题单"),
		removeHandler,
	)
	r.GET("/api/v1/judge/problemset/detail",
		registry.Perm("judge:problemset:detail", "题单详情"),
		detailHandler,
	)
}

func pageHandler(c *gin.Context) {
	var param problemset.ProblemsetPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	c.JSON(http.StatusOK, problemset.PageService(c, &param))
}

func createHandler(c *gin.Context) {
	var param problemset.ProblemsetCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := problemset.CreateService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var param problemset.ProblemsetModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := problemset.ModifyService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func removeHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := problemset.RemoveService(c, problemset.ProblemsetRemoveParam(param)); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func detailHandler(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}
	vo, err := problemset.DetailService(c, id)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, vo))
}

func init() {
	registry.RegisterRoute(RegisterRoutes)
}

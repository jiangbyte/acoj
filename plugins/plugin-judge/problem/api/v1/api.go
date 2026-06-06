package v1

import (
	"net/http"

	"hei-gin/sdk/exception"
	"hei-gin/sdk/pojo"
	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	problem "hei-gin/plugins/plugin-judge/problem"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/judge/problem/page
	r.GET("/api/v1/judge/problem/page",
		registry.Perm("judge:problem:page", "题目分页"),
		pageHandler,
	)

	// POST /api/v1/judge/problem/create
	r.POST("/api/v1/judge/problem/create",
		registry.Perm("judge:problem:create", "创建题目"),
		createHandler,
	)

	// POST /api/v1/judge/problem/modify
	r.POST("/api/v1/judge/problem/modify",
		registry.Perm("judge:problem:modify", "编辑题目"),
		modifyHandler,
	)

	// POST /api/v1/judge/problem/remove
	r.POST("/api/v1/judge/problem/remove",
		registry.Perm("judge:problem:remove", "删除题目"),
		removeHandler,
	)

	// GET /api/v1/judge/problem/detail
	r.GET("/api/v1/judge/problem/detail",
		registry.Perm("judge:problem:detail", "题目详情"),
		detailHandler,
	)
}

func pageHandler(c *gin.Context) {
	var param problem.ProblemPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	c.JSON(http.StatusOK, problem.PageService(c, &param))
}

func createHandler(c *gin.Context) {
	var param problem.ProblemCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := problem.CreateService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var param problem.ProblemModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := problem.ModifyService(c, &param); err != nil {
		code := 500
		if _, ok := err.(*exception.BusinessError); ok {
			code = 400
		}
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), code, nil))
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
	if err := problem.RemoveService(c, param); err != nil {
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
	vo, err := problem.DetailService(c, id)
	if err != nil {
		code := 500
		if _, ok := err.(*exception.BusinessError); ok {
			code = 400
		}
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), code, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, vo))
}

func init() {
	registry.RegisterRoute(RegisterRoutes)
}

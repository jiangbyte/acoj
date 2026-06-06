package v1

import (
	"net/http"

	"hei-gin/sdk/auth/middleware"
	"hei-gin/sdk/log"
	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	problemset "hei-gin/plugins/plugin-judge/problemset"

	"github.com/gin-gonic/gin"
)

// RegisterPublicRoutes registers public + client problemset routes.
func RegisterPublicRoutes(r *gin.Engine) {
	// Public: browse active problem sets
	r.GET("/api/v1/public/c/judge/problemset/page", publicPageHandler)
	r.GET("/api/v1/public/c/judge/problemset/detail", publicDetailHandler)

	// Client: manage own problem sets
	r.GET("/api/v1/c/judge/problemset/my-sets",
		middleware.HeiClientCheckLogin(),
		clientMySetsHandler,
	)
	r.POST("/api/v1/c/judge/problemset/create",
		middleware.HeiClientCheckLogin(),
		log.SysLog("C端创建题单"),
		clientCreateHandler,
	)
	r.POST("/api/v1/c/judge/problemset/modify",
		middleware.HeiClientCheckLogin(),
		log.SysLog("C端编辑题单"),
		clientModifyHandler,
	)
	r.POST("/api/v1/c/judge/problemset/remove",
		middleware.HeiClientCheckLogin(),
		log.SysLog("C端删除题单"),
		clientRemoveHandler,
	)
}

func publicPageHandler(c *gin.Context) {
	var param problemset.ProblemsetPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if param.Status == "" {
		param.Status = "ACTIVE"
	}
	c.JSON(http.StatusOK, problemset.PageService(c, &param))
}

func publicDetailHandler(c *gin.Context) {
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
	if vo.Status != "ACTIVE" {
		c.JSON(http.StatusOK, result.Failure(c, "题单不存在", 400, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, vo))
}

func clientMySetsHandler(c *gin.Context) {
	var param problemset.ProblemsetPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	c.JSON(http.StatusOK, problemset.ClientMySetsService(c, &param))
}

func clientCreateHandler(c *gin.Context) {
	var param problemset.ProblemsetCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := problemset.ClientCreateService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func clientModifyHandler(c *gin.Context) {
	var param problemset.ProblemsetModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	// 使用 ClientModifyService（内含归属校验）
	if err := problemset.ClientModifyService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 400, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func clientRemoveHandler(c *gin.Context) {
	var param problemset.ProblemsetRemoveParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	// 使用 ClientRemoveService（内含归属校验）
	if err := problemset.ClientRemoveService(c, param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 400, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func init() {
	registry.RegisterRoute(RegisterPublicRoutes)
}

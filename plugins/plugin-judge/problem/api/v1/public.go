package v1

import (
	"net/http"

	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	problem "hei-gin/plugins/plugin-judge/problem"

	"github.com/gin-gonic/gin"
)

// RegisterPublicRoutes registers public (C-end accessible) problem routes.
func RegisterPublicRoutes(r *gin.Engine) {
	r.GET("/api/v1/public/c/judge/problem/page", publicPageHandler)
	r.GET("/api/v1/public/c/judge/problem/detail", publicDetailHandler)
}

func publicPageHandler(c *gin.Context) {
	var param problem.ProblemPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	// 公开接口只展示已激活题目
	if param.Status == "" {
		param.Status = "ACTIVE"
	}
	c.JSON(http.StatusOK, problem.PageService(c, &param))
}

func publicDetailHandler(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}
	// 公开详情隐藏判题配置等敏感字段
	vo, err := problem.PublicDetailService(c, id)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 400, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, vo))
}

func init() {
	registry.RegisterRoute(RegisterPublicRoutes)
}

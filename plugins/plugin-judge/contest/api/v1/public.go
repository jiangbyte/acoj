package v1

import (
	"net/http"

	"hei-gin/sdk/auth/middleware"
	"hei-gin/sdk/log"
	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	contest "hei-gin/plugins/plugin-judge/contest"

	"github.com/gin-gonic/gin"
)

// RegisterPublicRoutes registers public (C-end accessible) contest routes.
func RegisterPublicRoutes(r *gin.Engine) {
	r.GET("/api/v1/public/c/judge/contest/page", publicPageHandler)
	r.GET("/api/v1/public/c/judge/contest/detail", publicDetailHandler)
	r.GET("/api/v1/public/c/judge/contest/rank", publicRankHandler)

	// Client-authenticated routes
	r.POST("/api/v1/c/judge/contest/register",
		middleware.HeiClientCheckLogin(),
		log.SysLog("C端报名竞赛"),
		clientRegisterHandler,
	)
}

func publicPageHandler(c *gin.Context) {
	var param contest.ContestPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	c.JSON(http.StatusOK, contest.PageService(c, &param))
}

func publicDetailHandler(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}
	vo, err := contest.DetailService(c, id)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, vo))
}

func publicRankHandler(c *gin.Context) {
	contestID := c.Query("contest_id")
	if contestID == "" {
		c.JSON(http.StatusOK, result.Failure(c, "contest_id不能为空", 400, nil))
		return
	}
	rankList, err := contest.CalculateRank(contestID)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, rankList))
}

func clientRegisterHandler(c *gin.Context) {
	var param contest.ContestRegisterParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	// 使用C端报名服务（内部使用 Consumer.GetLoginID）
	if err := contest.ClientRegisterService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func init() {
	registry.RegisterRoute(RegisterPublicRoutes)
}

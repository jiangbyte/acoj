package v1

import (
	"net/http"

	"hei-gin/sdk/pojo"
	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	contest "hei-gin/plugins/plugin-judge/contest"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/judge/contest/page",
		registry.Perm("judge:contest:page", "竞赛分页"),
		pageHandler,
	)
	r.POST("/api/v1/judge/contest/create",
		registry.Perm("judge:contest:create", "创建竞赛"),
		createHandler,
	)
	r.POST("/api/v1/judge/contest/modify",
		registry.Perm("judge:contest:modify", "编辑竞赛"),
		modifyHandler,
	)
	r.POST("/api/v1/judge/contest/remove",
		registry.Perm("judge:contest:remove", "删除竞赛"),
		removeHandler,
	)
	r.GET("/api/v1/judge/contest/detail",
		registry.Perm("judge:contest:detail", "竞赛详情"),
		detailHandler,
	)
	r.POST("/api/v1/judge/contest/register",
		registry.Perm("judge:contest:register", "报名竞赛"),
		registerHandler,
	)
	r.GET("/api/v1/judge/contest/rank",
		registry.Perm("judge:contest:rank", "竞赛排行榜"),
		rankHandler,
	)
}

func pageHandler(c *gin.Context) {
	var param contest.ContestPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	c.JSON(http.StatusOK, contest.PageService(c, &param))
}

func createHandler(c *gin.Context) {
	var param contest.ContestCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := contest.CreateService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var param contest.ContestModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := contest.ModifyService(c, &param); err != nil {
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
	if err := contest.RemoveService(c, contest.ContestRemoveParam(param)); err != nil {
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
	vo, err := contest.DetailService(c, id)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, vo))
}

func registerHandler(c *gin.Context) {
	var param contest.ContestRegisterParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := contest.RegisterService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func rankHandler(c *gin.Context) {
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

func init() {
	registry.RegisterRoute(RegisterRoutes)
}

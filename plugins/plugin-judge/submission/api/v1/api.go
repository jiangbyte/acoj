package v1

import (
	"net/http"

	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	"hei-gin/plugins/plugin-judge/judge"
	submission "hei-gin/plugins/plugin-judge/submission"

	"github.com/gin-gonic/gin"
)

// JudgeEngineRef 全局判题引擎引用，由 plugin 注入
var JudgeEngineRef *judge.JudgeEngine

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/judge/submission/page",
		registry.Perm("judge:submission:page", "提交分页"),
		pageHandler,
	)
	r.POST("/api/v1/judge/submission/create",
		registry.Perm("judge:submission:create", "创建提交"),
		createHandler,
	)
	r.POST("/api/v1/judge/submission/rejudge",
		registry.Perm("judge:submission:rejudge", "重新判题"),
		rejudgeHandler,
	)
	r.GET("/api/v1/judge/submission/detail",
		registry.Perm("judge:submission:detail", "提交详情"),
		detailHandler,
	)
}

func pageHandler(c *gin.Context) {
	var param submission.SubmissionPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	c.JSON(http.StatusOK, submission.PageService(c, &param))
}

func createHandler(c *gin.Context) {
	var param submission.SubmissionCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := submission.CreateService(c, &param, JudgeEngineRef); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func rejudgeHandler(c *gin.Context) {
	var param submission.SubmissionRejudgeParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := submission.RejudgeService(c, param, JudgeEngineRef); err != nil {
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
	vo, err := submission.DetailService(c, id)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, vo))
}

func init() {
	registry.RegisterRoute(RegisterRoutes)
}

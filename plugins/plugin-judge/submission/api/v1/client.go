package v1

import (
	"net/http"

	"hei-gin/sdk/auth"
	"hei-gin/sdk/auth/middleware"
	"hei-gin/sdk/log"
	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	submission "hei-gin/plugins/plugin-judge/submission"

	"github.com/gin-gonic/gin"
)

// RegisterClientRoutes registers client-authenticated (C-end) submission routes.
func RegisterClientRoutes(r *gin.Engine) {
	r.GET("/api/v1/c/judge/submission/page",
		middleware.HeiClientCheckLogin(),
		clientPageHandler,
	)
	r.POST("/api/v1/c/judge/submission/create",
		middleware.HeiClientCheckLogin(),
		log.SysLog("C端提交代码"),
		clientCreateHandler,
	)
	r.GET("/api/v1/c/judge/submission/detail",
		middleware.HeiClientCheckLogin(),
		clientDetailHandler,
	)
}

func clientPageHandler(c *gin.Context) {
	var param submission.SubmissionPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	// 限制为当前C端用户自己的提交
	userID := auth.Consumer.GetLoginID(c)
	param.UserID = userID
	c.JSON(http.StatusOK, submission.PageService(c, &param))
}

func clientCreateHandler(c *gin.Context) {
	var param submission.SubmissionCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	submissionID, err := submission.ClientCreateService(c, &param, JudgeEngineRef)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, gin.H{"submission_id": submissionID}))
}

func clientDetailHandler(c *gin.Context) {
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
	// 校验归属
	userID := auth.Consumer.GetLoginID(c)
	if vo.UserID != userID {
		c.JSON(http.StatusOK, result.Failure(c, "无权访问", 403, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, vo))
}

func init() {
	registry.RegisterRoute(RegisterClientRoutes)
}

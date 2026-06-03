package v1

import (
	"context"

	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/result"
	submission "hei-gin/modules/judge/submission"
	contest "hei-gin/modules/judge/contest"

	"github.com/gin-gonic/gin"
)

var clientAuth = auth.NewHeiClientAuthTool()

// RegisterRoutes registers admin routes.
func RegisterRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/judge/submission")
	g.POST("/create", log.SysLog("提交代码"), createHandler)
	g.GET("/page", pageHandler)
	g.GET("/detail", detailHandler)
	g.GET("/testcases", testcasesHandler)
	g.POST("/rejudge", log.SysLog("重判"), rejudgeHandler)
}

// RegisterPublicRoutes registers public and client routes.
func RegisterPublicRoutes(r *gin.Engine) {
	// Public (no auth)
	g := r.Group("/api/v1/public/judge/submission")
	g.GET("/page", pageHandler)
	g.GET("/detail", detailHandler)
	g.GET("/testcases", testcasesHandler)

	// Client auth (C-end login required)
	cg := r.Group("/api/v1/c/judge/submission", middleware.HeiClientCheckLogin())
	cg.POST("/create", log.SysLog("提交代码"), clientCreateHandler)
}

func pageHandler(c *gin.Context) {
	var param submission.SubmissionPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := submission.Page(c, &param)
	c.JSON(200, data)
}

func createHandler(c *gin.Context) {
	var param submission.SubmissionCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)

	// Validate contest access if in contest context
	if param.ContestID != nil && *param.ContestID != "" {
		if err := contest.ValidateContestAccess(context.Background(), *param.ContestID, userID); err != nil {
			c.JSON(200, result.Failure(c, err.Error(), 400, nil))
			return
		}
	}

	vo := submission.Create(c, &param, userID)
	c.JSON(200, result.Success(c, vo))
}

func clientCreateHandler(c *gin.Context) {
	var param submission.SubmissionCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := clientAuth.GetLoginIDDefaultNull(c)

	// Validate contest access if in contest context
	if param.ContestID != nil && *param.ContestID != "" {
		if err := contest.ValidateContestAccess(context.Background(), *param.ContestID, userID); err != nil {
			c.JSON(200, result.Failure(c, err.Error(), 400, nil))
			return
		}
	}

	vo := submission.Create(c, &param, userID)
	c.JSON(200, result.Success(c, vo))
}

func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := submission.Detail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

func testcasesHandler(c *gin.Context) {
	id := c.Query("id")
	data := submission.GetTestcaseResults(c, id)
	c.JSON(200, data)
}

func rejudgeHandler(c *gin.Context) {
	var param struct {
		ID string `json:"id"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	submission.Rejudge(c, param.ID)
	c.JSON(200, result.Success(c, nil))
}

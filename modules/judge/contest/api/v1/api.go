package v1

import (
	"context"

	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/db"
	"hei-gin/core/log"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	contest "hei-gin/modules/judge/contest"
	"hei-gin/modules/judge/contest/rank"

	"github.com/gin-gonic/gin"
)

var clientAuth = auth.NewHeiClientAuthTool()

// RegisterRoutes registers admin routes.
func RegisterRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/judge/contest")
	g.POST("/create", log.SysLog("创建比赛"), createHandler)
	g.POST("/modify", log.SysLog("编辑比赛"), modifyHandler)
	g.POST("/remove", log.SysLog("删除比赛"), deleteHandler)
	g.GET("/page", pageHandler)
	g.GET("/detail", detailHandler)
	g.POST("/problem/sync", log.SysLog("同步比赛题目"), syncProblemsHandler)
	g.POST("/register", log.SysLog("注册参赛"), registerHandler)
	g.POST("/unregister", log.SysLog("取消参赛"), unregisterHandler)
	g.GET("/rank", rankHandler)
	g.GET("/submissions", submissionsHandler)
	g.POST("/status/transition", log.SysLog("触发状态转换"), statusTransitionHandler)
}

// RegisterPublicRoutes registers public and client routes.
func RegisterPublicRoutes(r *gin.Engine) {
	// Public (no auth)
	g := r.Group("/api/v1/public/judge/contest")
	g.GET("/page", pageHandler)
	g.GET("/detail", detailHandler)
	g.GET("/rank", rankHandler)
	g.GET("/problems", problemsHandler)

	// Client auth (C-end login required)
	cg := r.Group("/api/v1/c/judge/contest", middleware.HeiClientCheckLogin())
	cg.POST("/register", log.SysLog("注册参赛"), clientRegisterHandler)
	cg.POST("/unregister", log.SysLog("取消参赛"), clientUnregisterHandler)
	cg.GET("/my-submissions", mySubmissionsHandler)
}

func pageHandler(c *gin.Context) {
	var param contest.ContestPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := contest.Page(c, &param)
	c.JSON(200, data)
}

func createHandler(c *gin.Context) {
	var param contest.ContestCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	contest.Create(c, &param, userID)
	c.JSON(200, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var param contest.ContestModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	contest.Modify(c, &param, userID)
	c.JSON(200, result.Success(c, nil))
}

func deleteHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	contest.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := contest.Detail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

func syncProblemsHandler(c *gin.Context) {
	var param struct {
		ContestID string                     `json:"contest_id"`
		Problems  []contest.ContestProblemVO `json:"problems"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	contest.SyncProblems(c, param.ContestID, param.Problems)
	c.JSON(200, result.Success(c, nil))
}

func registerHandler(c *gin.Context) {
	var param struct{ ContestID string `json:"contest_id"` }
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	contest.Register(c, param.ContestID, userID)
	c.JSON(200, result.Success(c, nil))
}

func unregisterHandler(c *gin.Context) {
	var param struct{ ContestID string `json:"contest_id"` }
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	contest.Unregister(c, param.ContestID, userID)
	c.JSON(200, result.Success(c, nil))
}

func clientRegisterHandler(c *gin.Context) {
	var param struct{ ContestID string `json:"contest_id"` }
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := clientAuth.GetLoginIDDefaultNull(c)
	contest.Register(c, param.ContestID, userID)
	c.JSON(200, result.Success(c, nil))
}

func clientUnregisterHandler(c *gin.Context) {
	var param struct{ ContestID string `json:"contest_id"` }
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := clientAuth.GetLoginIDDefaultNull(c)
	contest.Unregister(c, param.ContestID, userID)
	c.JSON(200, result.Success(c, nil))
}

func rankHandler(c *gin.Context) {
	id := c.Query("id")
	ctx := context.Background()

	var contestEntity contest.JudgeContest
	if err := db.DB.WithContext(ctx).First(&contestEntity, "id = ?", id).Error; err != nil {
		c.JSON(200, result.Success(c, nil))
		return
	}

	var entries interface{}
	var err error

	switch contestEntity.Mode {
	case "ACM", "CF", "HOMEWORK":
		calculator := &rank.ACMRankCalculator{}
		entries, err = calculator.Calculate(ctx, id)
	case "OI":
		calculator := &rank.OIRankCalculator{}
		entries, err = calculator.Calculate(ctx, id)
	case "IOI":
		calculator := &rank.IOIRankCalculator{}
		entries, err = calculator.Calculate(ctx, id)
	default:
		calculator := &rank.ACMRankCalculator{}
		entries, err = calculator.Calculate(ctx, id)
	}

	if err != nil {
		c.JSON(200, result.Failure(c, "计算排名失败: "+err.Error(), 500, nil))
		return
	}

	c.JSON(200, result.Success(c, gin.H{
		"contest_id": id,
		"mode":       contestEntity.Mode,
		"status":     contestEntity.Status,
		"entries":    entries,
	}))
}

func problemsHandler(c *gin.Context) {
	id := c.Query("id")
	problems := contest.GetContestProblems(context.Background(), id)
	c.JSON(200, result.Success(c, problems))
}

func mySubmissionsHandler(c *gin.Context) {
	contestID := c.Query("contest_id")
	userID := clientAuth.GetLoginIDDefaultNull(c)
	if userID == "" {
		userID = auth.GetLoginIDDefaultNull(c)
	}
	subs := contest.GetMyContestSubmissions(context.Background(), contestID, userID, 10)
	c.JSON(200, result.Success(c, subs))
}

func submissionsHandler(c *gin.Context) {
	contestID := c.Query("contest_id")
	ctx := context.Background()
	var subs []contest.JudgeSubmissionInContest
	db.DB.WithContext(ctx).Where("contest_id = ?", contestID).Order("created_at DESC").Find(&subs)
	c.JSON(200, result.Success(c, subs))
}

func statusTransitionHandler(c *gin.Context) {
	contest.StatusTransition(context.Background())
	c.JSON(200, result.Success(c, gin.H{"message": "状态转换完成"}))
}

package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	problemset "hei-gin/modules/judge/problemset"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers admin routes (requires admin auth via AuthCheck middleware).
func RegisterRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/judge/problemset")
	g.POST("/create", log.SysLog("创建题单"), createHandler)
	g.POST("/remove", log.SysLog("删除题单"), deleteHandler)
	g.GET("/page", pageHandler)
	g.GET("/detail", detailHandler)
	g.POST("/problem/sync", log.SysLog("同步题单项"), syncProblemsHandler)
	g.GET("/progress", progressHandler)
}

// RegisterPublicRoutes registers public routes (no auth required).
func RegisterPublicRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/public/judge/problemset")
	g.GET("/page", pageHandler)
	g.GET("/detail", detailHandler)
	g.GET("/progress", progressHandler)
}

func pageHandler(c *gin.Context) {
	var param problemset.ProblemSetPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := problemset.Page(c, &param)
	c.JSON(200, data)
}

func createHandler(c *gin.Context) {
	var param problemset.ProblemSetCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	problemset.Create(c, &param, userID)
	c.JSON(200, result.Success(c, nil))
}

func deleteHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problemset.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := problemset.Detail(c, id)
	if vo == nil { c.JSON(200, result.Success(c, nil)); return }
	c.JSON(200, result.Success(c, vo))
}

func syncProblemsHandler(c *gin.Context) {
	var param struct {
		SetID    string                       `json:"set_id"`
		Problems []problemset.ProblemSetItemVO `json:"problems"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problemset.SyncProblems(c, param.SetID, param.Problems)
	c.JSON(200, result.Success(c, nil))
}

func progressHandler(c *gin.Context) {
	setID := c.Query("set_id")
	userID := auth.GetLoginIDDefaultNull(c)
	data := problemset.GetProgress(c, setID, userID)
	c.JSON(200, data)
}

package v1

import (
	"hei-gin/core/auth/middleware"
	"hei-gin/core/result"
	session "hei-gin/modules/sys/session"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all session routes.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/session/analysis
	r.GET("/api/v1/sys/session/analysis",
		middleware.HeiCheckPermission([]string{"sys:session:page"}),
		sessionAnalysis,
	)

	// GET /api/v1/sys/session/page
	r.GET("/api/v1/sys/session/page",
		middleware.HeiCheckPermission([]string{"sys:session:page"}),
		sessionPage,
	)

	// POST /api/v1/sys/session/exit
	r.POST("/api/v1/sys/session/exit",
		middleware.HeiCheckPermission([]string{"sys:session:exit"}),
		sessionExit,
	)

	// GET /api/v1/sys/session/tokens
	r.GET("/api/v1/sys/session/tokens",
		middleware.HeiCheckPermission([]string{"sys:session:page"}),
		sessionTokens,
	)

	// POST /api/v1/sys/session/exit-token
	r.POST("/api/v1/sys/session/exit-token",
		middleware.HeiCheckPermission([]string{"sys:session:exit"}),
		sessionExitToken,
	)

	// GET /api/v1/sys/session/chart-data
	r.GET("/api/v1/sys/session/chart-data",
		middleware.HeiCheckPermission([]string{"sys:session:page"}),
		sessionChartData,
	)
}

// sessionAnalysis handles GET /api/v1/sys/session/analysis
func sessionAnalysis(c *gin.Context) {
	data := session.Analysis(c)
	c.JSON(200, result.Success(c, data))
}

// sessionPage handles GET /api/v1/sys/session/page
func sessionPage(c *gin.Context) {
	var param session.SessionPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := session.Page(c, &param)
	c.JSON(200, data)
}

// sessionExit handles POST /api/v1/sys/session/exit
func sessionExit(c *gin.Context) {
	var param session.SessionExitParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	session.Exit(c, param.UserID)
	c.JSON(200, result.Success(c, nil))
}

// sessionTokens handles GET /api/v1/sys/session/tokens
func sessionTokens(c *gin.Context) {
	var param struct {
		UserID string `form:"user_id"`
	}
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := session.TokenList(c, param.UserID)
	c.JSON(200, result.Success(c, data))
}

// sessionExitToken handles POST /api/v1/sys/session/exit-token
func sessionExitToken(c *gin.Context) {
	var param session.SessionExitTokenParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	session.ExitToken(c, param.UserID, param.Token)
	c.JSON(200, result.Success(c, nil))
}

// sessionChartData handles GET /api/v1/sys/session/chart-data
func sessionChartData(c *gin.Context) {
	data := session.ChartData(c)
	c.JSON(200, result.Success(c, data))
}

package session

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/session/page",
		auth.CheckPermission("sys:session:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/session/exit",
		log.SysLog("强退会话"),
		auth.CheckPermission("sys:session:exit"),
		ExitHandler,
	)
	r.GET("/api/v1/sys/session/analysis",
		auth.CheckPermission("sys:session:page"),
		AnalysisHandler,
	)
	r.GET("/api/v1/sys/session/tokens",
		auth.CheckPermission("sys:session:page"),
		TokenListHandler,
	)
	r.POST("/api/v1/sys/session/exit-token",
		log.SysLog("退出令牌"),
		auth.CheckPermission("sys:session:exit"),
		ExitTokenHandler,
	)
	r.GET("/api/v1/sys/session/chart-data",
		auth.CheckPermission("sys:session:page"),
		ChartDataHandler,
	)
}

func PageHandler(c *gin.Context) {
	var p PageParam
	if err := c.ShouldBindQuery(&p); err != nil {
		result.ValidationError(c, err)
		return
	}
	if p.Page <= 0 {
		p.Page = 1
	}
	if p.Size <= 0 {
		p.Size = 10
	}

	total, items, err := Page(p.Page, p.Size)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	result.Page(c, items, int64(total), p.Page, p.Size)
}

func ExitHandler(c *gin.Context) {
	var req ExitReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	if req.LoginID == "" {
		result.Failure(c, "登录ID不能为空", 400)
		return
	}

	auth.AuthTool.Kickout(req.LoginID)
	result.Success(c, nil)
}

func AnalysisHandler(c *gin.Context) {
	resultData, err := Analysis()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, resultData)
}

func TokenListHandler(c *gin.Context) {
	userID := c.Query("user_id")
	if userID == "" {
		result.Failure(c, "用户ID不能为空", 400)
		return
	}

	tokens, err := TokenList(userID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	result.Success(c, tokens)
}

func ExitTokenHandler(c *gin.Context) {
	var req ExitTokenReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	if req.UserID == "" || req.Token == "" {
		result.Failure(c, "用户ID和Token不能为空", 400)
		return
	}

	if err := ExitToken(req.UserID, req.Token); err != nil {
		result.Failure(c, "操作失败", 500)
		return
	}

	result.Success(c, nil)
}

func ChartDataHandler(c *gin.Context) {
	barData, pieData, err := ChartData()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	result.Success(c, gin.H{
		"bar_chart": barData,
		"pie_chart": pieData,
	})
}

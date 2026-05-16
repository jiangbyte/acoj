package v1

import (
	"hei-gin/core/auth/middleware"
	"hei-gin/core/result"
	clientsession "hei-gin/modules/client/session"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all client session routes.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/client/session/analysis
	r.GET("/api/v1/client/session/analysis",
		middleware.HeiCheckPermission([]string{"sys:session:page"}),
		analysisHandler,
	)

	// GET /api/v1/client/session/page
	r.GET("/api/v1/client/session/page",
		middleware.HeiCheckPermission([]string{"sys:session:page"}),
		pageHandler,
	)

	// POST /api/v1/client/session/exit
	r.POST("/api/v1/client/session/exit",
		middleware.HeiCheckPermission([]string{"sys:session:exit"}),
		exitHandler,
	)

	// GET /api/v1/client/session/tokens
	r.GET("/api/v1/client/session/tokens",
		middleware.HeiCheckPermission([]string{"sys:session:page"}),
		tokensHandler,
	)

	// POST /api/v1/client/session/exit-token
	r.POST("/api/v1/client/session/exit-token",
		middleware.HeiCheckPermission([]string{"sys:session:exit"}),
		exitTokenHandler,
	)

	// GET /api/v1/client/session/chart-data
	r.GET("/api/v1/client/session/chart-data",
		middleware.HeiCheckPermission([]string{"sys:session:page"}),
		chartDataHandler,
	)
}

// analysisHandler handles GET /api/v1/client/session/analysis
func analysisHandler(c *gin.Context) {
	data := clientsession.Analysis(c)
	c.JSON(200, result.Success(c, data))
}

// pageHandler handles GET /api/v1/client/session/page
func pageHandler(c *gin.Context) {
	var param clientsession.SessionPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := clientsession.Page(c, &param)
	c.JSON(200, data)
}

// exitHandler handles POST /api/v1/client/session/exit
func exitHandler(c *gin.Context) {
	var param clientsession.SessionExitParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	clientsession.Exit(c, param.UserID)
	c.JSON(200, result.Success(c, nil))
}

// tokensHandler handles GET /api/v1/client/session/tokens
func tokensHandler(c *gin.Context) {
	var param struct {
		UserID string `form:"user_id"`
	}
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := clientsession.TokenList(c, param.UserID)
	c.JSON(200, result.Success(c, data))
}

// exitTokenHandler handles POST /api/v1/client/session/exit-token
func exitTokenHandler(c *gin.Context) {
	var param clientsession.SessionExitTokenParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	clientsession.ExitToken(c, param.UserID, param.Token)
	c.JSON(200, result.Success(c, nil))
}

// chartDataHandler handles GET /api/v1/client/session/chart-data
func chartDataHandler(c *gin.Context) {
	data := clientsession.ChartData(c)
	c.JSON(200, result.Success(c, data))
}

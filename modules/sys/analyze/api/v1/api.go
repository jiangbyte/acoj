package v1

import (
	"hei-gin/core/result"
	analyze "hei-gin/modules/sys/analyze"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/analyze/dashboard — NO permission middleware
	r.GET("/api/v1/sys/analyze/dashboard", dashboard)
}

func dashboard(c *gin.Context) {
	data := analyze.Dashboard(c)
	c.JSON(200, result.Success(c, data))
}

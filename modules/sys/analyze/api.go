package analyze

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/analyze/dashboard",
		DashboardHandler,
	)
}

func DashboardHandler(c *gin.Context) {
	data, err := Dashboard()
	if err != nil {
		result.Failure(c, "获取统计数据失败", 500)
		return
	}
	result.Success(c, data)
}

package core

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/result"
)

func HealthCheck(c *gin.Context) {
	result.Success(c, map[string]string{
		"status": "ok",
	})
}

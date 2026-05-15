package sysauth

import (
	"github.com/gin-gonic/gin"

	"hei-gin/config"
	"hei-gin/core/result"
)

func SM2PublicKey(c *gin.Context) {
	result.Success(c, config.C.SM2.PublicKey)
}

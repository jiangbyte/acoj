package sm2_api

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/result"
	"hei-gin/core/utils"
)

// RegisterRoutes registers consumer SM2-related routes.
func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/public/c/sm2/public-key", GetPublicKey)
}

// GetPublicKey returns the SM2 public key for frontend encryption.
func GetPublicKey(c *gin.Context) {
	publicKey := utils.GetPublicKey()
	c.JSON(200, result.Success(c, publicKey))
}

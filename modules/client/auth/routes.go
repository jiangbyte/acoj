package clientauth

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/log"
	"hei-gin/core/norepeat"
)

func RegisterRoutes(r *gin.RouterGroup) {
	// Public routes
	r.GET("/api/v1/public/c/captcha", Captcha)
	r.GET("/api/v1/public/c/sm2/public-key", SM2PublicKey)
	r.POST("/api/v1/public/c/login", Login)
	r.POST("/api/v1/public/c/register",
		log.SysLog("注册"),
		norepeat.NoRepeat(5000),
		RegisterHandler,
	)

	// Consumer auth required
	r.POST("/api/v1/c/logout", Logout)
}

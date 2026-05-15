package sysauth

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/log"
	"hei-gin/core/norepeat"
)

func RegisterRoutes(r *gin.RouterGroup) {
	// Public routes (no auth required — caught by Auth middleware)
	r.GET("/api/v1/public/b/captcha", Captcha)
	r.GET("/api/v1/public/b/sm2/public-key", SM2PublicKey)
	r.POST("/api/v1/public/b/login", Login)
	r.POST("/api/v1/public/b/register",
		log.SysLog("注册"),
		norepeat.NoRepeat(5000),
		Register,
	)

	// Business auth required
	r.POST("/api/v1/b/logout", Logout)
}

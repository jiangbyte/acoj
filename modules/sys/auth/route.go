package auth

import (
	"github.com/gin-gonic/gin"

	captcha_api "hei-gin/modules/sys/auth/captcha/api/v1"
	sm2_api "hei-gin/modules/sys/auth/sm2/api/v1"
	username_api "hei-gin/modules/sys/auth/username/api/v1"
)

// RegisterRoutes registers all auth sub-module routes.
func RegisterRoutes(r *gin.Engine) {
	captcha_api.RegisterRoutes(r)
	sm2_api.RegisterRoutes(r)
	username_api.RegisterRoutes(r)
}

package captcha_api

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/captcha"
	"hei-gin/core/result"
)

// RegisterRoutes registers consumer captcha-related routes.
func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/public/c/captcha", GetCaptcha)
}

// GetCaptcha generates a captcha image and returns it as a base64-encoded string.
func GetCaptcha(c *gin.Context) {
	captchaResult, err := captcha.CCaptcha.GetCaptcha()
	if err != nil {
		c.JSON(200, result.Failure(c, "验证码生成失败", 500, nil))
		return
	}
	c.JSON(200, result.Success(c, captchaResult))
}

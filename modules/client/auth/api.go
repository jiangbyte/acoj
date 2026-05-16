package auth

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	authmw "hei-gin/core/auth/middleware"
	"hei-gin/core/captcha"
	"hei-gin/core/log"
	"hei-gin/core/result"
	"hei-gin/core/utils"
)

// RegisterRoutes registers all client auth routes.
func RegisterRoutes(r *gin.RouterGroup) {
	// Captcha
	r.GET("/api/v1/public/c/captcha", GetCaptcha)
	// SM2 public key
	r.GET("/api/v1/public/c/sm2/public-key", GetSM2PublicKey)
	// Login
	r.POST("/api/v1/public/c/login", Login)
	// Register
	r.POST("/api/v1/public/c/register", log.SysLog("注册"), authmw.NoRepeat(5000), Register)
	// Logout
	r.POST("/api/v1/c/logout", auth.CheckLogin(), log.SysLog("登出"), Logout)
}

// GetCaptcha returns a C-end CAPTCHA image.
func GetCaptcha(c *gin.Context) {
	captchaSvc := captcha.NewConsumerCaptcha()
	captchaResult, err := captchaSvc.Generate(false)
	if err != nil {
		result.Failure(c, "验证码生成失败", 500)
		return
	}
	result.Success(c, captchaResult)
}

// GetSM2PublicKey returns the SM2 public key for C-end.
func GetSM2PublicKey(c *gin.Context) {
	result.Success(c, utils.GetPublicKey())
}

// Login handles C-end username/password login.
func Login(c *gin.Context) {
	var param UsernameLoginParam
	if err := c.ShouldBind(&param); err != nil {
		result.ValidationError(c, err)
		return
	}
	data, err := doLogin(c, &param)
	if err != nil {
		panic(err)
	}
	result.Success(c, data)
}

// Register handles C-end user registration.
func Register(c *gin.Context) {
	var param UsernameRegisterParam
	if err := c.ShouldBind(&param); err != nil {
		result.ValidationError(c, err)
		return
	}
	data, err := doRegister(c, &param)
	if err != nil {
		panic(err)
	}
	result.Success(c, data)
}

// Logout handles C-end user logout.
func Logout(c *gin.Context) {
	auth.ClientAuthTool.Logout(c)
	result.Success(c, nil)
}

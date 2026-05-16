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

// RegisterRoutes registers all sys auth routes.
func RegisterRoutes(r *gin.RouterGroup) {
	// Captcha
	r.GET("/api/v1/public/b/captcha", GetCaptcha)
	// SM2 public key
	r.GET("/api/v1/public/b/sm2/public-key", GetSM2PublicKey)
	// Login
	r.POST("/api/v1/public/b/login", Login)
	// Register
	r.POST("/api/v1/public/b/register", log.SysLog("注册"), authmw.NoRepeat(5000), Register)
	// Logout
	r.POST("/api/v1/b/logout", auth.CheckLogin(), log.SysLog("登出"), Logout)
}

// GetCaptcha returns a B-end CAPTCHA image.
func GetCaptcha(c *gin.Context) {
	captchaSvc := captcha.NewBusinessCaptcha()
	captchaResult, err := captchaSvc.Generate(false)
	if err != nil {
		result.Failure(c, "验证码生成失败", 500)
		return
	}
	result.Success(c, captchaResult)
}

// GetSM2PublicKey returns the SM2 public key for B-end.
func GetSM2PublicKey(c *gin.Context) {
	result.Success(c, utils.GetPublicKey())
}

// Login handles B-end username/password login.
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

// Register handles B-end user registration.
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

// Logout handles B-end user logout.
func Logout(c *gin.Context) {
	auth.AuthTool.Logout(c)
	result.Success(c, nil)
}

package auth

import (
	"context"
	"log"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/captcha"
	"hei-gin/core/db"
	bizerr "hei-gin/core/exception"
	"hei-gin/core/utils"
	"hei-gin/ent/gen"
	"hei-gin/ent/gen/sysuser"
)

func doLogin(c *gin.Context, param *UsernameLoginParam) (*UsernameLoginResult, error) {
	// Verify captcha if provided
	if param.CaptchaID != "" && param.CaptchaCode != "" {
		captchaSvc := captcha.NewBusinessCaptcha()
		if err := captchaSvc.Verify(param.CaptchaID, param.CaptchaCode); err != nil {
			return nil, bizerr.NewBusinessError(err.Error())
		}
	}

	ctx := context.Background()

	// Find user by username
	user, err := db.Client.SysUser.Query().Where(sysuser.UsernameEQ(param.Username)).Only(ctx)
	if err != nil {
		return nil, bizerr.NewBusinessError("用户名或密码错误")
	}

	// Check user status
	if user.Status == "LOCKED" {
		return nil, bizerr.NewBusinessError("账户已锁定，请联系管理员")
	}
	if user.Status == "INACTIVE" {
		return nil, bizerr.NewBusinessError("账户未激活，请联系管理员")
	}

	// Decrypt and verify password
	decryptedPwd, err := utils.SM2Decrypt(param.Password)
	if err != nil {
		return nil, bizerr.NewBusinessError("密码解析失败")
	}
	if user.Password == nil || !utils.BcryptVerify(decryptedPwd, *user.Password) {
		return nil, bizerr.NewBusinessError("用户名或密码错误")
	}

	// Login via auth tool
	ip := utils.GetClientIP(c)
	extra := map[string]interface{}{
		"username":    safeStr(user.Username),
		"nickname":    safeStr(user.Nickname),
		"status":      user.Status,
		"device_type": "",
		"device_id":   "",
	}
	tokenStr, err := auth.AuthTool.Login(c, user.ID, extra)
	if err != nil {
		log.Printf("[AuthLogin] Login failed: %v", err)
		return nil, bizerr.NewBusinessError("登录失败")
	}

	// Update login metadata
	now := time.Now()
	loginCount := user.LoginCount + 1
	err = db.Client.SysUser.UpdateOneID(user.ID).
		SetLastLoginAt(now).
		SetLastLoginIP(ip).
		SetLoginCount(loginCount).
		SetUpdatedAt(now).
		Exec(ctx)
	if err != nil {
		log.Printf("[AuthLogin] Failed to update login meta: %v", err)
	}

	return &UsernameLoginResult{
		Token:     tokenStr,
		TokenName: auth.AuthTool.GetTokenName(),
		UserID:    user.ID,
	}, nil
}

func doRegister(c *gin.Context, param *UsernameRegisterParam) (*UsernameRegisterResult, error) {
	// Verify captcha if provided
	if param.CaptchaID != "" && param.CaptchaCode != "" {
		captchaSvc := captcha.NewBusinessCaptcha()
		if err := captchaSvc.Verify(param.CaptchaID, param.CaptchaCode); err != nil {
			return nil, bizerr.NewBusinessError(err.Error())
		}
	}

	ctx := context.Background()

	// Check if username already exists
	exists, err := db.Client.SysUser.Query().Where(sysuser.UsernameEQ(param.Username)).Exist(ctx)
	if err != nil {
		return nil, bizerr.NewBusinessError("注册失败")
	}
	if exists {
		return nil, bizerr.NewBusinessError("用户名已存在")
	}

	// Decrypt and hash password
	decryptedPwd, err := utils.SM2Decrypt(param.Password)
	if err != nil {
		return nil, bizerr.NewBusinessError("密码解析失败")
	}
	hashedPwd, err := utils.BcryptHash(decryptedPwd)
	if err != nil {
		return nil, bizerr.NewBusinessError("密码加密失败")
	}

	// Create user
	now := time.Now()
	id := utils.NextID()
	nickname := param.Nickname
	if nickname == "" {
		nickname = param.Username
	}
	_, err = db.Client.SysUser.Create().
		SetID(id).
		SetUsername(param.Username).
		SetPassword(hashedPwd).
		SetNickname(nickname).
		SetStatus("ACTIVE").
		SetLoginCount(0).
		SetCreatedAt(now).
		SetUpdatedAt(now).
		Save(ctx)
	if err != nil {
		log.Printf("[AuthRegister] Create user failed: %v", err)
		return nil, bizerr.NewBusinessError("注册失败")
	}

	// Auto login after register
	ip := utils.GetClientIP(c)
	extra := map[string]interface{}{
		"username":    param.Username,
		"nickname":    nickname,
		"status":      "ACTIVE",
		"device_type": "",
		"device_id":   "",
	}
	tokenStr, err := auth.AuthTool.Login(c, id, extra)
	if err != nil {
		log.Printf("[AuthRegister] Login after register failed: %v", err)
		return nil, bizerr.NewBusinessError("注册成功，但自动登录失败")
	}

	// Update login metadata
	now2 := time.Now()
	err = db.Client.SysUser.UpdateOneID(id).
		SetLastLoginAt(now2).
		SetLastLoginIP(ip).
		SetUpdatedAt(now2).
		Exec(ctx)
	if err != nil {
		log.Printf("[AuthRegister] Failed to update login meta: %v", err)
	}

	return &UsernameRegisterResult{
		Token:     tokenStr,
		TokenName: auth.AuthTool.GetTokenName(),
		UserID:    id,
	}, nil
}

func doLogout(c *gin.Context) *UsernameLogoutResult {
	auth.AuthTool.Logout(c)
	return &UsernameLogoutResult{Message: "登出成功"}
}

func safeStr(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

// Ensure *gen.SysUser is referenced to avoid import cycle issues
var _ = &gen.SysUser{}

package sysauth

import (
	"context"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	"hei-gin/ent/gen/clientuser"
	"hei-gin/ent/gen/sysuser"
)

func Login(c *gin.Context) {
	var req LoginReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	// Verify captcha (optional: only when captcha_id and captcha_code are provided)
	if req.CaptchaID != "" && req.CaptchaCode != "" {
		if !verifyCaptcha(req.CaptchaID, req.CaptchaCode) {
			result.Failure(c, "验证码错误", 400)
			return
		}
	} else {
		if !verifyCaptcha(req.CaptchaID, req.CaptchaCode) {
			result.Failure(c, "验证码错误", 400)
			return
		}
	}

	// SM2 decrypt password
	password, err := utils.SM2Decrypt(req.Password)
	if err != nil {
		result.Failure(c, "密码解密失败", 400)
		return
	}

	// Query user via ent
	ctx := context.Background()
	user, err := db.Client.SysUser.Query().
		Where(sysuser.UsernameEQ(req.Username)).
		Select(
			sysuser.FieldID,
			sysuser.FieldUsername,
			sysuser.FieldNickname,
			sysuser.FieldAvatar,
			sysuser.FieldEmail,
			sysuser.FieldPhone,
			sysuser.FieldPassword,
			sysuser.FieldStatus,
		).
		First(ctx)
	if err != nil {
		result.Failure(c, "用户名或密码错误", 400)
		return
	}

	// Check user status
	if user.Status == "INACTIVE" || user.Status == "LOCKED" {
		result.Failure(c, "该用户已被禁用", 400)
		return
	}

	// Verify bcrypt password
	if !utils.BcryptVerify(password, user.Password) {
		result.Failure(c, "用户名或密码错误", 400)
		return
	}

	// Build extra claims with device tracking
	extra := map[string]interface{}{
		"username":    user.Username,
		"nickname":    user.Nickname,
		"status":      user.Status,
		"device_type": c.GetHeader("User-Agent"),
		"device_id":   req.DeviceID,
	}

	// Create JWT token
	token, err := auth.AuthTool.Login(c, user.ID, extra)
	if err != nil {
		result.Failure(c, "登录失败", 500)
		return
	}

	// Update login info via ent
	now := time.Now()
	clientIP := utils.GetClientIP(c)
	db.Client.SysUser.UpdateOneID(user.ID).
		SetLastLoginAt(now).
		SetLastLoginIP(clientIP).
		SetLoginCount(user.LoginCount + 1).
		SetUpdatedAt(now).
		Exec(ctx)

	// Record auth log
	traceID := c.GetString("trace_id")
	userAgent := c.GetHeader("User-Agent")
	recordAuthLog(ctx, user.ID, "登录", "LOGIN", "SUCCESS", user.Username, traceID, clientIP, userAgent, c.Request.Method, c.Request.URL.String())

	result.Success(c, gin.H{"token": token})
}

func Register(c *gin.Context) {
	var req RegisterReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	// Verify captcha (optional: only when captcha_id and captcha_code are provided) (optional)
	if req.CaptchaID != "" && req.CaptchaCode != "" {
		if !verifyCaptcha(req.CaptchaID, req.CaptchaCode) {
			result.Failure(c, "验证码错误", 400)
			return
		}
	}

	// Check duplicate username via ent
	ctx := context.Background()
	count, err := db.Client.SysUser.Query().Where(sysuser.UsernameEQ(req.Username)).Count(ctx)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	if count > 0 {
		result.Failure(c, "用户名已存在", 400)
		return
	}

	// SM2 decrypt password
	password, err := utils.SM2Decrypt(req.Password)
	if err != nil {
		result.Failure(c, "密码解密失败", 400)
		return
	}

	// Hash password
	hashed, err := utils.BcryptHash(password)
	if err != nil {
		result.Failure(c, "密码加密失败", 500)
		return
	}

	id := utils.NextID()
	now := time.Now()
	_, err = db.Client.SysUser.Create().
		SetID(id).
		SetUsername(req.Username).
		SetPassword(hashed).
		SetNickname(req.Nickname).
		SetEmail(req.Email).
		SetPhone(req.Phone).
		SetStatus("ACTIVE").
		SetCreatedAt(now).
		SetCreatedBy(id).
		SetUpdatedAt(now).
		SetUpdatedBy(id).
		Save(ctx)
	if err != nil {
		result.Failure(c, "注册失败", 500)
		return
	}

	// Record auth log
	traceID := c.GetString("trace_id")
	clientIP := utils.GetClientIP(c)
	userAgent := c.GetHeader("User-Agent")
	recordAuthLog(ctx, id, "注册", "OPERATE", "SUCCESS", req.Username, traceID, clientIP, userAgent, c.Request.Method, c.Request.URL.String())

	result.Success(c, map[string]string{"id": id})
}

func Logout(c *gin.Context) {
	traceID := c.GetString("trace_id")
	loginID := auth.AuthTool.GetLoginID(c)
	clientIP := utils.GetClientIP(c)
	userAgent := c.GetHeader("User-Agent")

	auth.AuthTool.Logout(c)

	// Record auth log
	if loginID != "" {
		ctx := context.Background()
		recordAuthLog(ctx, loginID, "登出", "LOGOUT", "SUCCESS", loginID, traceID, clientIP, userAgent, c.Request.Method, c.Request.URL.String())
	}

	result.Success(c, gin.H{"message": "登出成功"})
}

// LoginFunc is a type alias for the login handler function reference
// to avoid import cycle when registering routes from router.go
type LoginFunc func(c *gin.Context)

// ClientAuthHandlers wraps the client auth login function
func ClientLogin(c *gin.Context) {
	var req LoginReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	ctx := context.Background()

	// Verify captcha (optional: only when captcha_id and captcha_code are provided)
	if req.CaptchaID != "" && req.CaptchaCode != "" {
		key := constants.CaptchaConsumerPrefix + req.CaptchaID
		expected, err := db.Redis.Get(ctx, key).Result()
		if err != nil || expected != req.CaptchaCode {
			result.Failure(c, "验证码错误", 400)
			return
		}
		db.Redis.Del(ctx, key)
	}

	// SM2 decrypt password
	password, err := utils.SM2Decrypt(req.Password)
	if err != nil {
		result.Failure(c, "密码解密失败", 400)
		return
	}

	// Query client user via ent
	cu, err := db.Client.ClientUser.Query().
		Where(clientuser.UsernameEQ(req.Username)).
		Select(
			clientuser.FieldID,
			clientuser.FieldUsername,
			clientuser.FieldNickname,
			clientuser.FieldAvatar,
			clientuser.FieldEmail,
			clientuser.FieldPhone,
			clientuser.FieldPassword,
			clientuser.FieldStatus,
			clientuser.FieldLoginCount,
		).
		First(ctx)
	if err != nil {
		result.Failure(c, "用户名或密码错误", 400)
		return
	}

	if cu.Status == "INACTIVE" || cu.Status == "LOCKED" {
		result.Failure(c, "该用户已被禁用", 400)
		return
	}

	if !utils.BcryptVerify(password, cu.Password) {
		result.Failure(c, "用户名或密码错误", 400)
		return
	}

	// Build extra claims with device tracking
	extra := map[string]interface{}{
		"username":    cu.Username,
		"nickname":    cu.Nickname,
		"status":      cu.Status,
		"device_type": c.GetHeader("User-Agent"),
		"device_id":   req.DeviceID,
	}

	token, err := auth.ClientAuthTool.Login(c, cu.ID, extra)
	if err != nil {
		result.Failure(c, "登录失败", 500)
		return
	}

	// Update login info via ent
	now := time.Now()
	db.Client.ClientUser.UpdateOneID(cu.ID).
		SetLastLoginAt(now).
		SetLastLoginIP(utils.GetClientIP(c)).
		SetLoginCount(cu.LoginCount + 1).
		SetUpdatedAt(now).
		Exec(ctx)

	result.Success(c, gin.H{"token": token})
}

func ClientLogout(c *gin.Context) {
	traceID := c.GetString("trace_id")
	loginID := auth.ClientAuthTool.GetLoginID(c)
	clientIP := utils.GetClientIP(c)
	userAgent := c.GetHeader("User-Agent")

	auth.ClientAuthTool.Logout(c)

	// Record auth log
	if loginID != "" {
		ctx := context.Background()
		recordAuthLog(ctx, loginID, "登出", "LOGOUT", "SUCCESS", loginID, traceID, clientIP, userAgent, c.Request.Method, c.Request.URL.String())
	}

	result.Success(c, gin.H{"message": "登出成功"})
}

// recordAuthLog persists an auth event (login/logout) to the sys_log table via ent.
func recordAuthLog(ctx context.Context, loginID, name, category, exeStatus, opUser, traceID, ip, userAgent, reqMethod, reqURL string) {
	now := time.Now()
	db.Client.SysLog.Create().
		SetID(utils.NextID()).
		SetCategory(category).
		SetName(name).
		SetExeStatus(exeStatus).
		SetTraceID(traceID).
		SetOpIP(ip).
		SetOpBrowser(userAgent).
		SetReqMethod(reqMethod).
		SetReqURL(reqURL).
		SetOpTime(int(now.UnixMilli())).
		SetOpUser(opUser).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		Save(ctx)
}

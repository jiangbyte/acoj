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
)

func Login(c *gin.Context) {
	var req LoginReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	// Verify captcha
	if !verifyCaptcha(req.CaptchaID, req.CaptchaCode) {
		result.Failure(c, "验证码错误", 400)
		return
	}

	// SM2 decrypt password
	password, err := utils.SM2Decrypt(req.Password)
	if err != nil {
		result.Failure(c, "密码解密失败", 400)
		return
	}

	// Query user
	ctx := context.Background()
	var userID, username, nickname, avatar, email, phone, hashedPwd, status string
	err = db.RawDB.QueryRowContext(ctx,
		"SELECT id, account, nickname, avatar, email, phone, password, status FROM sys_user WHERE account = ?",
		req.Username,
	).Scan(&userID, &username, &nickname, &avatar, &email, &phone, &hashedPwd, &status)
	if err != nil {
		result.Failure(c, "用户名或密码错误", 400)
		return
	}

	// Check user status
	if status == "INACTIVE" || status == "LOCKED" {
		result.Failure(c, "该用户已被禁用", 400)
		return
	}

	// Verify bcrypt password
	if !utils.BcryptVerify(password, hashedPwd) {
		result.Failure(c, "用户名或密码错误", 400)
		return
	}

	// Create JWT token
	token, err := auth.AuthTool.Login(c, userID, map[string]interface{}{
		"username": username,
		"nickname": nickname,
	})
	if err != nil {
		result.Failure(c, "登录失败", 500)
		return
	}

	// Update last login
	db.RawDB.ExecContext(ctx,
		"UPDATE sys_user SET updated_at = ? WHERE id = ?",
		time.Now(), userID,
	)

	result.Success(c, gin.H{"token": token})
}

func Register(c *gin.Context) {
	var req RegisterReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	// Verify captcha
	if !verifyCaptcha(req.CaptchaID, req.CaptchaCode) {
		result.Failure(c, "验证码错误", 400)
		return
	}

	// Check duplicate username
	ctx := context.Background()
	var count int
	db.RawDB.QueryRowContext(ctx, "SELECT COUNT(*) FROM sys_user WHERE account = ?", req.Username).Scan(&count)
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
	_, err = db.RawDB.ExecContext(ctx,
		`INSERT INTO sys_user (id, account, password, nickname, email, phone, status, created_at, created_by, updated_at, updated_by)
		 VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?, ?)`,
		id, req.Username, hashed, req.Nickname, req.Email, req.Phone, now, id, now, id,
	)
	if err != nil {
		result.Failure(c, "注册失败", 500)
		return
	}

	result.Success(c, map[string]string{"id": id})
}

func Logout(c *gin.Context) {
	auth.AuthTool.Logout(c)
	result.Success(c, gin.H{"message": "登出成功"})
}

// LoginFunc is a type alias for the login handler function reference
// to avoid import cycle when registering routes from router.go
type LoginFunc func(c *gin.Context)

// ClientAuthHandlers wraps the client auth login function
func ClientLogin(c *gin.Context) {
	var req LoginReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	// Verify captcha
	ctx := context.Background()
	key := constants.CaptchaConsumerPrefix + req.CaptchaID
	expected, err := db.Redis.Get(ctx, key).Result()
	if err != nil || expected != req.CaptchaCode {
		result.Failure(c, "验证码错误", 400)
		return
	}
	db.Redis.Del(ctx, key)

	// SM2 decrypt password
	password, err := utils.SM2Decrypt(req.Password)
	if err != nil {
		result.Failure(c, "密码解密失败", 400)
		return
	}

	// Query client user
	var userID, username, nickname, avatar, email, phone, hashedPwd, status string
	err = db.RawDB.QueryRowContext(ctx,
		"SELECT id, account, nickname, avatar, email, phone, password, status FROM client_user WHERE account = ?",
		req.Username,
	).Scan(&userID, &username, &nickname, &avatar, &email, &phone, &hashedPwd, &status)
	if err != nil {
		result.Failure(c, "用户名或密码错误", 400)
		return
	}

	if status == "INACTIVE" || status == "LOCKED" {
		result.Failure(c, "该用户已被禁用", 400)
		return
	}

	if !utils.BcryptVerify(password, hashedPwd) {
		result.Failure(c, "用户名或密码错误", 400)
		return
	}

	token, err := auth.ClientAuthTool.Login(c, userID, map[string]interface{}{
		"username": username,
		"nickname": nickname,
	})
	if err != nil {
		result.Failure(c, "登录失败", 500)
		return
	}

	db.RawDB.ExecContext(ctx,
		"UPDATE client_user SET updated_at = ? WHERE id = ?",
		time.Now(), userID,
	)

	result.Success(c, gin.H{"token": token})
}

func ClientLogout(c *gin.Context) {
	auth.ClientAuthTool.Logout(c)
	result.Success(c, gin.H{"message": "登出成功"})
}

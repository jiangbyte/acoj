package clientauth

import (
	"context"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	"hei-gin/ent/clientuser"
)

func Login(c *gin.Context) {
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

func Logout(c *gin.Context) {
	auth.ClientAuthTool.Logout(c)
	result.Success(c, gin.H{"message": "登出成功"})
}

func RegisterHandler(c *gin.Context) {
	var req RegisterReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	ctx := context.Background()

	// Verify captcha
	key := constants.CaptchaConsumerPrefix + req.CaptchaID
	expected, err := db.Redis.Get(ctx, key).Result()
	if err != nil || expected != req.CaptchaCode {
		result.Failure(c, "验证码错误", 400)
		return
	}
	db.Redis.Del(ctx, key)

	// Check if username already exists
	existing, err := db.Client.ClientUser.Query().Where(clientuser.UsernameEQ(req.Account)).Count(ctx)
	if err != nil {
		result.Failure(c, "注册失败", 500)
		return
	}
	if existing > 0 {
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
	hashedPwd, err := utils.BcryptHash(password)
	if err != nil {
		result.Failure(c, "注册失败", 500)
		return
	}

	// Create user
	now := time.Now()
	newID := utils.NextID()
	_, err = db.Client.ClientUser.Create().
		SetID(newID).
		SetUsername(req.Account).
		SetPassword(hashedPwd).
		SetNickname(req.Account).
		SetStatus("ACTIVE").
		SetCreatedAt(now).
		SetCreatedBy(newID).
		SetUpdatedAt(now).
		SetUpdatedBy(newID).
		Save(ctx)
	if err != nil {
		result.Failure(c, "注册失败", 500)
		return
	}

	result.Success(c, RegisterResp{Message: "注册成功"})
}

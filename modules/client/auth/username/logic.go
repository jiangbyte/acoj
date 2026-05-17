package username

import (
	"context"
	"time"

	"golang.org/x/crypto/bcrypt"

	"hei-gin/core/auth"
	"hei-gin/core/captcha"
	"hei-gin/core/db"
	"hei-gin/core/enums"
	"hei-gin/core/exception"
	"hei-gin/core/log"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	gen "hei-gin/ent/gen"
	"hei-gin/ent/gen/clientuser"

	"github.com/gin-gonic/gin"
)

// DoLogin handles the consumer username-password login flow.
func DoLogin(c *gin.Context) {
	var param UsernameLoginParam
	if err := c.ShouldBindJSON(&param); err != nil {
		panic(exception.NewBusinessError("请求参数错误", 400))
	}

	ctx := context.Background()

	// 1. Verify captcha
	if err := captcha.CCaptcha.CheckCaptcha(param.CaptchaID, param.CaptchaCode); err != nil {
		panic(exception.NewBusinessError(err.Error(), 400))
	}

	// 2. Query user by username
	user, err := db.Client.ClientUser.Query().
		Where(clientuser.UsernameEQ(param.Username)).
		Only(ctx)
	if err != nil {
		if gen.IsNotFound(err) {
			panic(exception.NewBusinessError("用户名或密码错误", 400))
		}
		panic(exception.NewBusinessError("系统异常", 500))
	}

	// 3. Check user status
	status := user.Status
	switch status {
	case string(enums.UserStatusLocked):
		panic(exception.NewBusinessError("账号已被锁定", 400))
	case string(enums.UserStatusInactive):
		panic(exception.NewBusinessError("账号已停用", 400))
	default:
		if status != string(enums.UserStatusActive) {
			panic(exception.NewBusinessError("账号状态异常", 400))
		}
	}

	// 4. SM2 decrypt password
	rawPwd := utils.Decrypt(param.Password)
	if rawPwd == "" {
		panic(exception.NewBusinessError("用户名或密码错误", 400))
	}

	// 5. Bcrypt verify
	if user.Password == nil {
		panic(exception.NewBusinessError("用户名或密码错误", 400))
	}
	if err := bcrypt.CompareHashAndPassword([]byte(*user.Password), []byte(rawPwd)); err != nil {
		panic(exception.NewBusinessError("用户名或密码错误", 400))
	}

	// 6. Build extra map
	ua := c.GetHeader("User-Agent")
	extra := map[string]any{
		"username":    safeStr(user.Username),
		"nickname":    safeStr(user.Nickname),
		"status":      status,
		"device_type": utils.GetBrowser(ua),
		"device_id":   param.DeviceID,
	}

	// 7. Call auth.Login to get token
	clientAuth := auth.NewHeiClientAuthTool()
	token, err := clientAuth.Login(c, user.ID, extra)
	if err != nil {
		panic(exception.NewBusinessError("登录失败", 500))
	}

	// 8. Update login info
	now := time.Now()
	ip := utils.GetClientIP(c)
	_ = db.Client.ClientUser.UpdateOneID(user.ID).
		SetLastLoginAt(now).
		SetLastLoginIP(ip).
		AddLoginCount(1).
		Exec(ctx)

	// 9. Record auth log
	username := safeStr(user.Username)
	log.RecordAuthLog(c, "登录", "LOGIN", "SUCCESS", "", username)

	// 10. Return token
	c.JSON(200, result.Success(c, UsernameLoginResult{
		Token: token,
	}))
}

// DoRegister handles the consumer username-password registration flow.
func DoRegister(c *gin.Context) {
	var param UsernameRegisterParam
	if err := c.ShouldBindJSON(&param); err != nil {
		panic(exception.NewBusinessError("请求参数错误", 400))
	}

	ctx := context.Background()

	// 1. Verify captcha
	if err := captcha.CCaptcha.CheckCaptcha(param.CaptchaID, param.CaptchaCode); err != nil {
		panic(exception.NewBusinessError(err.Error(), 400))
	}

	// 2. Check username uniqueness
	exists, err := db.Client.ClientUser.Query().
		Where(clientuser.UsernameEQ(param.Username)).
		Exist(ctx)
	if err != nil {
		panic(exception.NewBusinessError("系统异常", 500))
	}
	if exists {
		panic(exception.NewBusinessError("用户名已存在", 400))
	}

	// 3. SM2 decrypt password
	rawPwd := utils.Decrypt(param.Password)
	if rawPwd == "" {
		panic(exception.NewBusinessError("密码解密失败", 400))
	}

	// 4. Hash with bcrypt
	hashedPwd, err := bcrypt.GenerateFromPassword([]byte(rawPwd), bcrypt.DefaultCost)
	if err != nil {
		panic(exception.NewBusinessError("密码加密失败", 500))
	}

	// 5. Create user
	userID := utils.GenerateID()
	now := time.Now()
	hashedPwdStr := string(hashedPwd)

	_, err = db.Client.ClientUser.Create().
		SetID(userID).
		SetUsername(param.Username).
		SetPassword(hashedPwdStr).
		SetNickname(param.Username).
		SetStatus(string(enums.UserStatusActive)).
		SetCreatedAt(now).
		SetCreatedBy(userID).
		Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("注册失败", 500))
	}

	// 6. Record auth log
	log.RecordAuthLog(c, "注册", "REGISTER", "SUCCESS", "", param.Username)

	// 7. Return success
	c.JSON(200, result.Success(c, UsernameRegisterResult{
		Message: "注册成功",
	}))
}

// DoLogout handles the consumer logout flow.
func DoLogout(c *gin.Context) {
	ctx := context.Background()

	// 1. Get userID from current login
	clientAuth := auth.NewHeiClientAuthTool()
	userID := clientAuth.GetLoginIDDefaultNull(c)

	// 2. If logged in, query username and record auth log
	if userID != "" {
		user, err := db.Client.ClientUser.Query().
			Where(clientuser.IDEQ(userID)).
			Only(ctx)
		if err == nil && user != nil {
			username := safeStr(user.Username)
			log.RecordAuthLog(c, "登出", "LOGOUT", "SUCCESS", "", username)
		}
	}

	// 3. Call auth.Logout to clear Redis tokens
	clientAuth.Logout(c)

	// 4. Return success
	c.JSON(200, result.Success(c, UsernameLogoutResult{
		Message: "登出成功",
	}))
}

// safeStr safely dereferences a string pointer, returning empty string for nil.
func safeStr(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

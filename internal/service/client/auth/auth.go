package auth

import (
	"context"
	"errors"
	"strings"
	"time"

	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
	"golang.org/x/crypto/bcrypt"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/model/entity"
	"hei-goframe/internal/service/auth"
	"hei-goframe/internal/service/captcha"
	"hei-goframe/utility"
)

// Login authenticates a consumer user and returns a token.
func Login(ctx context.Context, username, password, captchaCode, captchaId, deviceId, ip, userAgent string) (token string, err error) {
	if captchaId != "" && captchaCode != "" {
		if !captcha.VerifyCaptcha(captcha.ConsumerCaptcha, captchaId, captchaCode) {
			return "", errors.New("验证码错误")
		}
	}

	user, err := getByAccount(ctx, username)
	if err != nil {
		return "", errors.New("用户名或密码错误")
	}
	if user == nil {
		return "", errors.New("用户名或密码错误")
	}

	// Check user status with specific error messages matching Python
	if user.Status == consts.UserStatusLocked {
		return "", errors.New("账号已被锁定")
	}
	if user.Status == consts.UserStatusInactive {
		return "", errors.New("账号已停用")
	}
	if user.Status != consts.UserStatusActive {
		return "", errors.New("账号状态异常")
	}

	decryptedPwd, err := utility.SM2Decrypt(password)
	if err != nil {
		return "", errors.New("解密失败")
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(decryptedPwd)); err != nil {
		return "", errors.New("用户名或密码错误")
	}

	// Derive device_type from User-Agent
	deviceType := getBrowserName(userAgent)

	extra := map[string]interface{}{
		"account":     user.Account,
		"nickname":    user.Nickname,
		"status":      user.Status,
		"device_id":   deviceId,
		"user_agent":  userAgent,
		"device_type": deviceType,
	}

	token, err = auth.ConsumerAuth.Login(ctx, user.Id, extra)
	if err != nil {
		return "", errors.New("登录失败")
	}

	recordLogin(ctx, user.Id, ip)

	// Record auth login log
	recordAuthLog(ctx, "登录", "LOGIN", user.Account)

	return token, nil
}

// Register creates a new consumer user.
func Register(ctx context.Context, username, password, captchaCode, captchaId string) error {
	if captchaId != "" && captchaCode != "" {
		if !captcha.VerifyCaptcha(captcha.ConsumerCaptcha, captchaId, captchaCode) {
			return errors.New("验证码错误")
		}
	}

	existing, err := getByAccount(ctx, username)
	if err != nil {
		return err
	}
	if existing != nil {
		return errors.New("用户名已存在")
	}

	decryptedPwd, err := utility.SM2Decrypt(password)
	if err != nil {
		return errors.New("解密失败")
	}

	hashedPwd, err := bcrypt.GenerateFromPassword([]byte(decryptedPwd), bcrypt.DefaultCost)
	if err != nil {
		return errors.New("密码加密失败")
	}

	_, err = dao.ClientUser.Ctx().Ctx(ctx).Insert(map[string]interface{}{
		"id":         utility.GenerateID(),
		"account":    username,
		"password":   string(hashedPwd),
		"nickname":   username,
		"status":     consts.UserStatusActive,
		"created_by": auth.GetLoginIdFromCtx(ctx),
	})
	if err != nil {
		return errors.New("注册失败")
	}

	return nil
}

// Logout logs out the current consumer user by invalidating the token.
func Logout(ctx context.Context, tokenStr string) error {
	// Get login user info for audit logging
	loginId := auth.GetLoginIdFromCtx(ctx)
	if loginId != "" {
		row, err := dao.ClientUser.Ctx().Ctx(ctx).Fields("account").WherePri(loginId).One()
		if err == nil && row != nil {
			opUser := row["account"].String()
			recordAuthLog(ctx, "登出", "LOGOUT", opUser)
		}
	}

	return auth.ConsumerAuth.Logout(ctx, tokenStr)
}

func getByAccount(ctx context.Context, account string) (*entity.ClientUser, error) {
	user, err := dao.ClientUser.Ctx().Ctx(ctx).Where("account", account).One()
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, nil
	}
	var u entity.ClientUser
	if err := user.Struct(&u); err != nil {
		return nil, err
	}
	return &u, nil
}

func recordLogin(ctx context.Context, userId, ip string) {
	dao.ClientUser.Ctx().Ctx(ctx).WherePri(userId).Update(gdb.Map{
		"last_login_ip": ip,
		"last_login_at": gdb.Raw("NOW()"),
		"login_count":   gdb.Raw("login_count + 1"),
	})
}

// ValidateToken validates a token and returns the login ID.
func ValidateToken(ctx context.Context, tokenStr string) (string, error) {
	return auth.ConsumerAuth.GetLoginId(ctx, tokenStr)
}

// recordAuthLog inserts an authentication log entry into sys_log.
func recordAuthLog(ctx context.Context, name, category, opUser string) {
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return
	}
	ip := r.GetClientIp()
	userAgent := r.Header.Get("User-Agent")
	now := time.Now().Format("2006-01-02 15:04:05")

	loginId := auth.GetLoginIdFromCtx(ctx)

	_, err := dao.SysLog.Ctx().Ctx(ctx).Insert(g.Map{
		"id":         utility.GenerateID(),
		"category":   category,
		"name":       name,
		"exe_status": "SUCCESS",
		"op_ip":      ip,
		"op_browser": getBrowserName(userAgent),
		"op_os":      getOsName(userAgent),
		"op_time":    now,
		"op_user":    opUser,
		"created_by": loginId,
		"trace_id":   utility.GetTraceID(ctx),
	})
	if err != nil {
		g.Log().Warning(ctx, "Failed to record auth log:", err)
	}
}

// getBrowserName extracts browser name from User-Agent string.
func getBrowserName(userAgent string) string {
	if userAgent == "" {
		return "-"
	}
	ua := strings.ToLower(userAgent)
	switch {
	case strings.Contains(ua, "edg"):
		return "Edge"
	case strings.Contains(ua, "chrome"):
		return "Chrome"
	case strings.Contains(ua, "firefox"):
		return "Firefox"
	case strings.Contains(ua, "safari"):
		return "Safari"
	case strings.Contains(ua, "opr") || strings.Contains(ua, "opera"):
		return "Opera"
	default:
		return "-"
	}
}

// getOsName extracts OS name from User-Agent string.
func getOsName(userAgent string) string {
	if userAgent == "" {
		return "-"
	}
	ua := strings.ToLower(userAgent)
	switch {
	case strings.Contains(ua, "windows"):
		return "Windows"
	case strings.Contains(ua, "macintosh") || strings.Contains(ua, "mac os"):
		return "macOS"
	case strings.Contains(ua, "iphone") || strings.Contains(ua, "ipad"):
		return "iOS"
	case strings.Contains(ua, "android"):
		return "Android"
	case strings.Contains(ua, "linux"):
		return "Linux"
	default:
		return "-"
	}
}

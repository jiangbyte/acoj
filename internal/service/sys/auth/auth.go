package auth

import (
	"context"
	"errors"

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

// Login authenticates a business user and returns a token.
func Login(ctx context.Context, username, password, captchaCode, captchaId, deviceId, ip, userAgent string) (token string, err error) {
	if captchaId != "" && captchaCode != "" {
		if !captcha.VerifyCaptcha(captcha.BusinessCaptcha, captchaId, captchaCode) {
			return "", errors.New("验证码错误")
		}
	}

	user, err := getByAccount(ctx, username)
	if err != nil {
		return "", errors.New("用户不存在")
	}
	if user == nil {
		return "", errors.New("用户不存在")
	}

	if user.Status != consts.UserStatusActive {
		return "", errors.New("账号已被禁用或锁定")
	}

	decryptedPwd, err := utility.SM2Decrypt(password)
	if err != nil {
		return "", errors.New("解密失败")
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(decryptedPwd)); err != nil {
		return "", errors.New("密码错误")
	}

	extra := g.Map{
		"account":    user.Account,
		"nickname":   user.Nickname,
		"status":     user.Status,
		"device_id":  deviceId,
		"user_agent": userAgent,
	}

	token, err = auth.BusinessAuth.Login(ctx, user.Id, extra)
	if err != nil {
		return "", errors.New("登录失败")
	}

	recordLogin(ctx, user.Id, ip)

	return token, nil
}

// Register creates a new business user.
func Register(ctx context.Context, username, password, captchaCode, captchaId string) error {
	if captchaId != "" && captchaCode != "" {
		if !captcha.VerifyCaptcha(captcha.BusinessCaptcha, captchaId, captchaCode) {
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

	_, err = dao.SysUser.Ctx().Ctx(ctx).Insert(g.Map{
		"id":       utility.GenerateID(),
		"account":  username,
		"password": string(hashedPwd),
		"nickname": username,
		"status":   consts.UserStatusActive,
	})
	if err != nil {
		return errors.New("注册失败")
	}

	return nil
}

// Logout logs out the current business user by invalidating the token.
func Logout(ctx context.Context, tokenStr string) error {
	return auth.BusinessAuth.Logout(ctx, tokenStr)
}

func getByAccount(ctx context.Context, account string) (*entity.SysUser, error) {
	user, err := dao.SysUser.Ctx().Ctx(ctx).Where("account", account).One()
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, nil
	}
	var u entity.SysUser
	if err := user.Struct(&u); err != nil {
		return nil, err
	}
	return &u, nil
}

func recordLogin(ctx context.Context, userId, ip string) {
	dao.SysUser.Ctx().Ctx(ctx).WherePri(userId).Update(gdb.Map{
		"last_login_ip": ip,
		"last_login_at": gdb.Raw("NOW()"),
		"login_count":   gdb.Raw("login_count + 1"),
	})
}

// ValidateToken validates a token and returns the login ID.
func ValidateToken(ctx context.Context, tokenStr string) (string, error) {
	return auth.BusinessAuth.GetLoginId(ctx, tokenStr)
}

package user

import (
	"context"
	"errors"

	"github.com/gogf/gf/v2/frame/g"
	"golang.org/x/crypto/bcrypt"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/model/entity"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

// Page queries client users with pagination.
func Page(ctx context.Context, keyword, status string, current, size int) (*utility.PageRes, error) {
	m := dao.ClientUser.Ctx().Ctx(ctx)
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("account LIKE ? OR nickname LIKE ?", kw, kw)
	}
	if status != "" {
		m = m.Where("status", status)
	}
	m = m.OrderDesc("created_at")

	count, err := m.Count()
	if err != nil {
		return nil, err
	}

	var users []*entity.ClientUser
	if err := m.Page(current, size).Scan(&users); err != nil {
		return nil, err
	}

	voList := make([]g.Map, 0, len(users))
	for _, u := range users {
		voList = append(voList, g.Map{
			"id":            u.Id,
			"account":       u.Account,
			"nickname":      u.Nickname,
			"avatar":        u.Avatar,
			"motto":         u.Motto,
			"gender":        u.Gender,
			"email":         u.Email,
			"phone":         u.Phone,
			"github":        u.Github,
			"status":        u.Status,
			"last_login_at": u.LastLoginAt,
			"last_login_ip": u.LastLoginIp,
			"login_count":   u.LoginCount,
			"created_at":    u.CreatedAt,
			"created_by":    u.CreatedBy,
			"updated_at":    u.UpdatedAt,
			"updated_by":    u.UpdatedBy,
		})
	}

	return utility.NewPageRes(voList, count, current, size), nil
}

// Create inserts a new client user.
func Create(ctx context.Context, account, nickname, avatar, motto, gender, birthday, email, github, phone, status string) error {
	if account != "" {
		existing, _ := getByAccount(ctx, account)
		if existing != nil {
			return errors.New("账号已存在")
		}
	}
	if email != "" {
		existing, _ := getByEmail(ctx, email)
		if existing != nil {
			return errors.New("邮箱已存在")
		}
	}

	id := utility.GenerateID()
	loginId := getLoginId(ctx)
	_, err := dao.ClientUser.Ctx().Ctx(ctx).Insert(g.Map{
		"id":         id,
		"account":    account,
		"nickname":   nickname,
		"avatar":     avatar,
		"motto":      motto,
		"gender":     gender,
		"birthday":   birthday,
		"email":      email,
		"github":     github,
		"phone":      phone,
		"status":     ifEmpty(status, consts.UserStatusActive),
		"created_by": loginId,
	})
	return err
}

// Modify updates an existing client user.
func Modify(ctx context.Context, id, account, nickname, avatar, motto, gender, birthday, email, github, phone, status string) error {
	existing := findById(ctx, id)
	if existing == nil {
		return errors.New("数据不存在")
	}

	updateData := g.Map{}
	if account != "" {
		updateData["account"] = account
	}
	if nickname != "" {
		updateData["nickname"] = nickname
	}
	if avatar != "" {
		updateData["avatar"] = avatar
	}
	if motto != "" {
		updateData["motto"] = motto
	}
	if gender != "" {
		updateData["gender"] = gender
	}
	if birthday != "" {
		updateData["birthday"] = birthday
	}
	if email != "" {
		updateData["email"] = email
	}
	if github != "" {
		updateData["github"] = github
	}
	if phone != "" {
		updateData["phone"] = phone
	}
	if status != "" {
		updateData["status"] = status
	}

	if len(updateData) > 0 {
		loginId := getLoginId(ctx)
		updateData["updated_by"] = loginId
		_, err := dao.ClientUser.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

// Remove deletes client users by IDs.
func Remove(ctx context.Context, ids []string) error {
	_, err := dao.ClientUser.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

// Detail returns client user detail with enriched fields.
func Detail(ctx context.Context, id string) (g.Map, error) {
	u := findById(ctx, id)
	if u == nil {
		return nil, nil
	}
	vo := g.Map{
		"id":            u.Id,
		"account":       u.Account,
		"nickname":      u.Nickname,
		"avatar":        u.Avatar,
		"motto":         u.Motto,
		"gender":        u.Gender,
		"birthday":      u.Birthday,
		"email":         u.Email,
		"github":        u.Github,
		"phone":         u.Phone,
		"status":        u.Status,
		"last_login_at": u.LastLoginAt,
		"last_login_ip": u.LastLoginIp,
		"login_count":   u.LoginCount,
		"created_at":    u.CreatedAt,
		"created_by":    u.CreatedBy,
		"updated_at":    u.UpdatedAt,
		"updated_by":    u.UpdatedBy,
	}
	enrichNames(ctx, vo)
	return vo, nil
}

// GetCurrentUser returns the current client user info.
func GetCurrentUser(ctx context.Context, loginId string) (g.Map, error) {
	u := findById(ctx, loginId)
	if u == nil {
		return nil, nil
	}

	positionName := ""
	if u.PositionId != "" {
		row, _ := dao.SysPosition.Ctx().Ctx(ctx).WherePri(u.PositionId).Fields("name").One()
		if row != nil {
			positionName = row["name"].String()
		}
	}

	return g.Map{
		"id":            u.Id,
		"account":       u.Account,
		"nickname":      u.Nickname,
		"avatar":        u.Avatar,
		"motto":         u.Motto,
		"gender":        u.Gender,
		"birthday":      u.Birthday,
		"email":         u.Email,
		"github":        u.Github,
		"phone":         u.Phone,
		"status":        u.Status,
		"org_names":     resolveNamePath(ctx, "sys_org", u.OrgId),
		"group_names":   resolveNamePath(ctx, "sys_group", u.GroupId),
		"position_name": positionName,
		"last_login_at": u.LastLoginAt,
		"last_login_ip": u.LastLoginIp,
		"login_count":   u.LoginCount,
	}, nil
}

// UpdateProfile updates the current client user's profile.
func UpdateProfile(ctx context.Context, loginId, account, nickname, motto, gender, birthday, email, github string) error {
	u := findById(ctx, loginId)
	if u == nil {
		return errors.New("用户不存在")
	}

	updateData := g.Map{}
	if account != "" && account != u.Account {
		existing, _ := getByAccount(ctx, account)
		if existing != nil {
			return errors.New("账号已存在")
		}
		updateData["account"] = account
	}
	if nickname != "" {
		updateData["nickname"] = nickname
	}
	if motto != "" {
		updateData["motto"] = motto
	}
	if gender != "" {
		updateData["gender"] = gender
	}
	if birthday != "" {
		updateData["birthday"] = birthday
	}
	if email != "" {
		updateData["email"] = email
	}
	if github != "" {
		updateData["github"] = github
	}

	if len(updateData) > 0 {
		_, err := dao.ClientUser.Ctx().Ctx(ctx).WherePri(loginId).Update(updateData)
		return err
	}
	return nil
}

// UpdateAvatar updates the current client user's avatar.
func UpdateAvatar(ctx context.Context, loginId, avatar string) error {
	_, err := dao.ClientUser.Ctx().Ctx(ctx).WherePri(loginId).Update(g.Map{"avatar": avatar})
	return err
}

// UpdatePassword changes the current client user's password using bcrypt.
func UpdatePassword(ctx context.Context, loginId, currentPassword, newPassword string) error {
	u := findById(ctx, loginId)
	if u == nil {
		return errors.New("用户不存在")
	}
	if u.Password == "" {
		return errors.New("未设置密码，无法修改")
	}

	decryptedCurrent, err := utility.SM2Decrypt(currentPassword)
	if err != nil {
		return errors.New("解密失败")
	}
	if err := bcrypt.CompareHashAndPassword([]byte(u.Password), []byte(decryptedCurrent)); err != nil {
		return errors.New("当前密码不正确")
	}

	decryptedNew, err := utility.SM2Decrypt(newPassword)
	if err != nil {
		return errors.New("解密失败")
	}
	hashed, err := bcrypt.GenerateFromPassword([]byte(decryptedNew), bcrypt.DefaultCost)
	if err != nil {
		return errors.New("密码加密失败")
	}

	_, err = dao.ClientUser.Ctx().Ctx(ctx).WherePri(loginId).Update(g.Map{"password": string(hashed)})
	return err
}

// --- Internal helpers ---

func findById(ctx context.Context, id string) *entity.ClientUser {
	row, err := dao.ClientUser.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil
	}
	var u entity.ClientUser
	row.Struct(&u)
	return &u
}

func getByAccount(ctx context.Context, account string) (*entity.ClientUser, error) {
	row, err := dao.ClientUser.Ctx().Ctx(ctx).Where("account", account).One()
	if err != nil {
		return nil, err
	}
	if row == nil {
		return nil, nil
	}
	var u entity.ClientUser
	row.Struct(&u)
	return &u, nil
}

func getByEmail(ctx context.Context, email string) (*entity.ClientUser, error) {
	row, err := dao.ClientUser.Ctx().Ctx(ctx).Where("email", email).One()
	if err != nil {
		return nil, err
	}
	if row == nil {
		return nil, nil
	}
	var u entity.ClientUser
	row.Struct(&u)
	return &u, nil
}

func enrichNames(ctx context.Context, vo g.Map) {
	orgId, _ := vo["org_id"].(string)
	vo["org_names"] = resolveNamePath(ctx, "sys_org", orgId)
	groupId, _ := vo["group_id"].(string)
	vo["group_names"] = resolveNamePath(ctx, "sys_group", groupId)
	positionId, _ := vo["position_id"].(string)
	if positionId != "" {
		row, _ := dao.SysPosition.Ctx().Ctx(ctx).WherePri(positionId).Fields("name").One()
		if row != nil {
			vo["position_name"] = row["name"].String()
		}
	}
}

func resolveNamePath(ctx context.Context, table string, id string) []string {
	if id == "" {
		return nil
	}
	var names []string
	currentId := id
	for currentId != "" {
		sql := "SELECT id, name, parent_id FROM " + table + " WHERE id = ?"
		row, err := g.DB().Ctx(ctx).GetOne(ctx, sql, currentId)
		if err != nil || row == nil {
			break
		}
		names = append([]string{row["name"].String()}, names...)
		currentId = row["parent_id"].String()
	}
	return names
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}

func ifEmpty(s, def string) string {
	if s == "" {
		return def
	}
	return s
}

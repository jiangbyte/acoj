package user

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
	"golang.org/x/crypto/bcrypt"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/model/entity"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func init() {
	auth.RegisterPermission("client:user:page", "client/user", "CLIENT", "C端用户查询")
	auth.RegisterPermission("client:user:create", "client/user", "CLIENT", "C端用户新增")
	auth.RegisterPermission("client:user:modify", "client/user", "CLIENT", "C端用户修改")
	auth.RegisterPermission("client:user:remove", "client/user", "CLIENT", "C端用户删除")
	auth.RegisterPermission("client:user:detail", "client/user", "CLIENT", "C端用户详情")
	auth.RegisterPermission("client:user:export", "client/user", "CLIENT", "C端用户导出")
	auth.RegisterPermission("client:user:template", "client/user", "CLIENT", "C端用户导入模板")
	auth.RegisterPermission("client:user:import", "client/user", "CLIENT", "C端用户导入")
}

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

	batchEnrichNames(ctx, voList)
	return utility.NewPageRes(voList, count, current, size), nil
}

// Create inserts a new client user with optional password.
func Create(ctx context.Context, account, nickname, avatar, motto, gender, birthday, email, github, phone, password, status string) error {
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
	data := g.Map{
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
	}

	if password != "" {
		decrypted, err := utility.SM2Decrypt(password)
		if err != nil {
			return errors.New("解密失败")
		}
		hashed, err := bcrypt.GenerateFromPassword([]byte(decrypted), bcrypt.DefaultCost)
		if err != nil {
			return errors.New("密码加密失败")
		}
		data["password"] = string(hashed)
	}

	_, err := dao.ClientUser.Ctx().Ctx(ctx).Insert(data)
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
	enrichCreatorUpdater(ctx, vo)
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

	createdName := ""
	if u.CreatedBy != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(u.CreatedBy).Fields("nickname").One()
		if row != nil {
			createdName = row["nickname"].String()
		}
	}
	updatedName := ""
	if u.UpdatedBy != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(u.UpdatedBy).Fields("nickname").One()
		if row != nil {
			updatedName = row["nickname"].String()
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
		"created_by":    u.CreatedBy,
		"created_name":  createdName,
		"updated_by":    u.UpdatedBy,
		"updated_name":  updatedName,
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

// Export exports client user data as an Excel file.
func Export(ctx context.Context, exportType string, selectedIds []string, current, size int) (*bytes.Buffer, error) {
	var records []g.Map

	switch exportType {
	case "current":
		pageSize := size
		if pageSize <= 0 {
			pageSize = 10
		}
		pageCurrent := current
		if pageCurrent <= 0 {
			pageCurrent = 1
		}
		m := dao.ClientUser.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.ClientUser.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default:
		m := dao.ClientUser.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}

	return utility.CreateExcelFromData(data, "C端用户数据")
}

// DownloadTemplate downloads an import template Excel file.
func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"account", "nickname", "avatar", "motto", "gender", "birthday", "email", "github", "phone", "org_id", "position_id", "group_id", "status"}
	return utility.CreateExcelTemplate(headers, "C端用户数据")
}

// Import imports client user data from an uploaded Excel file.
func Import(ctx context.Context, file ghttp.UploadFile) (g.Map, error) {
	f, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}
	defer f.Close()

	content, err := io.ReadAll(f)
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}

	rows, err := utility.ParseExcelFromBytes(content, true)
	if err != nil {
		return nil, err
	}

	if len(rows) == 0 {
		return nil, fmt.Errorf("导入数据不能为空")
	}

	imported := 0
	for _, row := range rows {
		id := utility.GenerateID()
		_, err := dao.ClientUser.Ctx().Ctx(ctx).Insert(g.Map{
			"id":          id,
			"account":     row["account"],
			"nickname":    row["nickname"],
			"avatar":      row["avatar"],
			"motto":       row["motto"],
			"gender":      row["gender"],
			"birthday":    row["birthday"],
			"email":       row["email"],
			"github":      row["github"],
			"phone":       row["phone"],
			"org_id":      row["org_id"],
			"position_id": row["position_id"],
			"group_id":    row["group_id"],
			"status":      row["status"],
			"created_by":  getLoginId(ctx),
		})
		if err == nil {
			imported++
		}
	}

	return g.Map{
		"total":   imported,
		"message": fmt.Sprintf("成功导入%d条数据", imported),
	}, nil
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

func cleanMapForExport(m g.Map) map[string]interface{} {
	result := make(map[string]interface{}, len(m))
	for k, v := range m {
		if v == nil {
			result[k] = ""
		} else {
			result[k] = v
		}
	}
	delete(result, "id")
	return result
}

func batchEnrichNames(ctx context.Context, list []g.Map) {
	for _, item := range list {
		enrichCreatorUpdater(ctx, item)
	}
}

func enrichCreatorUpdater(ctx context.Context, item g.Map) {
	if id, ok := item["created_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["created_name"] = row["nickname"].String()
		}
	}
	if id, ok := item["updated_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["updated_name"] = row["nickname"].String()
		}
	}
}

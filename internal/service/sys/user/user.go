package user

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"
	"strings"

	"github.com/gogf/gf/v2/database/gdb"
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
	auth.RegisterPermission("sys:user:page", "sys/user", "BACKEND", "用户查询")
	auth.RegisterPermission("sys:user:create", "sys/user", "BACKEND", "用户新增")
	auth.RegisterPermission("sys:user:modify", "sys/user", "BACKEND", "用户修改")
	auth.RegisterPermission("sys:user:remove", "sys/user", "BACKEND", "用户删除")
	auth.RegisterPermission("sys:user:detail", "sys/user", "BACKEND", "用户详情")
	auth.RegisterPermission("sys:user:export", "sys/user", "BACKEND", "用户导出")
	auth.RegisterPermission("sys:user:template", "sys/user", "BACKEND", "用户导入模板")
	auth.RegisterPermission("sys:user:import", "sys/user", "BACKEND", "用户导入")
	auth.RegisterPermission("sys:user:grant-role", "sys/user", "BACKEND", "用户分配角色")
	auth.RegisterPermission("sys:user:grant-permission", "sys/user", "BACKEND", "用户分配权限")
	auth.RegisterPermission("sys:user:own-permission-detail", "sys/user", "BACKEND", "用户权限详情")
	auth.RegisterPermission("sys:user:own-roles", "sys/user", "BACKEND", "用户角色ID列表")
}

// Page queries users with pagination.
func Page(ctx context.Context, keyword, status string, current, size int) (*utility.PageRes, error) {
	m := dao.SysUser.Ctx().Ctx(ctx)
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

	var users []*entity.SysUser
	if err := m.Page(current, size).Scan(&users); err != nil {
		return nil, err
	}

	var userIds []string
	for _, u := range users {
		userIds = append(userIds, u.Id)
	}

	roleMap := getRoleIdsMap(userIds)
	voList := make([]g.Map, 0, len(users))
	for _, u := range users {
		vo := g.Map{
			"id":            u.Id,
			"account":       u.Account,
			"nickname":      u.Nickname,
			"avatar":        u.Avatar,
			"motto":         u.Motto,
			"gender":        u.Gender,
			"email":         u.Email,
			"phone":         u.Phone,
			"org_id":        u.OrgId,
			"position_id":   u.PositionId,
			"group_id":      u.GroupId,
			"status":        u.Status,
			"last_login_at": u.LastLoginAt,
			"last_login_ip": u.LastLoginIp,
			"login_count":   u.LoginCount,
			"created_at":    u.CreatedAt,
			"created_by":    u.CreatedBy,
			"updated_at":    u.UpdatedAt,
			"updated_by":    u.UpdatedBy,
			"role_ids":      roleMap[u.Id],
		}
		voList = append(voList, vo)
	}

	batchEnrichNames(ctx, voList)
	return utility.NewPageRes(voList, count, current, size), nil
}

// Create inserts a new user.
func Create(ctx context.Context, req interface {
	GetAccount() string
	GetNickname() string
	GetEmail() string
	GetPassword() string
	GetRoleIds() []string
	GetGroupId() string
}) error {
	panic("implement")
}

func CreateUser(ctx context.Context, account, nickname, avatar, motto, gender, birthday, email, github, phone, orgId, positionId, groupId, status string, roleIds []string) error {
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
	_, err := dao.SysUser.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          id,
		"account":     account,
		"nickname":    nickname,
		"avatar":      avatar,
		"motto":       motto,
		"gender":      gender,
		"birthday":    birthday,
		"email":       email,
		"github":      github,
		"phone":       phone,
		"org_id":      orgId,
		"position_id": positionId,
		"group_id":    groupId,
		"status":      ifEmpty(status, consts.UserStatusActive),
		"created_by":  loginId,
	})
	if err != nil {
		return err
	}

	if len(roleIds) > 0 {
		grantRoles(ctx, id, roleIds, loginId, "", "")
	}
	if groupId != "" {
		setGroup(ctx, id, groupId)
	}
	return nil
}

// Modify updates an existing user.
func ModifyUser(ctx context.Context, id, account, nickname, avatar, motto, gender, birthday, email, github, phone, orgId, positionId, groupId, status string, roleIds []string) error {
	entity := findById(ctx, id)
	if entity == nil {
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
	if orgId != "" {
		updateData["org_id"] = orgId
	}
	if positionId != "" {
		updateData["position_id"] = positionId
	}
	if groupId != "" {
		updateData["group_id"] = groupId
	}
	if status != "" {
		updateData["status"] = status
	}

	if len(updateData) > 0 {
		loginId := getLoginId(ctx)
		updateData["updated_by"] = loginId
		dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
	}

	if roleIds != nil {
		grantRoles(ctx, id, roleIds, getLoginId(ctx), "", "")
	}
	return nil
}

// Remove deletes users by IDs.
func Remove(ctx context.Context, ids []string) error {
	dao.RelUserRole.Ctx().Ctx(ctx).Where("user_id in (?)", ids).Delete()
	dao.RelUserPermission.Ctx().Ctx(ctx).Where("user_id in (?)", ids).Delete()
	_, err := dao.SysUser.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

// Detail returns user detail with enriched fields.
func Detail(ctx context.Context, id string) (g.Map, error) {
	u := findById(ctx, id)
	if u == nil {
		return nil, nil
	}
	roleIds := getRoleIdsByUserId(id)
	vo := g.Map{
		"id":            u.Id,
		"account":       u.Account,
		"nickname":      u.Nickname,
		"avatar":        u.Avatar,
		"motto":         u.Motto,
		"gender":        u.Gender,
		"email":         u.Email,
		"github":        u.Github,
		"phone":         u.Phone,
		"org_id":        u.OrgId,
		"position_id":   u.PositionId,
		"group_id":      u.GroupId,
		"status":        u.Status,
		"last_login_at": u.LastLoginAt,
		"last_login_ip": u.LastLoginIp,
		"login_count":   u.LoginCount,
		"created_at":    u.CreatedAt,
		"created_by":    u.CreatedBy,
		"updated_at":    u.UpdatedAt,
		"updated_by":    u.UpdatedBy,
		"role_ids":      roleIds,
	}
	enrichNames(ctx, vo)
	enrichCreatorUpdater(ctx, vo)
	return vo, nil
}

// GrantRoles assigns roles to a user.
func GrantRoles(ctx context.Context, userId string, roleIds []string, scope, customScopeGroupIds string) error {
	loginId := getLoginId(ctx)
	grantRoles(ctx, userId, roleIds, loginId, scope, customScopeGroupIds)
	return nil
}

// GrantPermissions assigns direct permissions to a user.
func GrantPermissions(ctx context.Context, userId string, permissions []map[string]interface{}) error {
	loginId := getLoginId(ctx)

	dao.RelUserPermission.Ctx().Ctx(ctx).Where("user_id", userId).Delete()

	for _, p := range permissions {
		code, _ := p["permission_code"].(string)
		scope, _ := p["scope"].(string)
		if scope == "" {
			scope = consts.PermissionScopeAll
		}
		cgIds, _ := p["custom_scope_group_ids"].(string)
		coIds, _ := p["custom_scope_org_ids"].(string)
		dao.RelUserPermission.Ctx().Ctx(ctx).Insert(g.Map{
			"id":                     utility.GenerateID(),
			"user_id":                userId,
			"permission_code":        code,
			"scope":                  scope,
			"custom_scope_group_ids": cgIds,
			"custom_scope_org_ids":   coIds,
			"created_by":             loginId,
		})
	}
	return nil
}

// GetPermissionDetails returns direct permission assignments for a user.
func GetPermissionDetails(ctx context.Context, userId string) ([]g.Map, error) {
	rows, err := dao.RelUserPermission.Ctx().Ctx(ctx).
		Where("user_id", userId).
		Fields("permission_code", "scope", "custom_scope_group_ids", "custom_scope_org_ids").All()
	if err != nil {
		return nil, err
	}
	var result []g.Map
	for _, r := range rows {
		scope := r["scope"].String()
		if scope == "" {
			scope = consts.PermissionScopeAll
		}
		result = append(result, g.Map{
			"permission_code":        r["permission_code"].String(),
			"scope":                  scope,
			"custom_scope_group_ids": r["custom_scope_group_ids"].String(),
			"custom_scope_org_ids":   r["custom_scope_org_ids"].String(),
		})
	}
	return result, nil
}

// GetOwnRoles returns role IDs assigned to a user.
func GetOwnRoles(ctx context.Context, userId string) ([]string, error) {
	return getRoleIdsByUserId(userId), nil
}

// GetCurrentUser returns current user info.
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

// GetMenus returns menu tree for the current user.
func GetMenus(ctx context.Context, loginId string) ([]g.Map, error) {
	roleCodes := getRoleCodes(ctx, loginId)
	for _, code := range roleCodes {
		if code == consts.SuperAdminCode {
			return getAllMenus(ctx)
		}
	}

	roleIds := getRoleIdsAllSources(ctx, loginId)
	if len(roleIds) == 0 {
		return nil, nil
	}

	resourceIds := getRoleResourceIds(ctx, roleIds)
	if len(resourceIds) == 0 {
		return nil, nil
	}

	return getMenusByIds(ctx, resourceIds)
}

// GetPermissions returns sorted permission codes for the current user.
func GetPermissions(ctx context.Context, loginId string) ([]string, error) {
	// Check if user has SUPER_ADMIN role
	roleCodes := getRoleCodes(ctx, loginId)
	for _, code := range roleCodes {
		if code == consts.SuperAdminCode {
			return auth.PermTool.GetPermissionListByLoginId(ctx, loginId, consts.LoginTypeBusiness)
		}
	}

	roleIds := getRoleIdsAllSources(ctx, loginId)
	if len(roleIds) == 0 {
		return nil, nil
	}
	return getRolePermissionCodes(ctx, roleIds), nil
}

// UpdateProfile updates current user's profile.
func UpdateProfile(ctx context.Context, loginId, account, nickname, motto, gender, birthday, email, github, phone string) error {
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
	if phone != "" {
		updateData["phone"] = phone
	}

	if len(updateData) > 0 {
		_, err := dao.SysUser.Ctx().Ctx(ctx).WherePri(loginId).Update(updateData)
		return err
	}
	return nil
}

// UpdateAvatar updates current user's avatar.
func UpdateAvatar(ctx context.Context, loginId, avatar string) error {
	_, err := dao.SysUser.Ctx().Ctx(ctx).WherePri(loginId).Update(g.Map{"avatar": avatar})
	return err
}

// UpdatePassword changes current user's password.
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

	_, err = dao.SysUser.Ctx().Ctx(ctx).WherePri(loginId).Update(g.Map{"password": string(hashed)})
	return err
}

// Export exports user data as an Excel file.
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
		m := dao.SysUser.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysUser.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default:
		m := dao.SysUser.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}

	return utility.CreateExcelFromData(data, "用户数据")
}

// DownloadTemplate downloads an import template Excel file.
func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"account", "nickname", "avatar", "motto", "gender", "birthday", "email", "github", "phone", "org_id", "position_id", "group_id", "status"}
	return utility.CreateExcelTemplate(headers, "用户数据")
}

// Import imports user data from an uploaded Excel file.
func Import(ctx context.Context, file ghttp.UploadFile) (g.Map, error) {
	f, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}
	defer f.Close()

	if file.Size > 5*1024*1024 {
		return nil, fmt.Errorf("文件大小不能超过5MB")
	}
	if !strings.HasSuffix(strings.ToLower(file.Filename), ".xlsx") {
		return nil, fmt.Errorf("仅支持.xlsx格式文件")
	}

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
		_, err := dao.SysUser.Ctx().Ctx(ctx).Insert(g.Map{
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

func findById(ctx context.Context, id string) *entity.SysUser {
	row, err := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil
	}
	var u entity.SysUser
	row.Struct(&u)
	return &u
}

func getByAccount(ctx context.Context, account string) (*entity.SysUser, error) {
	row, err := dao.SysUser.Ctx().Ctx(ctx).Where("account", account).One()
	if err != nil {
		return nil, err
	}
	if row == nil {
		return nil, nil
	}
	var u entity.SysUser
	row.Struct(&u)
	return &u, nil
}

func getByEmail(ctx context.Context, email string) (*entity.SysUser, error) {
	row, err := dao.SysUser.Ctx().Ctx(ctx).Where("email", email).One()
	if err != nil {
		return nil, err
	}
	if row == nil {
		return nil, nil
	}
	var u entity.SysUser
	row.Struct(&u)
	return &u, nil
}

func getRoleIdsMap(userIds []string) map[string][]string {
	if len(userIds) == 0 {
		return nil
	}
	rows, _ := dao.RelUserRole.Ctx().Ctx(context.Background()).
		Where("user_id in (?)", userIds).
		Fields("user_id", "role_id").All()
	result := make(map[string][]string)
	for _, r := range rows {
		uid := r["user_id"].String()
		result[uid] = append(result[uid], r["role_id"].String())
	}
	return result
}

func getRoleIdsByUserId(userId string) []string {
	rows, _ := dao.RelUserRole.Ctx().Ctx(context.Background()).
		Where("user_id", userId).Fields("role_id").All()
	var ids []string
	for _, r := range rows {
		ids = append(ids, r["role_id"].String())
	}
	return ids
}

func grantRoles(ctx context.Context, userId string, roleIds []string, createdBy, scope, customScopeGroupIds string) {
	dao.RelUserRole.Ctx().Ctx(ctx).Where("user_id", userId).Delete()
	for _, rid := range roleIds {
		dao.RelUserRole.Ctx().Ctx(ctx).Insert(g.Map{
			"id":                     utility.GenerateID(),
			"user_id":                userId,
			"role_id":                rid,
			"scope":                  scope,
			"custom_scope_group_ids": customScopeGroupIds,
			"created_by":             createdBy,
		})
	}
}

func setGroup(ctx context.Context, userId, groupId string) {
	dao.SysUser.Ctx().Ctx(ctx).WherePri(userId).Update(g.Map{"group_id": groupId})
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}

func getRoleCodes(ctx context.Context, loginId string) []string {
	rows, _ := dao.SysRole.Ctx().Ctx(ctx).
		InnerJoin("rel_user_role", "sys_role.id = rel_user_role.role_id").
		Where("rel_user_role.user_id", loginId).
		Fields("sys_role.code").All()
	var codes []string
	for _, r := range rows {
		codes = append(codes, r["code"].String())
	}
	return codes
}

func getRoleIdsAllSources(ctx context.Context, loginId string) []string {
	set := make(map[string]bool)

	rows, _ := dao.RelUserRole.Ctx().Ctx(ctx).Where("user_id", loginId).Fields("role_id").All()
	for _, r := range rows {
		set[r["role_id"].String()] = true
	}

	user, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(loginId).Fields("org_id").One()
	if user != nil && user["org_id"].String() != "" {
		orgRows, _ := dao.RelOrgRole.Ctx().Ctx(ctx).Where("org_id", user["org_id"].String()).Fields("role_id").All()
		for _, r := range orgRows {
			set[r["role_id"].String()] = true
		}
	}

	var result []string
	for id := range set {
		result = append(result, id)
	}
	return result
}

func getRoleResourceIds(ctx context.Context, roleIds []string) []string {
	rows, _ := dao.RelRoleResource.Ctx().Ctx(ctx).
		Where("role_id in (?)", roleIds).
		Fields("resource_id").All()
	set := make(map[string]bool)
	for _, r := range rows {
		set[r["resource_id"].String()] = true
	}
	var ids []string
	for id := range set {
		ids = append(ids, id)
	}
	return ids
}

func getRolePermissionCodes(ctx context.Context, roleIds []string) []string {
	rows, _ := dao.RelRolePermission.Ctx().Ctx(ctx).
		Where("role_id in (?)", roleIds).
		Fields("permission_code").All()
	set := make(map[string]bool)
	for _, r := range rows {
		set[r["permission_code"].String()] = true
	}
	var codes []string
	for code := range set {
		codes = append(codes, code)
	}
	return codes
}

func getAllMenus(ctx context.Context) ([]g.Map, error) {
	rows, err := dao.SysResource.Ctx().Ctx(ctx).
		Where("category = ? AND type IN (?) AND status = ?",
			consts.ResourceCategoryBackendMenu,
			[]string{consts.ResourceTypeDirectory, consts.ResourceTypeMenu},
			consts.StatusEnabled).
		OrderAsc("sort_code").All()
	if err != nil {
		return nil, err
	}
	return buildMenuTree(rows), nil
}

func getMenusByIds(ctx context.Context, resourceIds []string) ([]g.Map, error) {
	rows, err := dao.SysResource.Ctx().Ctx(ctx).
		Where("id in (?) AND category = ? AND type IN (?) AND status = ?",
			resourceIds,
			consts.ResourceCategoryBackendMenu,
			[]string{consts.ResourceTypeDirectory, consts.ResourceTypeMenu},
			consts.StatusEnabled).
		OrderAsc("sort_code").All()
	if err != nil {
		return nil, err
	}
	return buildMenuTree(rows), nil
}

func buildMenuTree(rows []gdb.Record) []g.Map {
	resourceMap := make(map[string]g.Map)
	for _, r := range rows {
		resourceMap[r["id"].String()] = g.Map{
			"id":             r["id"].String(),
			"code":           r["code"].String(),
			"name":           r["name"].String(),
			"type":           r["type"].String(),
			"parent_id":      r["parent_id"].String(),
			"route_path":     r["route_path"].String(),
			"component_path": r["component_path"].String(),
			"redirect_path":  r["redirect_path"].String(),
			"icon":           r["icon"].String(),
			"is_visible":     r["is_visible"].String() == consts.Yes,
			"is_cache":       r["is_cache"].String() == consts.Yes,
			"is_affix":       r["is_affix"].String() == consts.Yes,
			"is_breadcrumb":  r["is_breadcrumb"].String() == consts.Yes,
			"sort_code":      r["sort_code"].Int(),
			"children":       []g.Map{},
		}
	}
	var tree []g.Map
	for _, r := range rows {
		node := resourceMap[r["id"].String()]
		pid := r["parent_id"].String()
		if pid != "" && resourceMap[pid] != nil {
			children, _ := resourceMap[pid]["children"].([]g.Map)
			children = append(children, node)
			resourceMap[pid]["children"] = children
		} else {
			tree = append(tree, node)
		}
	}
	return tree
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

func ifEmpty(s, def string) string {
	if s == "" {
		return def
	}
	return s
}

// FindByAccount is used by the login module.
func FindByAccount(ctx context.Context, account string) (*entity.SysUser, error) {
	return getByAccount(ctx, account)
}

// ToLoginUserInfo converts a SysUser to login info map.
func ToLoginUserInfo(u *entity.SysUser) g.Map {
	if u == nil {
		return nil
	}
	return g.Map{
		"id":            u.Id,
		"account":       u.Account,
		"password":      u.Password,
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
	}
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
		orgId, _ := item["org_id"].(string)
		item["org_names"] = resolveNamePath(ctx, "sys_org", orgId)
		groupId, _ := item["group_id"].(string)
		item["group_names"] = resolveNamePath(ctx, "sys_group", groupId)
		positionId, _ := item["position_id"].(string)
		if positionId != "" {
			row, _ := dao.SysPosition.Ctx().Ctx(ctx).WherePri(positionId).Fields("name").One()
			if row != nil {
				item["position_name"] = row["name"].String()
			}
		}
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

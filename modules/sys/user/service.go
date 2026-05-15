package user

import (
	"context"
	"strings"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/sysuser"
)

type PageParam struct {
	Page    int    `form:"page" json:"page"`
	Size    int    `form:"size" json:"size"`
	Keyword string `form:"keyword" json:"keyword"`
	Status  string `form:"status" json:"status"`
	OrgID   string `form:"org_id" json:"org_id"`
}

type UserVO struct {
	ID           string `json:"id"`
	Username     string `json:"username"`
	Nickname     string `json:"nickname"`
	Avatar       string `json:"avatar"`
	Email        string `json:"email"`
	Phone        string `json:"phone"`
	Status       string `json:"status"`
	OrgID        string `json:"org_id"`
	OrgName      string `json:"org_name"`
	GroupID      string `json:"group_id"`
	GroupName    string `json:"group_name"`
	PositionID   string `json:"position_id"`
	PositionName string `json:"position_name"`
	Gender       string `json:"gender"`
	Birthday     string `json:"birthday"`
	Description  string `json:"description"`
	SortCode     int    `json:"sort_code"`
	CreatedAt    string `json:"created_at"`
	CreatedBy    string `json:"created_by"`
	UpdatedAt    string `json:"updated_at"`
	UpdatedBy    string `json:"updated_by"`
}

type UserCreateReq struct {
	Username    string `json:"username" binding:"required"`
	Password    string `json:"password" binding:"required"`
	Nickname    string `json:"nickname"`
	Avatar      string `json:"avatar"`
	Email       string `json:"email"`
	Phone       string `json:"phone"`
	Status      string `json:"status"`
	OrgID       string `json:"org_id"`
	GroupID     string `json:"group_id"`
	PositionID  string `json:"position_id"`
	Gender      string `json:"gender"`
	Birthday    string `json:"birthday"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
}

type UserModifyReq struct {
	ID          string `json:"id" binding:"required"`
	Nickname    string `json:"nickname"`
	Avatar      string `json:"avatar"`
	Email       string `json:"email"`
	Phone       string `json:"phone"`
	Status      string `json:"status"`
	OrgID       string `json:"org_id"`
	GroupID     string `json:"group_id"`
	PositionID  string `json:"position_id"`
	Gender      string `json:"gender"`
	Birthday    string `json:"birthday"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

type GrantRoleReq struct {
	UserID  string   `json:"user_id" binding:"required"`
	RoleIDs []string `json:"role_ids" binding:"required"`
}

type GrantPermissionReq struct {
	UserID          string   `json:"user_id" binding:"required"`
	PermissionCodes []string `json:"permission_codes" binding:"required"`
}

type UpdateProfileReq struct {
	Nickname    string `json:"nickname"`
	Email       string `json:"email"`
	Phone       string `json:"phone"`
	Description string `json:"description"`
}

type UpdateAvatarReq struct {
	Avatar string `json:"avatar" binding:"required"`
}

type UpdatePasswordReq struct {
	OldPassword string `json:"old_password" binding:"required"`
	NewPassword string `json:"new_password" binding:"required"`
}

type CurrentUserVO struct {
	ID           string `json:"id"`
	Username     string `json:"username"`
	Nickname     string `json:"nickname"`
	Avatar       string `json:"avatar"`
	Email        string `json:"email"`
	Phone        string `json:"phone"`
	Gender       string `json:"gender"`
	OrgID        string `json:"org_id"`
	OrgName      string `json:"org_name"`
	GroupID      string `json:"group_id"`
	GroupName    string `json:"group_name"`
	PositionID   string `json:"position_id"`
	PositionName string `json:"position_name"`
	Description  string `json:"description"`
}

type OwnUserVO struct {
	UserID   string `json:"user_id"`
	Username string `json:"username"`
}

var UserExportFieldNames = map[string]string{
	"username":   "用户名",
	"nickname":   "昵称",
	"email":      "电子邮箱",
	"phone":      "手机号码",
	"status":     "状态",
	"gender":     "性别",
	"sort_code":  "排序",
	"created_at": "创建时间",
}

var UserExportFields = []string{"username", "nickname", "email", "phone", "status", "gender", "sort_code", "created_at"}

func toVO(u *ent.SysUser) UserVO {
	vo := UserVO{
		ID:          u.ID,
		Username:    u.Username,
		Nickname:    u.Nickname,
		Avatar:      u.Avatar,
		Email:       u.Email,
		Phone:       u.Phone,
		Status:      u.Status,
		OrgID:       u.OrgID,
		GroupID:     u.GroupID,
		PositionID:  u.PositionID,
		Gender:      u.Gender,
		Description: u.Description,
		SortCode:    u.SortCode,
		CreatedAt:   u.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   u.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   u.CreatedBy,
		UpdatedBy:   u.UpdatedBy,
	}
	if !u.Birthday.IsZero() {
		vo.Birthday = u.Birthday.Format("2006-01-02")
	}
	// Resolve names
	vo.OrgName = resolveOrgName(u.OrgID)
	vo.GroupName = resolveGroupName(u.GroupID)
	vo.PositionName = resolvePositionName(u.PositionID)
	return vo
}

// toVOBatch converts a user to a UserVO using pre-resolved name maps (batch mode, no N+1).
func toVOBatch(u *ent.SysUser, orgNameMap, groupNameMap, positionNameMap map[string]string) UserVO {
	vo := UserVO{
		ID:           u.ID,
		Username:     u.Username,
		Nickname:     u.Nickname,
		Avatar:       u.Avatar,
		Email:        u.Email,
		Phone:        u.Phone,
		Status:       u.Status,
		OrgID:        u.OrgID,
		OrgName:      orgNameMap[u.OrgID],
		GroupID:      u.GroupID,
		GroupName:    groupNameMap[u.GroupID],
		PositionID:   u.PositionID,
		PositionName: positionNameMap[u.PositionID],
		Gender:       u.Gender,
		Description:  u.Description,
		SortCode:     u.SortCode,
		CreatedAt:    u.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:    u.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:    u.CreatedBy,
		UpdatedBy:    u.UpdatedBy,
	}
	if !u.Birthday.IsZero() {
		vo.Birthday = u.Birthday.Format("2006-01-02")
	}
	return vo
}

func resolveOrgName(orgID string) string {
	if orgID == "" {
		return ""
	}
	ctx := context.Background()
	var name string
	err := db.RawDB.QueryRowContext(ctx, "SELECT name FROM sys_org WHERE id = ?", orgID).Scan(&name)
	if err != nil {
		return ""
	}
	return name
}

func resolveGroupName(groupID string) string {
	if groupID == "" {
		return ""
	}
	ctx := context.Background()
	var name string
	err := db.RawDB.QueryRowContext(ctx, "SELECT name FROM sys_group WHERE id = ?", groupID).Scan(&name)
	if err != nil {
		return ""
	}
	return name
}

func resolvePositionName(positionID string) string {
	if positionID == "" {
		return ""
	}
	ctx := context.Background()
	var name string
	err := db.RawDB.QueryRowContext(ctx, "SELECT name FROM sys_position WHERE id = ?", positionID).Scan(&name)
	if err != nil {
		return ""
	}
	return name
}

// batchResolveNames issues 3 queries total (one per table) to resolve all org, group, and position names
// for a list of users, eliminating the N+1 problem.
func batchResolveNames(users []*ent.SysUser) (orgNameMap, groupNameMap, positionNameMap map[string]string) {
	ctx := context.Background()

	// Collect unique IDs
	orgIDs := make([]string, 0, len(users))
	groupIDs := make([]string, 0, len(users))
	positionIDs := make([]string, 0, len(users))
	orgSet := make(map[string]struct{})
	groupSet := make(map[string]struct{})
	posSet := make(map[string]struct{})

	for _, u := range users {
		if u.OrgID != "" {
			if _, ok := orgSet[u.OrgID]; !ok {
				orgSet[u.OrgID] = struct{}{}
				orgIDs = append(orgIDs, u.OrgID)
			}
		}
		if u.GroupID != "" {
			if _, ok := groupSet[u.GroupID]; !ok {
				groupSet[u.GroupID] = struct{}{}
				groupIDs = append(groupIDs, u.GroupID)
			}
		}
		if u.PositionID != "" {
			if _, ok := posSet[u.PositionID]; !ok {
				posSet[u.PositionID] = struct{}{}
				positionIDs = append(positionIDs, u.PositionID)
			}
		}
	}

	// Resolve org names
	orgNameMap = make(map[string]string, len(orgIDs))
	if len(orgIDs) > 0 {
		placeholders := make([]string, len(orgIDs))
		args := make([]interface{}, len(orgIDs))
		for i, id := range orgIDs {
			placeholders[i] = "?"
			args[i] = id
		}
		query := "SELECT id, name FROM sys_org WHERE id IN (" + strings.Join(placeholders, ",") + ")"
		rows, err := db.RawDB.QueryContext(ctx, query, args...)
		if err == nil {
			defer rows.Close()
			for rows.Next() {
				var id, name string
				if err := rows.Scan(&id, &name); err == nil {
					orgNameMap[id] = name
				}
			}
		}
	}

	// Resolve group names
	groupNameMap = make(map[string]string, len(groupIDs))
	if len(groupIDs) > 0 {
		placeholders := make([]string, len(groupIDs))
		args := make([]interface{}, len(groupIDs))
		for i, id := range groupIDs {
			placeholders[i] = "?"
			args[i] = id
		}
		query := "SELECT id, name FROM sys_group WHERE id IN (" + strings.Join(placeholders, ",") + ")"
		rows, err := db.RawDB.QueryContext(ctx, query, args...)
		if err == nil {
			defer rows.Close()
			for rows.Next() {
				var id, name string
				if err := rows.Scan(&id, &name); err == nil {
					groupNameMap[id] = name
				}
			}
		}
	}

	// Resolve position names
	positionNameMap = make(map[string]string, len(positionIDs))
	if len(positionIDs) > 0 {
		placeholders := make([]string, len(positionIDs))
		args := make([]interface{}, len(positionIDs))
		for i, id := range positionIDs {
			placeholders[i] = "?"
			args[i] = id
		}
		query := "SELECT id, name FROM sys_position WHERE id IN (" + strings.Join(placeholders, ",") + ")"
		rows, err := db.RawDB.QueryContext(ctx, query, args...)
		if err == nil {
			defer rows.Close()
			for rows.Next() {
				var id, name string
				if err := rows.Scan(&id, &name); err == nil {
					positionNameMap[id] = name
				}
			}
		}
	}

	return
}

func Page(page, size int, keyword, status, orgID string) (int, []UserVO, error) {
	ctx := context.Background()
	q := db.Client.SysUser.Query()

	if keyword != "" {
		q = q.Where(
			sysuser.Or(
				sysuser.UsernameContains(keyword),
				sysuser.NicknameContains(keyword),
				sysuser.PhoneContains(keyword),
			),
		)
	}
	if status != "" {
		q = q.Where(sysuser.StatusEQ(status))
	}
	if orgID != "" {
		q = q.Where(sysuser.OrgIDEQ(orgID))
	}

	total, err := q.Count(ctx)
	if err != nil {
		return 0, nil, err
	}

	if size <= 0 {
		size = 10
	}
	if page <= 0 {
		page = 1
	}

	items, err := q.
		Order(ent.Desc(sysuser.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	// Batch resolve names (3 queries total instead of 3*N)
	orgNameMap, groupNameMap, positionNameMap := batchResolveNames(items)

	// Convert to VOs using pre-resolved maps
	vos := make([]UserVO, len(items))
	for i, item := range items {
		vos[i] = toVOBatch(item, orgNameMap, groupNameMap, positionNameMap)
	}

	return total, vos, nil
}

func Create(req *UserCreateReq, loginID string) (*ent.SysUser, error) {
	ctx := context.Background()
	now := time.Now()

	hashedPwd, err := utils.BcryptHash(req.Password)
	if err != nil {
		return nil, err
	}

	q := db.Client.SysUser.Create().
		SetID(utils.NextID()).
		SetUsername(req.Username).
		SetPassword(hashedPwd).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)

	if req.Nickname != "" {
		q.SetNickname(req.Nickname)
	}
	if req.Avatar != "" {
		q.SetAvatar(req.Avatar)
	}
	if req.Email != "" {
		q.SetEmail(req.Email)
	}
	if req.Phone != "" {
		q.SetPhone(req.Phone)
	}
	if req.Status != "" {
		q.SetStatus(req.Status)
	} else {
		q.SetStatus("ACTIVE")
	}
	if req.OrgID != "" {
		q.SetOrgID(req.OrgID)
	}
	if req.GroupID != "" {
		q.SetGroupID(req.GroupID)
	}
	if req.PositionID != "" {
		q.SetPositionID(req.PositionID)
	}
	if req.Gender != "" {
		q.SetGender(req.Gender)
	} else {
		q.SetGender("UNKNOWN")
	}
	if req.Birthday != "" {
		birthday, err := time.Parse("2006-01-02", req.Birthday)
		if err == nil {
			q.SetBirthday(birthday)
		}
	}
	if req.Description != "" {
		q.SetDescription(req.Description)
	}
	if req.SortCode > 0 {
		q.SetSortCode(req.SortCode)
	}

	return q.Save(ctx)
}

func Modify(req *UserModifyReq, loginID string) (*ent.SysUser, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysUser.UpdateOneID(req.ID)

	if req.Nickname != "" {
		u.SetNickname(req.Nickname)
	}
	if req.Avatar != "" {
		u.SetAvatar(req.Avatar)
	}
	if req.Email != "" {
		u.SetEmail(req.Email)
	}
	if req.Phone != "" {
		u.SetPhone(req.Phone)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	if req.OrgID != "" {
		u.SetOrgID(req.OrgID)
	}
	if req.GroupID != "" {
		u.SetGroupID(req.GroupID)
	}
	if req.PositionID != "" {
		u.SetPositionID(req.PositionID)
	}
	if req.Gender != "" {
		u.SetGender(req.Gender)
	}
	if req.Birthday != "" {
		birthday, err := time.Parse("2006-01-02", req.Birthday)
		if err == nil {
			u.SetBirthday(birthday)
		}
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}
	if req.SortCode > 0 {
		u.SetSortCode(req.SortCode)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()
	tx, err := db.RawDB.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	// Cascade delete from rel tables
	for _, id := range ids {
		_, _ = tx.ExecContext(ctx, "DELETE FROM rel_user_role WHERE user_id = ?", id)
		_, _ = tx.ExecContext(ctx, "DELETE FROM rel_user_permission WHERE user_id = ?", id)
	}

	// Delete users via Ent
	_, err = db.Client.SysUser.Delete().Where(sysuser.IDIn(ids...)).Exec(ctx)
	if err != nil {
		return err
	}

	return tx.Commit()
}

func Detail(id string) (*ent.SysUser, error) {
	ctx := context.Background()
	return db.Client.SysUser.Get(ctx, id)
}

func QueryAll() ([]*ent.SysUser, error) {
	ctx := context.Background()
	return db.Client.SysUser.Query().Order(ent.Desc(sysuser.FieldCreatedAt)).All(ctx)
}

// OwnRoles returns role IDs assigned to a user.
func OwnRoles(userID string) ([]string, error) {
	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx, "SELECT role_id FROM rel_user_role WHERE user_id = ?", userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var roleIDs []string
	for rows.Next() {
		var roleID string
		if err := rows.Scan(&roleID); err != nil {
			return nil, err
		}
		roleIDs = append(roleIDs, roleID)
	}
	if roleIDs == nil {
		roleIDs = []string{}
	}
	return roleIDs, rows.Err()
}

// OwnPermissionDetail returns both P0 (direct) and P1 (via roles) permission codes.
func OwnPermissionDetail(userID string) ([]string, error) {
	ctx := context.Background()
	permSet := make(map[string]bool)

	// P0: direct user permissions
	p0Rows, err := db.RawDB.QueryContext(ctx, "SELECT permission_code FROM rel_user_permission WHERE user_id = ?", userID)
	if err != nil {
		return nil, err
	}
	defer p0Rows.Close()
	for p0Rows.Next() {
		var code string
		if err := p0Rows.Scan(&code); err != nil {
			return nil, err
		}
		permSet[code] = true
	}

	// P1: permissions via roles
	roleRows, err := db.RawDB.QueryContext(ctx, "SELECT role_id FROM rel_user_role WHERE user_id = ?", userID)
	if err != nil {
		return nil, err
	}
	defer roleRows.Close()

	var roleIDs []string
	for roleRows.Next() {
		var roleID string
		if err := roleRows.Scan(&roleID); err != nil {
			return nil, err
		}
		roleIDs = append(roleIDs, roleID)
	}

	for _, roleID := range roleIDs {
		rpRows, err := db.RawDB.QueryContext(ctx, "SELECT permission_code FROM rel_role_permission WHERE role_id = ?", roleID)
		if err != nil {
			continue
		}
		for rpRows.Next() {
			var code string
			if err := rpRows.Scan(&code); err != nil {
				rpRows.Close()
				continue
			}
			permSet[code] = true
		}
		rpRows.Close()
	}

	var result []string
	for code := range permSet {
		result = append(result, code)
	}
	if result == nil {
		result = []string{}
	}
	return result, nil
}

// GrantRole deletes old role assignments and inserts new ones.
func GrantRole(userID string, roleIDs []string) error {
	ctx := context.Background()
	tx, err := db.RawDB.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.ExecContext(ctx, "DELETE FROM rel_user_role WHERE user_id = ?", userID)
	if err != nil {
		return err
	}

	for _, roleID := range roleIDs {
		_, err = tx.ExecContext(ctx, "INSERT INTO rel_user_role (id, user_id, role_id) VALUES (?, ?, ?)", utils.NextID(), userID, roleID)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

// GrantPermission deletes old permission assignments and inserts new ones.
func GrantPermission(userID string, permissionCodes []string) error {
	ctx := context.Background()
	tx, err := db.RawDB.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.ExecContext(ctx, "DELETE FROM rel_user_permission WHERE user_id = ?", userID)
	if err != nil {
		return err
	}

	for _, code := range permissionCodes {
		_, err = tx.ExecContext(ctx, "INSERT INTO rel_user_permission (id, user_id, permission_code) VALUES (?, ?, ?)", utils.NextID(), userID, code)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

// Current returns a detailed current user VO with resolved names.
func Current(userID string) (*CurrentUserVO, error) {
	ctx := context.Background()
	u, err := db.Client.SysUser.Get(ctx, userID)
	if err != nil {
		return nil, err
	}

	vo := &CurrentUserVO{
		ID:          u.ID,
		Username:    u.Username,
		Nickname:    u.Nickname,
		Avatar:      u.Avatar,
		Email:       u.Email,
		Phone:       u.Phone,
		Gender:      u.Gender,
		OrgID:       u.OrgID,
		GroupID:     u.GroupID,
		PositionID:  u.PositionID,
		Description: u.Description,
	}
	vo.OrgName = resolveOrgName(u.OrgID)
	vo.GroupName = resolveGroupName(u.GroupID)
	vo.PositionName = resolvePositionName(u.PositionID)
	return vo, nil
}

// UpdateProfile updates the current user's profile fields.
func UpdateProfile(userID string, req *UpdateProfileReq) (*ent.SysUser, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysUser.UpdateOneID(userID)

	if req.Nickname != "" {
		u.SetNickname(req.Nickname)
	}
	if req.Email != "" {
		u.SetEmail(req.Email)
	}
	if req.Phone != "" {
		u.SetPhone(req.Phone)
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(userID).Save(ctx)
}

// UpdateAvatar updates the current user's avatar.
func UpdateAvatar(userID, avatar string) (*ent.SysUser, error) {
	ctx := context.Background()
	now := time.Now()
	return db.Client.SysUser.UpdateOneID(userID).
		SetAvatar(avatar).
		SetUpdatedAt(now).
		SetUpdatedBy(userID).
		Save(ctx)
}

// UpdatePassword verifies the old password and sets the new one.
func UpdatePassword(userID, oldPassword, newPassword string) error {
	ctx := context.Background()
	u, err := db.Client.SysUser.Get(ctx, userID)
	if err != nil {
		return err
	}

	if !utils.BcryptVerify(oldPassword, u.Password) {
		return err
	}

	hashedPwd, err := utils.BcryptHash(newPassword)
	if err != nil {
		return err
	}

	now := time.Now()
	_, err = db.Client.SysUser.UpdateOneID(userID).
		SetPassword(hashedPwd).
		SetUpdatedAt(now).
		SetUpdatedBy(userID).
		Save(ctx)
	return err
}

// GetUserRoles returns all role records for a user.
func GetUserRoles(userID string) ([]ent.RelUserRole, error) {
	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx, "SELECT id, user_id, role_id FROM rel_user_role WHERE user_id = ?", userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []ent.RelUserRole
	for rows.Next() {
		var r ent.RelUserRole
		if err := rows.Scan(&r.ID, &r.UserID, &r.RoleID); err != nil {
			return nil, err
		}
		result = append(result, r)
	}
	return result, rows.Err()
}

// IsSuperAdmin checks if the user has a role with SUPER_ADMIN code.
func IsSuperAdmin(userID string) bool {
	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx,
		"SELECT r.code FROM sys_role r INNER JOIN rel_user_role rur ON r.id = rur.role_id WHERE rur.user_id = ?", userID)
	if err != nil {
		return false
	}
	defer rows.Close()

	for rows.Next() {
		var code string
		if err := rows.Scan(&code); err != nil {
			continue
		}
		if code == "SUPER_ADMIN" {
			return true
		}
	}
	return false
}

// GetUserResourceIDs returns resource IDs assigned to a user via roles.
func GetUserResourceIDs(userID string) ([]string, error) {
	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT DISTINCT rrr.resource_id FROM rel_role_resource rrr
		 INNER JOIN rel_user_role rur ON rrr.role_id = rur.role_id
		 WHERE rur.user_id = ?`, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var ids []string
	for rows.Next() {
		var id string
		if err := rows.Scan(&id); err != nil {
			return nil, err
		}
		ids = append(ids, id)
	}
	return ids, nil
}

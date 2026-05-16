package user

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/relrolepermission"
	"hei-gin/ent/gen/relroleresource"
	"hei-gin/ent/gen/reluserpermission"
	"hei-gin/ent/gen/reluserrole"
	"hei-gin/ent/gen/sysgroup"
	"hei-gin/ent/gen/sysorg"
	"hei-gin/ent/gen/sysposition"
	"hei-gin/ent/gen/sysrole"
	"hei-gin/ent/gen/sysuser"
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
	Motto        string `json:"motto"`
	Github       string `json:"github"`
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
	LoginCount   int    `json:"login_count"`
	LastLoginAt  string `json:"last_login_at"`
	LastLoginIP  string `json:"last_login_ip"`
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
	Motto       string `json:"motto"`
	Github      string `json:"github"`
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
	Motto       string `json:"motto"`
	Github      string `json:"github"`
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
	UserID              string   `json:"user_id" binding:"required"`
	RoleIDs             []string `json:"role_ids" binding:"required"`
	Scope               string   `json:"scope"`
	CustomScopeGroupIds string   `json:"custom_scope_group_ids"`
}

// PermissionItem represents a permission with optional scope info.
type PermissionItem struct {
	PermissionCode      string `json:"permission_code" binding:"required"`
	Scope               string `json:"scope"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
}

type GrantPermissionReq struct {
	UserID      string           `json:"user_id" binding:"required"`
	Permissions []PermissionItem `json:"permissions" binding:"required"`
}

type UpdateProfileReq struct {
	Nickname    string `json:"nickname"`
	Email       string `json:"email"`
	Phone       string `json:"phone"`
	Motto       string `json:"motto"`
	Github      string `json:"github"`
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
	Motto        string `json:"motto"`
	Github       string `json:"github"`
	Gender       string `json:"gender"`
	OrgID        string `json:"org_id"`
	OrgName      string `json:"org_name"`
	GroupID      string `json:"group_id"`
	GroupName    string `json:"group_name"`
	PositionID   string `json:"position_id"`
	PositionName string `json:"position_name"`
	Description  string `json:"description"`
	LoginCount   int    `json:"login_count"`
	LastLoginAt  string `json:"last_login_at"`
	LastLoginIP  string `json:"last_login_ip"`
}

type OwnUserVO struct {
	UserID   string `json:"user_id"`
	Username string `json:"username"`
}

func toVO(u *ent.SysUser) UserVO {
	vo := UserVO{
		ID:          u.ID,
		Username:    u.Username,
		Nickname:    u.Nickname,
		Avatar:      u.Avatar,
		Email:       u.Email,
		Phone:       u.Phone,
		Motto:       u.Motto,
		Github:      u.Github,
		Status:      u.Status,
		OrgID:       u.OrgID,
		GroupID:     u.GroupID,
		PositionID:  u.PositionID,
		Gender:      u.Gender,
		Description: u.Description,
		LoginCount:  u.LoginCount,
		LastLoginAt: u.LastLoginAt.Format("2006-01-02 15:04:05"),
		LastLoginIP: u.LastLoginIP,
		SortCode:    u.SortCode,
		CreatedAt:   u.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   u.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   u.CreatedBy,
		UpdatedBy:   u.UpdatedBy,
	}
	if !u.Birthday.IsZero() {
		vo.Birthday = u.Birthday.Format("2006-01-02")
	}
	if u.LastLoginAt.IsZero() {
		vo.LastLoginAt = ""
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
		Motto:        u.Motto,
		Github:       u.Github,
		Status:       u.Status,
		OrgID:        u.OrgID,
		OrgName:      orgNameMap[u.OrgID],
		GroupID:      u.GroupID,
		GroupName:    groupNameMap[u.GroupID],
		PositionID:   u.PositionID,
		PositionName: positionNameMap[u.PositionID],
		Gender:       u.Gender,
		Description:  u.Description,
		LoginCount:   u.LoginCount,
		LastLoginAt:  u.LastLoginAt.Format("2006-01-02 15:04:05"),
		LastLoginIP:  u.LastLoginIP,
		SortCode:     u.SortCode,
		CreatedAt:    u.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:    u.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:    u.CreatedBy,
		UpdatedBy:    u.UpdatedBy,
	}
	if !u.Birthday.IsZero() {
		vo.Birthday = u.Birthday.Format("2006-01-02")
	}
	if u.LastLoginAt.IsZero() {
		vo.LastLoginAt = ""
	}
	return vo
}

func resolveOrgName(orgID string) string {
	if orgID == "" {
		return ""
	}
	ctx := context.Background()
	org, err := db.Client.SysOrg.Query().Where(sysorg.IDEQ(orgID)).Select(sysorg.FieldName).First(ctx)
	if err != nil {
		return ""
	}
	return org.Name
}

func resolveGroupName(groupID string) string {
	if groupID == "" {
		return ""
	}
	ctx := context.Background()
	g, err := db.Client.SysGroup.Query().Where(sysgroup.IDEQ(groupID)).Select(sysgroup.FieldName).First(ctx)
	if err != nil {
		return ""
	}
	return g.Name
}

func resolvePositionName(positionID string) string {
	if positionID == "" {
		return ""
	}
	ctx := context.Background()
	p, err := db.Client.SysPosition.Query().Where(sysposition.IDEQ(positionID)).Select(sysposition.FieldName).First(ctx)
	if err != nil {
		return ""
	}
	return p.Name
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
		orgs, err := db.Client.SysOrg.Query().
			Where(sysorg.IDIn(orgIDs...)).
			Select(sysorg.FieldID, sysorg.FieldName).
			All(ctx)
		if err == nil {
			for _, o := range orgs {
				orgNameMap[o.ID] = o.Name
			}
		}
	}

	// Resolve group names
	groupNameMap = make(map[string]string, len(groupIDs))
	if len(groupIDs) > 0 {
		groups, err := db.Client.SysGroup.Query().
			Where(sysgroup.IDIn(groupIDs...)).
			Select(sysgroup.FieldID, sysgroup.FieldName).
			All(ctx)
		if err == nil {
			for _, g := range groups {
				groupNameMap[g.ID] = g.Name
			}
		}
	}

	// Resolve position names
	positionNameMap = make(map[string]string, len(positionIDs))
	if len(positionIDs) > 0 {
		positions, err := db.Client.SysPosition.Query().
			Where(sysposition.IDIn(positionIDs...)).
			Select(sysposition.FieldID, sysposition.FieldName).
			All(ctx)
		if err == nil {
			for _, p := range positions {
				positionNameMap[p.ID] = p.Name
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
	if req.Motto != "" {
		q.SetMotto(req.Motto)
	}
	if req.Github != "" {
		q.SetGithub(req.Github)
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
	if req.Motto != "" {
		u.SetMotto(req.Motto)
	}
	if req.Github != "" {
		u.SetGithub(req.Github)
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
	tx, err := db.Client.Tx(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	// Cascade delete from rel tables
	_, err = tx.RelUserRole.Delete().Where(reluserrole.UserIDIn(ids...)).Exec(ctx)
	if err != nil {
		return err
	}
	_, err = tx.RelUserPermission.Delete().Where(reluserpermission.UserIDIn(ids...)).Exec(ctx)
	if err != nil {
		return err
	}

	// Delete users
	_, err = tx.SysUser.Delete().Where(sysuser.IDIn(ids...)).Exec(ctx)
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
	rels, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserIDEQ(userID)).
		Select(reluserrole.FieldRoleID).
		All(ctx)
	if err != nil {
		return nil, err
	}
	roleIDs := make([]string, len(rels))
	for i, r := range rels {
		roleIDs[i] = r.RoleID
	}
	if len(roleIDs) == 0 {
		roleIDs = []string{}
	}
	return roleIDs, nil
}

// PermissionDetailItem is a permission with scope info returned by OwnPermissionDetail.
type PermissionDetailItem struct {
	PermissionCode      string `json:"permission_code"`
	Scope               string `json:"scope"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
}

// OwnPermissionDetail returns both P0 (direct) and P1 (via roles) permissions with scope info.
func OwnPermissionDetail(userID string) (*OwnPermissionDetailVO, error) {
	ctx := context.Background()

	// P0: direct user permissions with scope
	p0Items := []PermissionDetailItem{}
	p0Rels, err := db.Client.RelUserPermission.Query().
		Where(reluserpermission.UserIDEQ(userID)).
		All(ctx)
	if err != nil {
		return nil, err
	}
	for _, r := range p0Rels {
		p0Items = append(p0Items, PermissionDetailItem{
			PermissionCode:      r.PermissionCode,
			Scope:               r.Scope,
			CustomScopeGroupIds: r.CustomScopeGroupIds,
			CustomScopeOrgIds:   r.CustomScopeOrgIds,
		})
	}

	// P1: permissions via roles with scope
	p1Items := []PermissionDetailItem{}
	roleRels, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserIDEQ(userID)).
		Select(reluserrole.FieldRoleID).
		All(ctx)
	if err != nil {
		return nil, err
	}

	for _, rr := range roleRels {
		rpRels, err := db.Client.RelRolePermission.Query().
			Where(relrolepermission.RoleIDEQ(rr.RoleID)).
			All(ctx)
		if err != nil {
			continue
		}
		for _, rp := range rpRels {
			p1Items = append(p1Items, PermissionDetailItem{
				PermissionCode:      rp.PermissionCode,
				Scope:               rp.Scope,
				CustomScopeGroupIds: rp.CustomScopeGroupIds,
				CustomScopeOrgIds:   rp.CustomScopeOrgIds,
			})
		}
	}

	if p0Items == nil {
		p0Items = []PermissionDetailItem{}
	}
	if p1Items == nil {
		p1Items = []PermissionDetailItem{}
	}
	return &OwnPermissionDetailVO{P0: p0Items, P1: p1Items}, nil
}

// OwnPermissionDetailVO groups P0 and P1 permission details.
type OwnPermissionDetailVO struct {
	P0 []PermissionDetailItem `json:"p0"`
	P1 []PermissionDetailItem `json:"p1"`
}

// GrantRole deletes old role assignments and inserts new ones.
func GrantRole(userID string, roleIDs []string) error {
	ctx := context.Background()
	tx, err := db.Client.Tx(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.RelUserRole.Delete().Where(reluserrole.UserIDEQ(userID)).Exec(ctx)
	if err != nil {
		return err
	}

	for _, roleID := range roleIDs {
		_, err = tx.RelUserRole.Create().
			SetID(utils.NextID()).
			SetUserID(userID).
			SetRoleID(roleID).
			Save(ctx)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

// GrantPermission deletes old permission assignments and inserts new ones with scope.
func GrantPermission(userID string, permissions []PermissionItem) error {
	ctx := context.Background()
	tx, err := db.Client.Tx(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.RelUserPermission.Delete().Where(reluserpermission.UserIDEQ(userID)).Exec(ctx)
	if err != nil {
		return err
	}

	for _, p := range permissions {
		scope := p.Scope
		if scope == "" {
			scope = "ALL"
		}
		_, err = tx.RelUserPermission.Create().
			SetID(utils.NextID()).
			SetUserID(userID).
			SetPermissionCode(p.PermissionCode).
			SetScope(scope).
			SetCustomScopeGroupIds(p.CustomScopeGroupIds).
			SetCustomScopeOrgIds(p.CustomScopeOrgIds).
			Save(ctx)
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
		Motto:       u.Motto,
		Github:      u.Github,
		Gender:      u.Gender,
		OrgID:       u.OrgID,
		GroupID:     u.GroupID,
		PositionID:  u.PositionID,
		Description: u.Description,
		LoginCount:  u.LoginCount,
		LastLoginAt: u.LastLoginAt.Format("2006-01-02 15:04:05"),
		LastLoginIP: u.LastLoginIP,
	}
	if u.LastLoginAt.IsZero() {
		vo.LastLoginAt = ""
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
	if req.Motto != "" {
		u.SetMotto(req.Motto)
	}
	if req.Github != "" {
		u.SetGithub(req.Github)
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
func GetUserRoles(userID string) ([]*ent.RelUserRole, error) {
	ctx := context.Background()
	return db.Client.RelUserRole.Query().Where(reluserrole.UserIDEQ(userID)).All(ctx)
}

// IsSuperAdmin checks if the user has a role with SUPER_ADMIN code.
func IsSuperAdmin(userID string) bool {
	ctx := context.Background()
	roleRels, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserIDEQ(userID)).
		Select(reluserrole.FieldRoleID).
		All(ctx)
	if err != nil || len(roleRels) == 0 {
		return false
	}
	roleIDs := make([]string, len(roleRels))
	for i, r := range roleRels {
		roleIDs[i] = r.RoleID
	}
	count, err := db.Client.SysRole.Query().
		Where(sysrole.IDIn(roleIDs...), sysrole.CodeEQ("SUPER_ADMIN")).
		Count(ctx)
	return err == nil && count > 0
}

// GetUserResourceIDs returns resource IDs assigned to a user via roles.
func GetUserResourceIDs(userID string) ([]string, error) {
	ctx := context.Background()
	roleRels, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserIDEQ(userID)).
		Select(reluserrole.FieldRoleID).
		All(ctx)
	if err != nil {
		return nil, err
	}
	if len(roleRels) == 0 {
		return []string{}, nil
	}
	roleIDs := make([]string, len(roleRels))
	for i, r := range roleRels {
		roleIDs[i] = r.RoleID
	}
	ress, err := db.Client.RelRoleResource.Query().
		Where(relroleresource.RoleIDIn(roleIDs...)).
		Select(relroleresource.FieldResourceID).
		All(ctx)
	if err != nil {
		return nil, err
	}
	seen := make(map[string]struct{})
	var ids []string
	for _, r := range ress {
		if _, ok := seen[r.ResourceID]; !ok {
			seen[r.ResourceID] = struct{}{}
			ids = append(ids, r.ResourceID)
		}
	}
	if ids == nil {
		ids = []string{}
	}
	return ids, nil
}

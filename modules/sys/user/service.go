package user

import (
	"context"
	"sort"
	"time"

	"hei-gin/core/auth"
	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/core/enums"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/relrolepermission"
	"hei-gin/ent/gen/relroleresource"
	"hei-gin/ent/gen/reluserpermission"
	"hei-gin/ent/gen/reluserrole"
	"hei-gin/ent/gen/sysposition"
	"hei-gin/ent/gen/sysresource"
	"hei-gin/ent/gen/sysrole"
	"hei-gin/ent/gen/sysuser"
	"hei-gin/modules/sys/role"

	"entgo.io/ent/dialect/sql"
	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
)

// ---------------------------------------------------------------------------
// Time helpers
// ---------------------------------------------------------------------------

func formatDate(t *time.Time) string {
	if t == nil {
		return ""
	}
	return t.Format("2006-01-02")
}

func formatTime(t *time.Time) string {
	if t == nil {
		return ""
	}
	return t.Format("2006-01-02 15:04:05")
}

func parseDate(s string) *time.Time {
	if s == "" {
		return nil
	}
	t, err := time.Parse("2006-01-02", s)
	if err != nil {
		return nil
	}
	return &t
}

// ---------------------------------------------------------------------------
// Name path resolution
// ---------------------------------------------------------------------------

type namePathNode struct {
	Name     string
	ParentID *string
}

func buildNamePathMapFromOrgs(orgs []*ent.SysOrg) map[string]namePathNode {
	m := make(map[string]namePathNode, len(orgs))
	for _, o := range orgs {
		m[o.ID] = namePathNode{Name: o.Name, ParentID: o.ParentID}
	}
	return m
}

func buildNamePathMapFromGroups(groups []*ent.SysGroup) map[string]namePathNode {
	m := make(map[string]namePathNode, len(groups))
	for _, g := range groups {
		m[g.ID] = namePathNode{Name: g.Name, ParentID: g.ParentID}
	}
	return m
}

func resolveNamePathFromMap(id *string, nodeMap map[string]namePathNode) []string {
	if id == nil || *id == "" {
		return nil
	}
	var path []string
	current := *id
	for {
		node, ok := nodeMap[current]
		if !ok {
			break
		}
		path = append(path, node.Name)
		if node.ParentID == nil || *node.ParentID == "" {
			break
		}
		current = *node.ParentID
	}
	// Reverse to get root -> leaf order
	for i, j := 0, len(path)-1; i < j; i, j = i+1, j-1 {
		path[i], path[j] = path[j], path[i]
	}
	return path
}

// ---------------------------------------------------------------------------
// entToVO
// ---------------------------------------------------------------------------

func entToVO(entity *ent.SysUser) *UserVO {
	if entity == nil {
		return nil
	}
	return &UserVO{
		ID:          entity.ID,
		Username:    entity.Username,
		Nickname:    entity.Nickname,
		Avatar:      entity.Avatar,
		Motto:       entity.Motto,
		Gender:      entity.Gender,
		Birthday:    formatDate(entity.Birthday),
		Email:       entity.Email,
		Github:      entity.Github,
		Phone:       entity.Phone,
		OrgID:       entity.OrgID,
		PositionID:  entity.PositionID,
		GroupID:     entity.GroupID,
		Status:      entity.Status,
		LastLoginAt: formatTime(entity.LastLoginAt),
		LastLoginIP: entity.LastLoginIP,
		LoginCount:  entity.LoginCount,
		CreatedAt:   formatTime(entity.CreatedAt),
		CreatedBy:   entity.CreatedBy,
		UpdatedAt:   formatTime(entity.UpdatedAt),
		UpdatedBy:   entity.UpdatedBy,
	}
}

// ---------------------------------------------------------------------------
// Batch role IDs
// ---------------------------------------------------------------------------

func batchGetRoleIDs(userIDs []string) map[string][]string {
	if len(userIDs) == 0 {
		return nil
	}
	ctx := context.Background()
	relations, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserIDIn(userIDs...)).
		All(ctx)
	if err != nil {
		return nil
	}
	result := make(map[string][]string)
	for _, r := range relations {
		result[r.UserID] = append(result[r.UserID], r.RoleID)
	}
	return result
}

// ---------------------------------------------------------------------------
// Batch enrich org/group/position names
// ---------------------------------------------------------------------------

func batchEnrichNames(vos []*UserVO) {
	if len(vos) == 0 {
		return
	}
	ctx := context.Background()

	// Collect position IDs
	posIDs := make([]string, 0)
	for _, vo := range vos {
		if vo.PositionID != nil && *vo.PositionID != "" {
			posIDs = append(posIDs, *vo.PositionID)
		}
	}

	// Batch query position names
	posMap := make(map[string]string)
	if len(posIDs) > 0 {
		positions, err := db.Client.SysPosition.Query().
			Where(sysposition.IDIn(posIDs...)).
			All(ctx)
		if err == nil {
			for _, p := range positions {
				posMap[p.ID] = p.Name
			}
		}
	}

	// Query all orgs and groups for name path resolution
	orgs, err := db.Client.SysOrg.Query().All(ctx)
	orgNodeMap := make(map[string]namePathNode)
	if err == nil {
		orgNodeMap = buildNamePathMapFromOrgs(orgs)
	}

	groups, err := db.Client.SysGroup.Query().All(ctx)
	groupNodeMap := make(map[string]namePathNode)
	if err == nil {
		groupNodeMap = buildNamePathMapFromGroups(groups)
	}

	for _, vo := range vos {
		vo.OrgNames = resolveNamePathFromMap(vo.OrgID, orgNodeMap)
		vo.GroupNames = resolveNamePathFromMap(vo.GroupID, groupNodeMap)
		if vo.PositionID != nil {
			if name, ok := posMap[*vo.PositionID]; ok {
				vo.PositionName = &name
			}
		}
	}
}

// ---------------------------------------------------------------------------
// Role helpers
// ---------------------------------------------------------------------------

func getUserRoleCodes(userID string) []string {
	ctx := context.Background()
	relations, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserID(userID)).
		All(ctx)
	if err != nil || len(relations) == 0 {
		return nil
	}
	roleIDs := make([]string, len(relations))
	for i, r := range relations {
		roleIDs[i] = r.RoleID
	}
	roles, err := db.Client.SysRole.Query().
		Where(sysrole.IDIn(roleIDs...)).
		All(ctx)
	if err != nil {
		return nil
	}
	codes := make([]string, len(roles))
	for i, r := range roles {
		codes[i] = r.Code
	}
	return codes
}

func getUserAllRoleIDs(userID string) []string {
	ctx := context.Background()
	relations, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserID(userID)).
		All(ctx)
	if err != nil {
		return nil
	}
	ids := make([]string, len(relations))
	for i, r := range relations {
		ids[i] = r.RoleID
	}
	return ids
}

func grantRoles(userID string, roleIDs []string, createdBy string) {
	ctx := context.Background()
	// Delete existing
	_, _ = db.Client.RelUserRole.Delete().
		Where(reluserrole.UserID(userID)).
		Exec(ctx)
	// Re-create
	for _, rid := range roleIDs {
		_, _ = db.Client.RelUserRole.Create().
			SetID(utils.GenerateID()).
			SetUserID(userID).
			SetRoleID(rid).
			Save(ctx)
	}
}

// ---------------------------------------------------------------------------
// UserPage
// ---------------------------------------------------------------------------

func UserPage(c *gin.Context, param *UserPageParam) gin.H {
	ctx := context.Background()

	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	query := db.Client.SysUser.Query()
	if param.Keyword != "" {
		query = query.Where(sysuser.Or(
			sysuser.UsernameContains(param.Keyword),
			sysuser.NicknameContains(param.Keyword),
		))
	}
	if param.Status != "" {
		query = query.Where(sysuser.StatusEQ(param.Status))
	}

	total, err := query.Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户列表失败: "+err.Error(), 500))
	}

	offset := (param.Current - 1) * param.Size
	records, err := query.Clone().
		Order(sysuser.ByCreatedAt(sql.OrderDesc())).
		Limit(param.Size).
		Offset(offset).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户列表失败: "+err.Error(), 500))
	}

	userIDs := make([]string, len(records))
	for i, r := range records {
		userIDs[i] = r.ID
	}
	roleMap := batchGetRoleIDs(userIDs)

	vos := make([]*UserVO, len(records))
	for i, r := range records {
		vos[i] = entToVO(r)
		vos[i].RoleIDs = roleMap[r.ID]
	}

	batchEnrichNames(vos)

	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

// ---------------------------------------------------------------------------
// UserCreate
// ---------------------------------------------------------------------------

func UserCreate(c *gin.Context, vo *UserVO, userID string) {
	ctx := context.Background()

	if vo.Username != nil && *vo.Username != "" {
		exists, err := db.Client.SysUser.Query().
			Where(sysuser.UsernameEQ(*vo.Username)).
			Exist(ctx)
		if err == nil && exists {
			panic(exception.NewBusinessError("账号已存在", 400))
		}
	}

	if vo.Email != nil && *vo.Email != "" {
		exists, err := db.Client.SysUser.Query().
			Where(sysuser.EmailEQ(*vo.Email)).
			Exist(ctx)
		if err == nil && exists {
			panic(exception.NewBusinessError("邮箱已存在", 400))
		}
	}

	now := time.Now()
	builder := db.Client.SysUser.Create().
		SetID(utils.GenerateID()).
		SetCreatedAt(now).
		SetUpdatedAt(now)

	if vo.Username != nil {
		builder.SetNillableUsername(vo.Username)
	}
	if vo.Nickname != nil {
		builder.SetNillableNickname(vo.Nickname)
	}
	if vo.Avatar != nil {
		builder.SetNillableAvatar(vo.Avatar)
	}
	if vo.Motto != nil {
		builder.SetNillableMotto(vo.Motto)
	}
	if vo.Gender != nil {
		builder.SetNillableGender(vo.Gender)
	}
	if vo.Birthday != "" {
		builder.SetNillableBirthday(parseDate(vo.Birthday))
	}
	if vo.Email != nil {
		builder.SetNillableEmail(vo.Email)
	}
	if vo.Github != nil {
		builder.SetNillableGithub(vo.Github)
	}
	if vo.Phone != nil {
		builder.SetNillablePhone(vo.Phone)
	}
	if vo.OrgID != nil {
		builder.SetNillableOrgID(vo.OrgID)
	}
	if vo.PositionID != nil {
		builder.SetNillablePositionID(vo.PositionID)
	}
	if vo.GroupID != nil {
		builder.SetNillableGroupID(vo.GroupID)
	}
	if vo.Status != "" {
		builder.SetStatus(vo.Status)
	}
	if userID != "" {
		builder.SetCreatedBy(userID).SetUpdatedBy(userID)
	}

	entity, err := builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("添加用户失败: "+err.Error(), 500))
	}

	if len(vo.RoleIDs) > 0 {
		grantRoles(entity.ID, vo.RoleIDs, userID)
	}
}

// ---------------------------------------------------------------------------
// UserModify
// ---------------------------------------------------------------------------

func UserModify(c *gin.Context, vo *UserVO, userID string) {
	ctx := context.Background()

	if vo.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}

	// Verify exists
	_, err := db.Client.SysUser.Get(ctx, vo.ID)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("数据不存在", 404))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}

	now := time.Now()
	builder := db.Client.SysUser.UpdateOneID(vo.ID).
		SetUpdatedAt(now)

	if vo.Username != nil {
		builder.SetNillableUsername(vo.Username)
	}
	if vo.Nickname != nil {
		builder.SetNillableNickname(vo.Nickname)
	}
	if vo.Avatar != nil {
		builder.SetNillableAvatar(vo.Avatar)
	}
	if vo.Motto != nil {
		builder.SetNillableMotto(vo.Motto)
	}
	if vo.Gender != nil {
		builder.SetNillableGender(vo.Gender)
	}
	if vo.Birthday != "" {
		builder.SetNillableBirthday(parseDate(vo.Birthday))
	}
	if vo.Email != nil {
		builder.SetNillableEmail(vo.Email)
	}
	if vo.Github != nil {
		builder.SetNillableGithub(vo.Github)
	}
	if vo.Phone != nil {
		builder.SetNillablePhone(vo.Phone)
	}
	if vo.OrgID != nil {
		builder.SetNillableOrgID(vo.OrgID)
	}
	if vo.PositionID != nil {
		builder.SetNillablePositionID(vo.PositionID)
	}
	if vo.GroupID != nil {
		if *vo.GroupID != "" {
			builder.SetGroupID(*vo.GroupID)
		} else {
			builder.ClearGroupID()
		}
	}
	if vo.Status != "" {
		builder.SetStatus(vo.Status)
	}
	if userID != "" {
		builder.SetUpdatedBy(userID)
	}

	_, err = builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("编辑用户失败: "+err.Error(), 500))
	}

	// Re-grant roles if role_ids specified (even if empty array)
	if vo.RoleIDs != nil {
		grantRoles(vo.ID, vo.RoleIDs, userID)
	}
}

// ---------------------------------------------------------------------------
// UserRemove
// ---------------------------------------------------------------------------

func UserRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()

	// Delete user-role relations
	_, _ = db.Client.RelUserRole.Delete().
		Where(reluserrole.UserIDIn(ids...)).
		Exec(ctx)

	// Delete user-permission relations
	_, _ = db.Client.RelUserPermission.Delete().
		Where(reluserpermission.UserIDIn(ids...)).
		Exec(ctx)

	// Delete users
	_, err := db.Client.SysUser.Delete().
		Where(sysuser.IDIn(ids...)).
		Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除用户失败: "+err.Error(), 500))
	}
}

// ---------------------------------------------------------------------------
// UserDetail
// ---------------------------------------------------------------------------

func UserDetail(c *gin.Context, id string) *UserVO {
	if id == "" {
		return nil
	}
	ctx := context.Background()

	entity, err := db.Client.SysUser.Get(ctx, id)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil
		}
		panic(exception.NewBusinessError("查询用户详情失败: "+err.Error(), 500))
	}

	vo := entToVO(entity)

	// Enrich role IDs
	roleRelations, _ := db.Client.RelUserRole.Query().
		Where(reluserrole.UserID(id)).
		All(ctx)
	if len(roleRelations) > 0 {
		vo.RoleIDs = make([]string, len(roleRelations))
		for i, r := range roleRelations {
			vo.RoleIDs[i] = r.RoleID
		}
	}

	// Enrich names
	batchEnrichNames([]*UserVO{vo})

	return vo
}

// ---------------------------------------------------------------------------
// UserGrantRoles
// ---------------------------------------------------------------------------

func UserGrantRoles(c *gin.Context, userID string, roleIDs []string, createdBy string) {
	// Verify user exists
	ctx := context.Background()
	_, err := db.Client.SysUser.Get(ctx, userID)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("用户不存在", 404))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}

	grantRoles(userID, roleIDs, createdBy)
}

// ---------------------------------------------------------------------------
// UserGrantPermissions
// ---------------------------------------------------------------------------

func UserGrantPermissions(c *gin.Context, userID string, permissions []role.PermissionItem, createdBy string) {
	ctx := context.Background()

	// Verify user exists
	_, err := db.Client.SysUser.Get(ctx, userID)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("用户不存在", 404))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}

	// Delete existing
	_, _ = db.Client.RelUserPermission.Delete().
		Where(reluserpermission.UserID(userID)).
		Exec(ctx)

	// Re-create
	for _, p := range permissions {
		builder := db.Client.RelUserPermission.Create().
			SetID(utils.GenerateID()).
			SetUserID(userID).
			SetPermissionCode(p.PermissionCode)

		if p.Scope != "" {
			builder.SetScope(p.Scope)
		}
		if p.CustomScopeGroupIds != nil {
			builder.SetNillableCustomScopeGroupIds(p.CustomScopeGroupIds)
		}
		if p.CustomScopeOrgIds != nil {
			builder.SetNillableCustomScopeOrgIds(p.CustomScopeOrgIds)
		}

		_, err := builder.Save(ctx)
		if err != nil {
			panic(exception.NewBusinessError("分配用户权限失败: "+err.Error(), 500))
		}
	}
}

// ---------------------------------------------------------------------------
// UserOwnPermissionDetails
// ---------------------------------------------------------------------------

func UserOwnPermissionDetails(c *gin.Context, userID string) []map[string]interface{} {
	ctx := context.Background()
	relations, err := db.Client.RelUserPermission.Query().
		Where(reluserpermission.UserID(userID)).
		All(ctx)
	if err != nil {
		return nil
	}
	result := make([]map[string]interface{}, 0, len(relations))
	for _, r := range relations {
		item := map[string]interface{}{
			"permission_code": r.PermissionCode,
			"scope":           r.Scope,
		}
		if r.CustomScopeGroupIds != nil {
			item["custom_scope_group_ids"] = *r.CustomScopeGroupIds
		} else {
			item["custom_scope_group_ids"] = nil
		}
		if r.CustomScopeOrgIds != nil {
			item["custom_scope_org_ids"] = *r.CustomScopeOrgIds
		} else {
			item["custom_scope_org_ids"] = nil
		}
		result = append(result, item)
	}
	return result
}

// ---------------------------------------------------------------------------
// UserOwnRoles
// ---------------------------------------------------------------------------

func UserOwnRoles(c *gin.Context, userID string) []string {
	ctx := context.Background()
	relations, err := db.Client.RelUserRole.Query().
		Where(reluserrole.UserID(userID)).
		All(ctx)
	if err != nil {
		return nil
	}
	ids := make([]string, len(relations))
	for i, r := range relations {
		ids[i] = r.RoleID
	}
	return ids
}

// ---------------------------------------------------------------------------
// UserCurrent
// ---------------------------------------------------------------------------

func UserCurrent(c *gin.Context) *UserVO {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		return nil
	}
	ctx := context.Background()
	entity, err := db.Client.SysUser.Get(ctx, userID)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}
	vo := entToVO(entity)
	batchEnrichNames([]*UserVO{vo})
	return vo
}

// ---------------------------------------------------------------------------
// UserMenus
// ---------------------------------------------------------------------------

func UserMenus(c *gin.Context) []map[string]interface{} {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		return nil
	}
	ctx := context.Background()

	roleCodes := getUserRoleCodes(userID)
	isSuperAdmin := false
	for _, code := range roleCodes {
		if code == constants.SUPER_ADMIN_CODE {
			isSuperAdmin = true
			break
		}
	}

	var resources []*ent.SysResource
	if isSuperAdmin {
		var err error
		resources, err = db.Client.SysResource.Query().
			Where(
				sysresource.CategoryEQ(string(enums.ResourceCategoryBackendMenu)),
				sysresource.TypeIn(string(enums.ResourceTypeDirectory), string(enums.ResourceTypeMenu)),
				sysresource.StatusEQ(string(enums.StatusEnabled)),
			).
			Order(sysresource.BySortCode()).
			All(ctx)
		if err != nil {
			return nil
		}
	} else {
		roleIDs := getUserAllRoleIDs(userID)
		if len(roleIDs) == 0 {
			return nil
		}

		rrRelations, err := db.Client.RelRoleResource.Query().
			Where(relroleresource.RoleIDIn(roleIDs...)).
			All(ctx)
		if err != nil || len(rrRelations) == 0 {
			return nil
		}

		resourceIDSet := make(map[string]struct{})
		for _, rr := range rrRelations {
			resourceIDSet[rr.ResourceID] = struct{}{}
		}
		resourceIDs := make([]string, 0, len(resourceIDSet))
		for id := range resourceIDSet {
			resourceIDs = append(resourceIDs, id)
		}

		resources, err = db.Client.SysResource.Query().
			Where(
				sysresource.IDIn(resourceIDs...),
				sysresource.CategoryEQ(string(enums.ResourceCategoryBackendMenu)),
				sysresource.TypeIn(string(enums.ResourceTypeDirectory), string(enums.ResourceTypeMenu)),
				sysresource.StatusEQ(string(enums.StatusEnabled)),
			).
			Order(sysresource.BySortCode()).
			All(ctx)
		if err != nil {
			return nil
		}
	}

	return buildMenuTree(resources)
}

// ---------------------------------------------------------------------------
// UserPermissions
// ---------------------------------------------------------------------------

func UserPermissions(c *gin.Context) []string {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		return nil
	}
	ctx := context.Background()

	roleCodes := getUserRoleCodes(userID)
	isSuperAdmin := false
	for _, code := range roleCodes {
		if code == constants.SUPER_ADMIN_CODE {
			isSuperAdmin = true
			break
		}
	}

	permSet := make(map[string]struct{})

	if isSuperAdmin {
		// Return all permissions in the system
		allRolePerms, err := db.Client.RelRolePermission.Query().All(ctx)
		if err == nil {
			for _, p := range allRolePerms {
				permSet[p.PermissionCode] = struct{}{}
			}
		}
		allDirectPerms, err := db.Client.RelUserPermission.Query().All(ctx)
		if err == nil {
			for _, p := range allDirectPerms {
				permSet[p.PermissionCode] = struct{}{}
			}
		}
	} else {
		roleIDs := getUserAllRoleIDs(userID)
		if len(roleIDs) > 0 {
			perms, err := db.Client.RelRolePermission.Query().
				Where(relrolepermission.RoleIDIn(roleIDs...)).
				All(ctx)
			if err == nil {
				for _, p := range perms {
					permSet[p.PermissionCode] = struct{}{}
				}
			}
		}
		directPerms, err := db.Client.RelUserPermission.Query().
			Where(reluserpermission.UserID(userID)).
			All(ctx)
		if err == nil {
			for _, p := range directPerms {
				permSet[p.PermissionCode] = struct{}{}
			}
		}
	}

	result := make([]string, 0, len(permSet))
	for code := range permSet {
		result = append(result, code)
	}
	sort.Strings(result)
	return result
}

// ---------------------------------------------------------------------------
// UserUpdateProfile
// ---------------------------------------------------------------------------

func UserUpdateProfile(c *gin.Context, param *UpdateProfileParam) {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	ctx := context.Background()

	// Check username uniqueness if changed
	if param.Username != nil && *param.Username != "" {
		exists, err := db.Client.SysUser.Query().
			Where(sysuser.And(
				sysuser.UsernameEQ(*param.Username),
				sysuser.IDNEQ(userID),
			)).
			Exist(ctx)
		if err == nil && exists {
			panic(exception.NewBusinessError("账号已存在", 400))
		}
	}

	now := time.Now()
	builder := db.Client.SysUser.UpdateOneID(userID).SetUpdatedAt(now)

	if param.Username != nil {
		builder.SetNillableUsername(param.Username)
	}
	if param.Nickname != nil {
		builder.SetNillableNickname(param.Nickname)
	}
	if param.Motto != nil {
		builder.SetNillableMotto(param.Motto)
	}
	if param.Gender != nil {
		builder.SetNillableGender(param.Gender)
	}
	if param.Birthday != "" {
		builder.SetNillableBirthday(parseDate(param.Birthday))
	}
	if param.Email != nil {
		builder.SetNillableEmail(param.Email)
	}
	if param.Github != nil {
		builder.SetNillableGithub(param.Github)
	}
	if param.Phone != nil {
		builder.SetNillablePhone(param.Phone)
	}

	_, err := builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("更新个人信息失败: "+err.Error(), 500))
	}
}

// ---------------------------------------------------------------------------
// UserUpdateAvatar
// ---------------------------------------------------------------------------

func UserUpdateAvatar(c *gin.Context, param *UpdateAvatarParam) {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	ctx := context.Background()

	err := db.Client.SysUser.UpdateOneID(userID).
		SetAvatar(param.Avatar).
		Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("更新头像失败: "+err.Error(), 500))
	}
}

// ---------------------------------------------------------------------------
// UserUpdatePassword
// ---------------------------------------------------------------------------

func UserUpdatePassword(c *gin.Context, param *UpdatePasswordParam) {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	ctx := context.Background()

	entity, err := db.Client.SysUser.Get(ctx, userID)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("用户不存在", 404))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}

	if entity.Password == nil || *entity.Password == "" {
		panic(exception.NewBusinessError("未设置密码，无法修改", 400))
	}

	currentPassword := utils.Decrypt(param.CurrentPassword)
	err = bcrypt.CompareHashAndPassword([]byte(*entity.Password), []byte(currentPassword))
	if err != nil {
		panic(exception.NewBusinessError("当前密码不正确", 400))
	}

	newPassword := utils.Decrypt(param.NewPassword)
	hashed, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		panic(exception.NewBusinessError("密码加密失败", 500))
	}

	err = db.Client.SysUser.UpdateOneID(userID).
		SetPassword(string(hashed)).
		Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("修改密码失败: "+err.Error(), 500))
	}
}

// ---------------------------------------------------------------------------
// Menu tree helpers
// ---------------------------------------------------------------------------

func buildMenuTree(resources []*ent.SysResource) []map[string]interface{} {
	childrenMap := make(map[string][]*ent.SysResource)
	for _, r := range resources {
		pid := ""
		if r.ParentID != nil {
			pid = *r.ParentID
		}
		childrenMap[pid] = append(childrenMap[pid], r)
	}

	// Sort each group by sort_code
	for _, children := range childrenMap {
		sort.Slice(children, func(i, j int) bool {
			return children[i].SortCode < children[j].SortCode
		})
	}

	return buildMenuChildren(childrenMap, "")
}

func buildMenuChildren(childrenMap map[string][]*ent.SysResource, parentID string) []map[string]interface{} {
	children := childrenMap[parentID]
	result := make([]map[string]interface{}, 0, len(children))
	for _, r := range children {
		node := resourceToNode(r)
		node["children"] = buildMenuChildren(childrenMap, r.ID)
		result = append(result, node)
	}
	return result
}

func resourceToNode(r *ent.SysResource) map[string]interface{} {
	node := map[string]interface{}{
		"id":             r.ID,
		"code":           r.Code,
		"name":           r.Name,
		"category":       r.Category,
		"type":           r.Type,
		"route_path":     r.RoutePath,
		"component_path": r.ComponentPath,
		"redirect_path":  r.RedirectPath,
		"icon":           r.Icon,
		"color":          r.Color,
		"is_visible":     r.IsVisible,
		"is_cache":       r.IsCache,
		"is_affix":       r.IsAffix,
		"is_breadcrumb":  r.IsBreadcrumb,
		"external_url":   r.ExternalURL,
		"sort_code":      r.SortCode,
		"status":         r.Status,
	}
	if r.ParentID != nil {
		node["parent_id"] = *r.ParentID
	} else {
		node["parent_id"] = nil
	}
	if r.Description != nil {
		node["description"] = *r.Description
	}
	if r.Extra != nil {
		node["extra"] = *r.Extra
	}
	return node
}

package user

import (
	"context"
	"sync"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/enums"
	"hei-gin/core/utils"

	"hei-gin/core/constants"
	"hei-gin/core/pojo"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
)

type cacheOrg struct {
	ID       string
	Name     string
	ParentID *string
}
type cacheGroup struct {
	ID       string
	Name     string
	ParentID *string
}

var (
	cachedOrgs   []cacheOrg
	cachedGroups []cacheGroup
	cacheMu      sync.RWMutex
	cacheExpiry  time.Time
)

func formatTime(t *time.Time) string { if t == nil { return "" }; return pojo.FormatDateTime(*t) }
func fmtDate(t *time.Time) string {
	if t == nil {
		return ""
	}
	return t.Format("2006-01-02")
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

func entToVO(e *SysUser) *UserVO {
	if e == nil {
		return nil
	}
	return &UserVO{
		ID: e.ID, Username: e.Username, Nickname: e.Nickname, Avatar: e.Avatar,
		Motto: e.Motto, Gender: e.Gender, Birthday: fmtDate(e.Birthday),
		Email: e.Email, Github: e.Github, Phone: e.Phone,
		OrgID: e.OrgID, PositionID: e.PositionID, GroupID: e.GroupID,
		Status: e.Status, LastLoginAt: formatTime(e.LastLoginAt), LastLoginIP: e.LastLoginIP,
		LoginCount: e.LoginCount, CreatedAt: formatTime(e.CreatedAt), CreatedBy: e.CreatedBy,
		UpdatedAt: formatTime(e.UpdatedAt), UpdatedBy: e.UpdatedBy,
	}
}

func batchRoleIDs(uids []string) map[string][]string {
	if len(uids) == 0 {
		return nil
	}
	ctx := context.Background()
	var rr []RelUserRole
	db.DB.WithContext(ctx).Where("user_id IN ?", uids).Find(&rr)
	m := make(map[string][]string)
	for _, r := range rr {
		m[r.UserID] = append(m[r.UserID], r.RoleID)
	}
	return m
}

func enrichNames(vos []*UserVO) {
	if len(vos) == 0 {
		return
	}
	ctx := context.Background()

	var pids []string
	for _, v := range vos {
		if v.PositionID != nil && *v.PositionID != "" {
			pids = append(pids, *v.PositionID)
		}
	}
	pn := make(map[string]string)
	if len(pids) > 0 {
		type pr struct{ ID, Name string }
		var ps []pr
		db.DB.WithContext(ctx).Table("sys_position").Where("id IN ?", pids).Find(&ps)
		for _, p := range ps {
			pn[p.ID] = p.Name
		}
	}

	cacheMu.RLock()
	if time.Since(cacheExpiry) > 5*time.Minute {
		cacheMu.RUnlock()
		cacheMu.Lock()
		if time.Since(cacheExpiry) > 5*time.Minute {
			db.DB.WithContext(ctx).Table("sys_org").Select("id,name,parent_id").Find(&cachedOrgs)
			db.DB.WithContext(ctx).Table("sys_group").Select("id,name,parent_id").Find(&cachedGroups)
			cacheExpiry = time.Now()
		}
		cacheMu.Unlock()
		cacheMu.RLock()
	}
	orgMap := make(map[string]struct {
		N string
		P *string
	})
	for _, o := range cachedOrgs {
		orgMap[o.ID] = struct {
			N string
			P *string
		}{o.Name, o.ParentID}
	}
	grpMap := make(map[string]struct {
		N string
		P *string
	})
	for _, g := range cachedGroups {
		grpMap[g.ID] = struct {
			N string
			P *string
		}{g.Name, g.ParentID}
	}
	cacheMu.RUnlock()

	resolve := func(id *string, m map[string]struct {
		N string
		P *string
	}) []string {
		if id == nil || *id == "" {
			return nil
		}
		var path []string
		cur := *id
		for {
			n, ok := m[cur]
			if !ok {
				break
			}
			path = append(path, n.N)
			if n.P == nil || *n.P == "" {
				break
			}
			cur = *n.P
		}
		for i, j := 0, len(path)-1; i < j; i, j = i+1, j-1 {
			path[i], path[j] = path[j], path[i]
		}
		return path
	}

	for _, v := range vos {
		if v.PositionID != nil {
			if n, ok := pn[*v.PositionID]; ok {
				v.PositionName = &n
			}
		}
		if v.OrgID != nil {
			v.OrgNames = resolve(v.OrgID, orgMap)
		}
		if v.GroupID != nil {
			v.GroupNames = resolve(v.GroupID, grpMap)
		}
	}
}

func UserPage(c *gin.Context, p *UserPageParam) gin.H {
	ctx := context.Background()
	if p.Current < 1 {
		p.Current = 1
	}
	if p.Size < 1 || p.Size > 100 {
		p.Size = 10
	}

	q := db.DB.WithContext(ctx).Model(&SysUser{})
	if p.Keyword != "" {
		like := "%" + p.Keyword + "%"
		q = q.Where("username LIKE ? OR nickname LIKE ? OR phone LIKE ? OR email LIKE ?", like, like, like, like)
	}
	if p.Status != "" {
		q = q.Where("status = ?", p.Status)
	}

	var total int64
	q.Count(&total)

	var rows []SysUser
	q.Order("created_at DESC").Limit(p.Size).Offset((p.Current - 1) * p.Size).Find(&rows)

	vos := make([]*UserVO, len(rows))
	for i, r := range rows {
		vos[i] = entToVO(&r)
	}
	enrichNames(vos)
	rm := batchRoleIDs(func() []string {
		ids := make([]string, len(rows))
		for i, r := range rows {
			ids[i] = r.ID
		}
		return ids
	}())
	for _, v := range vos {
		v.RoleIDs = rm[v.ID]
	}
	return result.PageDataResult(c, vos, total, p.Current, p.Size)
}

func UserCreate(c *gin.Context, v *UserVO, uid string) {
	ctx := context.Background()
	now := time.Now()
	e := SysUser{ID: utils.GenerateID(), Status: string(enums.UserStatusActive), CreatedAt: &now, UpdatedAt: &now}
	if v.Username != nil {
		var c int64
		db.DB.WithContext(ctx).Model(&SysUser{}).Where("username = ?", *v.Username).Count(&c)
		if c > 0 {
			panic(exception.NewBusinessError("账号已存在", 400))
		}
		e.Username = v.Username
	}
	if v.Password != nil {
		h, _ := bcrypt.GenerateFromPassword([]byte(*v.Password), bcrypt.DefaultCost)
		s := string(h)
		e.Password = &s
	}
	if v.Nickname != nil {
		e.Nickname = v.Nickname
	}
	if v.Avatar != nil {
		e.Avatar = v.Avatar
	}
	if v.Motto != nil {
		e.Motto = v.Motto
	}
	if v.Gender != nil {
		e.Gender = v.Gender
	}
	if v.Birthday != "" {
		e.Birthday = parseDate(v.Birthday)
	}
	if v.Email != nil {
		e.Email = v.Email
	}
	if v.Github != nil {
		e.Github = v.Github
	}
	if v.Phone != nil {
		e.Phone = v.Phone
	}
	if v.OrgID != nil {
		e.OrgID = v.OrgID
	}
	if v.PositionID != nil {
		e.PositionID = v.PositionID
	}
	if v.GroupID != nil {
		e.GroupID = v.GroupID
	}
	if v.Status != "" {
		e.Status = v.Status
	}
	if uid != "" {
		e.CreatedBy = &uid
		e.UpdatedBy = &uid
	}
	if err := db.DB.WithContext(ctx).Create(&e).Error; err != nil {
		panic(exception.NewBusinessError("添加用户失败: "+err.Error(), 500))
	}
}

func UserDetail(c *gin.Context, id string) *UserVO {
	if id == "" {
		return nil
	}
	ctx := context.Background()
	var e SysUser
	if err := db.DB.WithContext(ctx).First(&e, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil
		}
		panic(exception.NewBusinessError("查询用户详情失败: "+err.Error(), 500))
	}
	vo := entToVO(&e)
	enrichNames([]*UserVO{vo})
	if rm := batchRoleIDs([]string{e.ID}); rm != nil {
		vo.RoleIDs = rm[e.ID]
	}
	return vo
}

func UserModify(c *gin.Context, v *UserVO, uid string) {
	ctx := context.Background()
	if v.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}
	var old SysUser
	if err := db.DB.WithContext(ctx).First(&old, "id = ?", v.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			panic(exception.NewBusinessError("数据不存在", 400))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}
	up := map[string]interface{}{}
	if v.Username != nil {
		var c int64
		db.DB.WithContext(ctx).Model(&SysUser{}).Where("username = ? AND id != ?", *v.Username, v.ID).Count(&c)
		if c > 0 {
			panic(exception.NewBusinessError("账号已存在", 400))
		}
		up["username"] = *v.Username
	}
	if v.Nickname != nil {
		up["nickname"] = *v.Nickname
	}
	if v.Avatar != nil {
		up["avatar"] = *v.Avatar
	}
	if v.Motto != nil {
		up["motto"] = *v.Motto
	}
	if v.Gender != nil {
		up["gender"] = *v.Gender
	}
	if v.Birthday != "" {
		up["birthday"] = parseDate(v.Birthday)
	}
	if v.Email != nil {
		up["email"] = *v.Email
	}
	if v.Github != nil {
		up["github"] = *v.Github
	}
	if v.Phone != nil {
		up["phone"] = *v.Phone
	}
	if v.OrgID != nil {
		up["org_id"] = *v.OrgID
	} else if old.OrgID != nil {
		up["org_id"] = nil
	}
	if v.PositionID != nil {
		up["position_id"] = *v.PositionID
	} else if old.PositionID != nil {
		up["position_id"] = nil
	}
	if v.GroupID != nil {
		up["group_id"] = *v.GroupID
	} else if old.GroupID != nil {
		up["group_id"] = nil
	}
	if v.Status != "" {
		up["status"] = v.Status
	}
	up["updated_at"] = time.Now()
	if uid != "" {
		up["updated_by"] = uid
	}
	db.DB.WithContext(ctx).Model(&SysUser{}).Where("id = ?", v.ID).Updates(up)
}

func UserRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()
	tx := db.DB.WithContext(ctx).Begin()
	tx.Where("user_id IN ?", ids).Delete(&RelUserRole{})
	tx.Where("user_id IN ?", ids).Delete(&RelUserPermission{})
	tx.Where("user_id IN ?", ids).Delete(&SysQuickAction{})
	tx.Where("id IN ?", ids).Delete(&SysUser{})
	tx.Commit()
}

func UserResetPassword(c *gin.Context, id string) {
	if id == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}
	ctx := context.Background()
	db.DB.WithContext(ctx).Model(&SysUser{}).Where("id = ?", id).Updates(map[string]interface{}{
		"password": func() string {
			h, _ := bcrypt.GenerateFromPassword([]byte("123456"), bcrypt.DefaultCost)
			return string(h)
		}(),
		"updated_at": time.Now(),
	})
}

func UserGrantRole(c *gin.Context, p *GrantRoleParam) {
	if p.UserID == "" {
		panic(exception.NewBusinessError("用户ID不能为空", 400))
	}
	ctx := context.Background()
	tx := db.DB.WithContext(ctx).Begin()
	tx.Where("user_id = ?", p.UserID).Delete(&RelUserRole{})
	seen := make(map[string]bool)
	for _, id := range p.RoleIDs {
		if !seen[id] {
			seen[id] = true
			tx.Create(&RelUserRole{ID: utils.GenerateID(), UserID: p.UserID, RoleID: id})
		}
	}
	tx.Commit()
}

func UserGrantPermission(c *gin.Context, p *GrantUserPermissionParam) {
	if p.UserID == "" {
		panic(exception.NewBusinessError("用户ID不能为空", 400))
	}
	ctx := context.Background()
	tx := db.DB.WithContext(ctx).Begin()
	tx.Where("user_id = ?", p.UserID).Delete(&RelUserPermission{})
	for _, pi := range p.Permissions {
		r := RelUserPermission{ID: utils.GenerateID(), UserID: p.UserID, PermissionCode: pi.PermissionCode, Scope: pi.Scope}
		if pi.CustomScopeGroupIds != nil {
			r.CustomScopeGroupIds = pi.CustomScopeGroupIds
		}
		if pi.CustomScopeOrgIds != nil {
			r.CustomScopeOrgIds = pi.CustomScopeOrgIds
		}
		tx.Create(&r)
	}
	tx.Commit()
}

func UserBatchImport(c *gin.Context, p *BatchImportParam) {
	if len(p.Users) == 0 {
		return
	}
	ctx := context.Background()
	now := time.Now()
	for _, u := range p.Users {
		e := SysUser{ID: utils.GenerateID(), Status: "ACTIVE", CreatedAt: &now, UpdatedAt: &now}
		if u.Username != nil {
			e.Username = u.Username
		}
		if u.Nickname != nil {
			e.Nickname = u.Nickname
		}
		if u.Phone != nil {
			e.Phone = u.Phone
		}
		if u.Email != nil {
			e.Email = u.Email
		}
		if u.Gender != nil {
			e.Gender = u.Gender
		}
		db.DB.WithContext(ctx).Create(&e)
	}
}

func UserUpdateStatus(c *gin.Context, p *UpdateStatusParam) {
	if len(p.IDs) == 0 {
		return
	}
	db.DB.WithContext(context.Background()).Model(&SysUser{}).Where("id IN ?", p.IDs).Updates(
		map[string]interface{}{"status": p.Status, "updated_at": time.Now()})
}

func UserOwnRoleIDs(c *gin.Context, uid string) []string {
	var rr []RelUserRole
	db.DB.WithContext(context.Background()).Where("user_id = ?", uid).Find(&rr)
	ids := make([]string, len(rr))
	for i, r := range rr {
		ids[i] = r.RoleID
	}
	return ids
}

func UserOwnPermissionDetails(c *gin.Context, uid string) []map[string]interface{} {
	var pp []RelUserPermission
	db.DB.WithContext(context.Background()).Where("user_id = ?", uid).Find(&pp)
	r := make([]map[string]interface{}, len(pp))
	for i, p := range pp {
		r[i] = map[string]interface{}{
			"permission_code": p.PermissionCode, "scope": p.Scope,
			"custom_scope_group_ids": p.CustomScopeGroupIds, "custom_scope_org_ids": p.CustomScopeOrgIds,
		}
	}
	return r
}

func UserExport(c *gin.Context, p *UserPageParam) []*UserVO {
	ctx := context.Background()
	q := db.DB.WithContext(ctx).Model(&SysUser{})
	if p.Keyword != "" {
		like := "%" + p.Keyword + "%"
		q = q.Where("username LIKE ? OR nickname LIKE ? OR phone LIKE ? OR email LIKE ?", like, like, like, like)
	}
	if p.Status != "" {
		q = q.Where("status = ?", p.Status)
	}
	var rows []SysUser
	q.Order("created_at DESC").Find(&rows)
	vos := make([]*UserVO, len(rows))
	for i, r := range rows {
		vos[i] = entToVO(&r)
	}
	enrichNames(vos)
	rm := batchRoleIDs(func() []string {
		ids := make([]string, len(rows))
		for i, r := range rows {
			ids[i] = r.ID
		}
		return ids
	}())
	for _, v := range vos {
		v.RoleIDs = rm[v.ID]
	}
	return vos
}

func UserUpdateProfile(c *gin.Context, uid string, p *UpdateProfileParam) {
	if uid == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	up := map[string]interface{}{}
	if p.Username != nil {
		up["username"] = *p.Username
	}
	if p.Nickname != nil {
		up["nickname"] = *p.Nickname
	}
	if p.Motto != nil {
		up["motto"] = *p.Motto
	}
	if p.Gender != nil {
		up["gender"] = *p.Gender
	}
	if p.Birthday != "" {
		up["birthday"] = parseDate(p.Birthday)
	}
	if p.Email != nil {
		up["email"] = *p.Email
	}
	if p.Github != nil {
		up["github"] = *p.Github
	}
	if p.Phone != nil {
		up["phone"] = *p.Phone
	}
	up["updated_at"] = time.Now()
	db.DB.WithContext(context.Background()).Model(&SysUser{}).Where("id = ?", uid).Updates(up)
}

func UserUpdateAvatar(c *gin.Context, uid, avatar string) {
	if uid == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	db.DB.WithContext(context.Background()).Model(&SysUser{}).Where("id = ?", uid).Update("avatar", avatar)
}

func UserUpdatePassword(c *gin.Context, uid string, p *UpdatePasswordParam) {
	if uid == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	ctx := context.Background()
	var e SysUser
	if err := db.DB.WithContext(ctx).First(&e, "id = ?", uid).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			panic(exception.NewBusinessError("用户不存在", 404))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}
	if e.Password == nil || *e.Password == "" {
		panic(exception.NewBusinessError("未设置密码，无法修改", 400))
	}
	if bcrypt.CompareHashAndPassword([]byte(*e.Password), []byte(utils.Decrypt(p.CurrentPassword))) != nil {
		panic(exception.NewBusinessError("当前密码不正确", 400))
	}
	h, _ := bcrypt.GenerateFromPassword([]byte(utils.Decrypt(p.NewPassword)), bcrypt.DefaultCost)
	db.DB.WithContext(ctx).Model(&SysUser{}).Where("id = ?", uid).Update("password", string(h))
}

type rawResource struct {
	ID            string
	ParentID      *string
	Code          string
	Name          string
	Category      string
	Type          string
	RoutePath     *string
	ComponentPath *string
	RedirectPath  *string
	Icon          *string
	Color         *string
	IsVisible     string
	IsCache       string
	IsAffix       string
	IsBreadcrumb  string
	ExternalURL   *string
	Description   *string
	SortCode      int
	Status        string
}

func UserGrantRoles(c *gin.Context, userID string, roleIDs []string, loginUserID string) {
	UserGrantRole(c, &GrantRoleParam{
		UserID:  userID,
		RoleIDs: roleIDs,
	})
}

func UserGrantPermissions(c *gin.Context, userID string, permissions []PermissionItem, loginUserID string) {
	UserGrantPermission(c, &GrantUserPermissionParam{
		UserID:      userID,
		Permissions: permissions,
	})
}

func UserOwnRoles(c *gin.Context, uid string) gin.H {
	roleIDs := UserOwnRoleIDs(c, uid)
	return gin.H{"code": 200, "message": "璇锋眰鎴愬姛", "success": true, "data": roleIDs}
}

func UserCurrent(c *gin.Context, userID string) *UserVO {
	if userID == "" {
		return nil
	}
	return UserDetail(c, userID)
}

func UserMenus(c *gin.Context, userID string) []map[string]interface{} {
	if userID == "" {
		return make([]map[string]interface{}, 0)
	}

	// Super admin: return all enabled resources
	roleIDs := UserOwnRoleIDs(c, userID)
	isSuperAdmin := false
	if len(roleIDs) > 0 {
		var roles []struct{ Code string }
		db.DB.Table("sys_role").Where("id IN ?", roleIDs).Find(&roles)
		for _, role := range roles {
			if role.Code == constants.SUPER_ADMIN_CODE {
				isSuperAdmin = true
				break
			}
		}
	}
	if isSuperAdmin {
		var resources []rawResource
		db.DB.Table("sys_resource").Where("status = ?", string(enums.StatusEnabled)).Order("sort_code ASC").Find(&resources)
		cm := make(map[string][]rawResource)
		for _, r := range resources {
			pid := ""
			if r.ParentID != nil && *r.ParentID != "" {
				pid = *r.ParentID
			}
			cm[pid] = append(cm[pid], r)
		}
		return buildUserMenuTree(cm, "")
	}

	if len(roleIDs) == 0 {
		return make([]map[string]interface{}, 0)
	}

	var rr []RelRoleResource
	db.DB.Where("role_id IN ?", roleIDs).Find(&rr)
	if len(rr) == 0 {
		return make([]map[string]interface{}, 0)
	}

	resourceIDs := make([]string, len(rr))
	for i, r := range rr {
		resourceIDs[i] = r.ResourceID
	}

	var resources []rawResource
	db.DB.Table("sys_resource").Where("id IN ? AND status = ?", resourceIDs, string(enums.StatusEnabled)).Order("sort_code ASC").Find(&resources)

	cm := make(map[string][]rawResource)
	for _, r := range resources {
		pid := ""
		if r.ParentID != nil && *r.ParentID != "" {
			pid = *r.ParentID
		}
		cm[pid] = append(cm[pid], r)
	}
	return buildUserMenuTree(cm, "")
}

func buildUserMenuTree(cm map[string][]rawResource, pid string) []map[string]interface{} {
	cs := cm[pid]
	r := make([]map[string]interface{}, 0, len(cs))
	for _, c := range cs {
		n := map[string]interface{}{
			"id": c.ID, "code": c.Code, "name": c.Name, "category": c.Category, "type": c.Type,
			"route_path": c.RoutePath, "component_path": c.ComponentPath, "redirect_path": c.RedirectPath,
			"icon": c.Icon, "color": c.Color, "is_visible": c.IsVisible, "is_cache": c.IsCache,
			"is_affix": c.IsAffix, "is_breadcrumb": c.IsBreadcrumb, "external_url": c.ExternalURL,
			"sort_code": c.SortCode, "status": c.Status,
		}
		if c.ParentID != nil {
			n["parent_id"] = *c.ParentID
		} else {
			n["parent_id"] = nil
		}
		if c.Description != nil {
			n["description"] = *c.Description
		}
		n["children"] = buildUserMenuTree(cm, c.ID)
		r = append(r, n)
	}
	return r
}

func UserPermissions(c *gin.Context, userID string) []string {
	if userID == "" {
		return make([]string, 0)
	}

	roleIDs := UserOwnRoleIDs(c, userID)
	var permCodes []string

	if len(roleIDs) > 0 {
		var rp []RelRolePermission
		db.DB.Where("role_id IN ?", roleIDs).Select("DISTINCT permission_code").Find(&rp)
		for _, p := range rp {
			permCodes = append(permCodes, p.PermissionCode)
		}
	}

	var up []RelUserPermission
	db.DB.Where("user_id = ?", userID).Select("DISTINCT permission_code").Find(&up)
	for _, p := range up {
		permCodes = append(permCodes, p.PermissionCode)
	}

	return permCodes
}


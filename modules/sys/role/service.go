package role

import (
	"context"
	"encoding/json"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	userModel "hei-gin/modules/sys/user"

	"github.com/gin-gonic/gin"
)

func entToVO(entity *SysRole) *RoleVO {
	if entity == nil { return nil }
	return &RoleVO{
		ID: entity.ID, Code: entity.Code, Name: entity.Name, Category: entity.Category,
		Description: entity.Description, Status: entity.Status, SortCode: entity.SortCode,
		Extra: entity.Extra, CreatedAt: fmtTime(entity.CreatedAt),
		CreatedBy: entity.CreatedBy, UpdatedAt: fmtTime(entity.UpdatedAt),
		UpdatedBy: entity.UpdatedBy,
	}
}

func fmtTime(t *time.Time) string { if t == nil { return "" }; return t.Format("2006-01-02 15:04:05") }

func RolePage(c *gin.Context, param *RolePageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 { param.Current = 1 }
	if param.Size < 1 { param.Size = 10 }
	if param.Size > 100 { param.Size = 100 }

	var total int64
	db.DB.WithContext(ctx).Model(&SysRole{}).Count(&total)

	var records []SysRole
	db.DB.WithContext(ctx).Order("created_at DESC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)

	vos := make([]*RoleVO, 0, len(records))
	for _, r := range records { vos = append(vos, entToVO(&r)) }
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func RoleCreate(c *gin.Context, vo *RoleVO, userID string) {
	ctx := context.Background()
	now := time.Now()
	e := SysRole{ID: utils.GenerateID(), Code: vo.Code, Name: vo.Name, Category: vo.Category, SortCode: vo.SortCode, Status: "ENABLED", CreatedAt: &now, UpdatedAt: &now}
	if vo.Description != nil { e.Description = vo.Description }
	if vo.Status != "" { e.Status = vo.Status }
	if vo.Extra != nil { e.Extra = vo.Extra }
	if userID != "" { e.CreatedBy = &userID; e.UpdatedBy = &userID }
	if err := db.DB.WithContext(ctx).Create(&e).Error; err != nil { panic(exception.NewBusinessError("添加角色失败: "+err.Error(), 500)) }
}

func RoleModify(c *gin.Context, vo *RoleVO, userID string) {
	ctx := context.Background()
	if vo.ID == "" { panic(exception.NewBusinessError("ID不能为空", 400)) }

	var e SysRole
	if err := db.DB.WithContext(ctx).First(&e, "id = ?", vo.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound { panic(exception.NewBusinessError("数据不存在", 400)) }
		panic(exception.NewBusinessError("查询角色失败: "+err.Error(), 500))
	}

	up := map[string]interface{}{"code": vo.Code, "name": vo.Name, "category": vo.Category, "sort_code": vo.SortCode, "updated_at": time.Now()}
	if vo.Description != nil { up["description"] = *vo.Description }
	if vo.Status != "" { up["status"] = vo.Status }
	if vo.Extra != nil { up["extra"] = *vo.Extra }
	if userID != "" { up["updated_by"] = userID }
	if err := db.DB.WithContext(ctx).Model(&SysRole{}).Where("id = ?", vo.ID).Updates(up).Error; err != nil { panic(exception.NewBusinessError("编辑角色失败: "+err.Error(), 500)) }
}

func RoleRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	ctx := context.Background()

	var cnt int64
	db.DB.WithContext(ctx).Model(&userModel.RelUserRole{}).Where("role_id IN ?", ids).Count(&cnt)
	if cnt > 0 { panic(exception.NewBusinessError("角色存在关联用户，无法删除", 400)) }

	tx := db.DB.WithContext(ctx).Begin()
	tx.Where("role_id IN ?", ids).Delete(&userModel.RelRolePermission{})
	tx.Where("role_id IN ?", ids).Delete(&userModel.RelRoleResource{})
	tx.Where("role_id IN ?", ids).Delete(&userModel.RelUserRole{})
	tx.Where("id IN ?", ids).Delete(&SysRole{})
	if err := tx.Commit().Error; err != nil { panic(exception.NewBusinessError("提交事务失败: "+err.Error(), 500)) }
}

func RoleDetail(c *gin.Context, id string) *RoleVO {
	if id == "" { return nil }
	ctx := context.Background()
	var e SysRole
	if err := db.DB.WithContext(ctx).First(&e, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound { return nil }
		panic(exception.NewBusinessError("查询角色详情失败: "+err.Error(), 500))
	}
	return entToVO(&e)
}

func RoleAssignResource(c *gin.Context, roleID string, resourceIDs []string) {
	if roleID == "" { panic(exception.NewBusinessError("角色ID不能为空", 400)) }
	ctx := context.Background()

	uIDs := make([]string, 0)
	seen := make(map[string]bool)
	for _, id := range resourceIDs { if !seen[id] { seen[id] = true; uIDs = append(uIDs, id) } }

	tx := db.DB.WithContext(ctx).Begin()
	tx.Where("role_id = ?", roleID).Delete(&userModel.RelRoleResource{})
	for _, id := range uIDs { tx.Create(&userModel.RelRoleResource{ID: utils.GenerateID(), RoleID: roleID, ResourceID: id}) }

	type rr struct{ ID string; Extra *string }
	var res []rr
	tx.Table("sys_resource").Where("id IN ? AND extra IS NOT NULL AND extra != ''", uIDs).Find(&res)

	var existingPerms []userModel.RelRolePermission
	tx.Where("role_id = ?", roleID).Select("permission_code").Find(&existingPerms)
	epm := make(map[string]bool)
	for _, p := range existingPerms { epm[p.PermissionCode] = true }

	for _, r := range res {
		if r.Extra == nil || *r.Extra == "" { continue }
		var em map[string]interface{}
		if err := json.Unmarshal([]byte(*r.Extra), &em); err != nil { continue }
		pc, ok := em["permission_code"].(string)
		if !ok || pc == "" || epm[pc] { continue }
		tx.Create(&userModel.RelRolePermission{ID: utils.GenerateID(), RoleID: roleID, PermissionCode: pc, Scope: "ALL"})
	}
	if err := tx.Commit().Error; err != nil { panic(exception.NewBusinessError("提交事务失败: "+err.Error(), 500)) }
}

func RoleAssignPermission(c *gin.Context, roleID string, permissions []userModel.PermissionItem) {
	if roleID == "" { panic(exception.NewBusinessError("角色ID不能为空", 400)) }
	ctx := context.Background()

	tx := db.DB.WithContext(ctx).Begin()
	tx.Where("role_id = ?", roleID).Delete(&userModel.RelRolePermission{})
	for _, p := range permissions {
		r := userModel.RelRolePermission{ID: utils.GenerateID(), RoleID: roleID, PermissionCode: p.PermissionCode, Scope: p.Scope}
		if p.CustomScopeGroupIds != nil { r.CustomScopeGroupIds = p.CustomScopeGroupIds }
		if p.CustomScopeOrgIds != nil { r.CustomScopeOrgIds = p.CustomScopeOrgIds }
		tx.Create(&r)
	}
	if err := tx.Commit().Error; err != nil { panic(exception.NewBusinessError("提交事务失败: "+err.Error(), 500)) }
}

func RoleOwnPermissionCodes(c *gin.Context, roleID string) []string {
	ctx := context.Background()
	var perms []userModel.RelRolePermission
	db.DB.WithContext(ctx).Where("role_id = ?", roleID).Select("permission_code").Find(&perms)
	codes := make([]string, len(perms))
	for i, p := range perms { codes[i] = p.PermissionCode }
	return codes
}

func RoleOwnPermissionDetails(c *gin.Context, roleID string) []map[string]interface{} {
	ctx := context.Background()
	var perms []userModel.RelRolePermission
	db.DB.WithContext(ctx).Where("role_id = ?", roleID).Find(&perms)
	r := make([]map[string]interface{}, len(perms))
	for i, p := range perms {
		r[i] = map[string]interface{}{"permission_code": p.PermissionCode, "scope": p.Scope, "custom_scope_group_ids": p.CustomScopeGroupIds, "custom_scope_org_ids": p.CustomScopeOrgIds}
	}
	return r
}

func RoleOwnResourceIDs(c *gin.Context, roleID string) []string {
	ctx := context.Background()
	var resources []userModel.RelRoleResource
	db.DB.WithContext(ctx).Where("role_id = ?", roleID).Select("resource_id").Find(&resources)
	ids := make([]string, len(resources))
	for i, r := range resources { ids[i] = r.ResourceID }
	return ids
}

func RoleGrantPermissions(c *gin.Context, roleID string, permissions []PermissionItem, userID string) {
	// Convert PermissionItem to userModel.PermissionItem
	items := make([]userModel.PermissionItem, len(permissions))
	for i, p := range permissions {
		items[i] = userModel.PermissionItem{
			PermissionCode:      p.PermissionCode,
			Scope:               p.Scope,
			CustomScopeGroupIds: p.CustomScopeGroupIds,
			CustomScopeOrgIds:   p.CustomScopeOrgIds,
		}
	}
	RoleAssignPermission(c, roleID, items)
}

func RoleGrantResources(c *gin.Context, roleID string, resourceIDs []string, permissions []ButtonPermissionScope) {
	RoleAssignResource(c, roleID, resourceIDs)
}

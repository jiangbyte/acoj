package role

import (
	"context"
	"errors"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/relrolepermission"
	"hei-gin/ent/gen/relroleresource"
	"hei-gin/ent/gen/reluserrole"
	"hei-gin/ent/gen/syspermission"
	"hei-gin/ent/gen/sysrole"
)

type PageParam struct {
	Page    int    `form:"page" json:"page"`
	Size    int    `form:"size" json:"size"`
	Keyword string `form:"keyword" json:"keyword"`
	Status  string `form:"status" json:"status"`
}

type RoleVO struct {
	ID                  string `json:"id"`
	Name                string `json:"name"`
	Code                string `json:"code"`
	Category            string `json:"category"`
	Extra               string `json:"extra"`
	DataScope           string `json:"data_scope"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	SortCode            int    `json:"sort_code"`
	Status              string `json:"status"`
	Description         string `json:"description"`
	CreatedAt           string `json:"created_at"`
	CreatedBy           string `json:"created_by"`
	UpdatedAt           string `json:"updated_at"`
	UpdatedBy           string `json:"updated_by"`
}

type RoleCreateReq struct {
	Name                string `json:"name" binding:"required"`
	Code                string `json:"code" binding:"required"`
	Category            string `json:"category"`
	DataScope           string `json:"data_scope"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	SortCode            int    `json:"sort_code"`
	Status              string `json:"status"`
	Description         string `json:"description"`
}

type RoleModifyReq struct {
	ID                  string `json:"id" binding:"required"`
	Name                string `json:"name"`
	Code                string `json:"code"`
	Category            string `json:"category"`
	DataScope           string `json:"data_scope"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	SortCode            int    `json:"sort_code"`
	Status              string `json:"status"`
	Description         string `json:"description"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

type GrantResourceReq struct {
	RoleID      string   `json:"role_id" binding:"required"`
	ResourceIDs []string `json:"resource_ids" binding:"required"`
}

type GrantPermissionReq struct {
	RoleID      string           `json:"role_id" binding:"required"`
	Permissions []PermissionItem `json:"permissions" binding:"required"`
}

// PermissionItem represents a permission with optional scope info.
type PermissionItem struct {
	PermissionCode      string `json:"permission_code" binding:"required"`
	Scope               string `json:"scope"`
	CustomScopeGroupIds string `json:"custom_scope_group_ids"`
	CustomScopeOrgIds   string `json:"custom_scope_org_ids"`
}

func toVO(r *ent.SysRole) RoleVO {
	vo := RoleVO{
		ID:                  r.ID,
		Name:                r.Name,
		Code:                r.Code,
		Category:            r.Category,
		Extra:               r.Extra,
		DataScope:           r.DataScope,
		CustomScopeOrgIds:   r.CustomScopeOrgIds,
		CustomScopeGroupIds: r.CustomScopeGroupIds,
		SortCode:            r.SortCode,
		Status:              r.Status,
		Description:         r.Description,
		CreatedAt:           r.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:           r.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:           r.CreatedBy,
		UpdatedBy:           r.UpdatedBy,
	}
	return vo
}

func Page(page, size int, keyword, status string) (int, []*ent.SysRole, error) {
	ctx := context.Background()
	q := db.Client.SysRole.Query()

	if keyword != "" {
		q = q.Where(
			sysrole.Or(
				sysrole.NameContains(keyword),
				sysrole.CodeContains(keyword),
			),
		)
	}
	if status != "" {
		q = q.Where(sysrole.StatusEQ(status))
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
		Order(ent.Desc(sysrole.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Create(req *RoleCreateReq, loginID string) (*ent.SysRole, error) {
	ctx := context.Background()
	now := time.Now()
	q := db.Client.SysRole.Create().
		SetID(utils.NextID()).
		SetName(req.Name).
		SetCode(req.Code).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)

	if req.Category != "" {
		q.SetCategory(req.Category)
	}
	if req.DataScope != "" {
		q.SetDataScope(req.DataScope)
	} else {
		q.SetDataScope("ALL")
	}
	if req.CustomScopeOrgIds != "" {
		q.SetCustomScopeOrgIds(req.CustomScopeOrgIds)
	}
	if req.CustomScopeGroupIds != "" {
		q.SetCustomScopeGroupIds(req.CustomScopeGroupIds)
	}
	if req.SortCode > 0 {
		q.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		q.SetStatus(req.Status)
	} else {
		q.SetStatus("ENABLED")
	}
	if req.Description != "" {
		q.SetDescription(req.Description)
	}

	return q.Save(ctx)
}

func Modify(req *RoleModifyReq, loginID string) (*ent.SysRole, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysRole.UpdateOneID(req.ID)

	if req.Name != "" {
		u.SetName(req.Name)
	}
	if req.Code != "" {
		u.SetCode(req.Code)
	}
	if req.Category != "" {
		u.SetCategory(req.Category)
	}
	if req.DataScope != "" {
		u.SetDataScope(req.DataScope)
	}
	if req.CustomScopeOrgIds != "" {
		u.SetCustomScopeOrgIds(req.CustomScopeOrgIds)
	}
	if req.CustomScopeGroupIds != "" {
		u.SetCustomScopeGroupIds(req.CustomScopeGroupIds)
	}
	if req.SortCode > 0 {
		u.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()

	// Check for user associations
	userCount, err := db.Client.RelUserRole.Query().Where(reluserrole.RoleIDIn(ids...)).Count(ctx)
	if err != nil {
		return err
	}
	if userCount > 0 {
		return errors.New("存在关联用户，无法删除")
	}

	// Clean up rel_role_permission
	_, err = db.Client.RelRolePermission.Delete().Where(relrolepermission.RoleIDIn(ids...)).Exec(ctx)
	if err != nil {
		return err
	}

	// Clean up rel_role_resource
	_, err = db.Client.RelRoleResource.Delete().Where(relroleresource.RoleIDIn(ids...)).Exec(ctx)
	if err != nil {
		return err
	}

	// Delete roles
	_, err = db.Client.SysRole.Delete().Where(sysrole.IDIn(ids...)).Exec(ctx)
	return err
}

func Detail(id string) (*ent.SysRole, error) {
	ctx := context.Background()
	return db.Client.SysRole.Get(ctx, id)
}

// OwnResources queries rel_role_resource for resource IDs assigned to a role.
func OwnResources(roleID string) ([]string, error) {
	ctx := context.Background()
	rels, err := db.Client.RelRoleResource.Query().
		Where(relroleresource.RoleIDEQ(roleID)).
		Select(relroleresource.FieldResourceID).
		All(ctx)
	if err != nil {
		return nil, err
	}
	ids := make([]string, len(rels))
	for i, r := range rels {
		ids[i] = r.ResourceID
	}
	return ids, nil
}

// GrantResource deletes old rel_role_resource records and inserts new ones.
func GrantResource(roleID string, resourceIDs []string) error {
	ctx := context.Background()
	tx, err := db.Client.Tx(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.RelRoleResource.Delete().Where(relroleresource.RoleIDEQ(roleID)).Exec(ctx)
	if err != nil {
		return err
	}

	for _, resourceID := range resourceIDs {
		_, err = tx.RelRoleResource.Create().
			SetID(utils.NextID()).
			SetRoleID(roleID).
			SetResourceID(resourceID).
			Save(ctx)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

// OwnPermissions queries rel_role_permission for permission codes assigned to a role.
func OwnPermissions(roleID string) ([]string, error) {
	ctx := context.Background()
	perms, err := db.Client.RelRolePermission.Query().
		Where(relrolepermission.RoleIDEQ(roleID)).
		Select(relrolepermission.FieldPermissionCode).
		All(ctx)
	if err != nil {
		return nil, err
	}
	codes := make([]string, len(perms))
	for i, p := range perms {
		codes[i] = p.PermissionCode
	}
	return codes, nil
}

// GrantPermission deletes old rel_role_permission records and inserts new ones with scope.
func GrantPermission(roleID string, permissions []PermissionItem) error {
	ctx := context.Background()
	tx, err := db.Client.Tx(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.RelRolePermission.Delete().Where(relrolepermission.RoleIDEQ(roleID)).Exec(ctx)
	if err != nil {
		return err
	}

	for _, p := range permissions {
		scope := p.Scope
		if scope == "" {
			scope = "ALL"
		}
		_, err = tx.RelRolePermission.Create().
			SetID(utils.NextID()).
			SetRoleID(roleID).
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

func OwnPermissionDetail(roleID string) (interface{}, error) {
	ctx := context.Background()

	// Query rel_role_permission to get permission codes with scope info
	rpList, err := db.Client.RelRolePermission.Query().
		Where(relrolepermission.RoleIDEQ(roleID)).
		All(ctx)
	if err != nil {
		return nil, err
	}

	if len(rpList) == 0 {
		return []map[string]interface{}{}, nil
	}

	// Collect permission codes and query sys_permission for display info
	codes := make([]string, len(rpList))
	codeMap := make(map[string]*ent.RelRolePermission)
	for i, rp := range rpList {
		codes[i] = rp.PermissionCode
		codeMap[rp.PermissionCode] = rp
	}

	perms, _ := db.Client.SysPermission.Query().
		Where(syspermission.CodeIn(codes...)).
		Select(syspermission.FieldID, syspermission.FieldCode, syspermission.FieldName).
		All(ctx)

	permInfo := make(map[string]*ent.SysPermission)
	for _, p := range perms {
		permInfo[p.Code] = p
	}

	var result []map[string]interface{}
	for _, code := range codes {
		rp := codeMap[code]
		row := map[string]interface{}{
			"permission_code": rp.PermissionCode,
			"scope":           rp.Scope,
		}
		if p, ok := permInfo[code]; ok {
			row["id"] = p.ID
			row["code"] = p.Code
			row["name"] = p.Name
		}
		if rp.CustomScopeOrgIds != "" {
			row["custom_scope_org_ids"] = rp.CustomScopeOrgIds
		}
		if rp.CustomScopeGroupIds != "" {
			row["custom_scope_group_ids"] = rp.CustomScopeGroupIds
		}
		result = append(result, row)
	}

	return result, nil
}

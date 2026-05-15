package role

import (
	"context"
	"database/sql"
	"errors"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/relrolepermission"
	"hei-gin/ent/relroleresource"
	"hei-gin/ent/reluserrole"
	"hei-gin/ent/sysrole"
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
	RoleID          string   `json:"role_id" binding:"required"`
	PermissionCodes []string `json:"permission_codes" binding:"required"`
}

var RoleExportFieldNames = map[string]string{
	"name":        "角色名称",
	"code":        "角色编码",
	"data_scope":  "数据权限范围",
	"sort_code":   "排序",
	"status":      "状态",
	"description": "角色描述",
	"created_at":  "创建时间",
}

var RoleExportFields = []string{"name", "code", "data_scope", "sort_code", "status", "description", "created_at"}

func toVO(r *ent.SysRole) RoleVO {
	vo := RoleVO{
		ID:                  r.ID,
		Name:                r.Name,
		Code:                r.Code,
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

func QueryAll() ([]*ent.SysRole, error) {
	ctx := context.Background()
	return db.Client.SysRole.Query().Order(ent.Desc(sysrole.FieldCreatedAt)).All(ctx)
}

// OwnResources queries rel_role_resource for resource IDs assigned to a role.
func OwnResources(roleID string) ([]string, error) {
	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx, "SELECT resource_id FROM rel_role_resource WHERE role_id = ?", roleID)
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
	if ids == nil {
		ids = []string{}
	}
	return ids, rows.Err()
}

// GrantResource deletes old rel_role_resource records and inserts new ones.
func GrantResource(roleID string, resourceIDs []string) error {
	ctx := context.Background()
	tx, err := db.RawDB.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.ExecContext(ctx, "DELETE FROM rel_role_resource WHERE role_id = ?", roleID)
	if err != nil {
		return err
	}

	for _, resourceID := range resourceIDs {
		_, err = tx.ExecContext(ctx, "INSERT INTO rel_role_resource (id, role_id, resource_id) VALUES (?, ?, ?)", utils.NextID(), roleID, resourceID)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

// OwnPermissions queries rel_role_permission for permission codes assigned to a role.
func OwnPermissions(roleID string) ([]string, error) {
	ctx := context.Background()
	rows, err := db.RawDB.QueryContext(ctx, "SELECT permission_code FROM rel_role_permission WHERE role_id = ?", roleID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var codes []string
	for rows.Next() {
		var code string
		if err := rows.Scan(&code); err != nil {
			return nil, err
		}
		codes = append(codes, code)
	}
	if codes == nil {
		codes = []string{}
	}
	return codes, rows.Err()
}

// GrantPermission deletes old rel_role_permission records and inserts new ones.
func GrantPermission(roleID string, permissionCodes []string) error {
	ctx := context.Background()
	tx, err := db.RawDB.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	_, err = tx.ExecContext(ctx, "DELETE FROM rel_role_permission WHERE role_id = ?", roleID)
	if err != nil {
		return err
	}

	for _, code := range permissionCodes {
		_, err = tx.ExecContext(ctx, "INSERT INTO rel_role_permission (id, role_id, permission_code) VALUES (?, ?, ?)", utils.NextID(), roleID, code)
		if err != nil {
			return err
		}
	}

	return tx.Commit()
}

func OwnPermissionDetail(roleID string) (interface{}, error) {
	ctx := context.Background()

	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT p.id, p.code, p.name, p.module, p.scope, p.custom_scope_org_ids, p.custom_scope_group_ids
		 FROM sys_permission p
		 INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
		 WHERE rp.role_id = ?`, roleID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []map[string]interface{}
	for rows.Next() {
		var id, code, name, module string
		var scope sql.NullString
		var customScopeOrgIds, customScopeGroupIds sql.NullString
		if err := rows.Scan(&id, &code, &name, &module, &scope, &customScopeOrgIds, &customScopeGroupIds); err != nil {
			continue
		}
		row := map[string]interface{}{
			"id":     id,
			"code":   code,
			"name":   name,
			"module": module,
			"scope":  scope.String,
		}
		if customScopeOrgIds.String != "" {
			row["custom_scope_org_ids"] = customScopeOrgIds.String
		}
		if customScopeGroupIds.String != "" {
			row["custom_scope_group_ids"] = customScopeGroupIds.String
		}
		result = append(result, row)
	}
	if result == nil {
		result = []map[string]interface{}{}
	}
	return result, nil
}

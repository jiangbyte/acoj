package resource

import (
	"context"
	"encoding/json"
	"log"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/enums"
	"hei-gin/core/utils"
)

func ModulePage(c *gin.Context, param *ModulePageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 { param.Current = 1 }
	if param.Size < 1 { param.Size = 10 }
	if param.Size > 100 { param.Size = 100 }

	var total int64
	db.DB.WithContext(ctx).Model(&SysModule{}).Count(&total)

	var records []SysModule
	db.DB.WithContext(ctx).Order("created_at DESC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)
	return result.PageDataResult(c, records, total, param.Current, param.Size)
}

func ModuleDetail(c *gin.Context, id string) *SysModule {
	if id == "" { return nil }
	ctx := context.Background()
	var entity SysModule
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil { return nil }
	return &entity
}

func ModuleCreate(c *gin.Context, vo *ModuleVO, userID string) {
	ctx := context.Background()
	now := time.Now()
	e := SysModule{ID: utils.GenerateID(), Code: vo.Code, Name: vo.Name, Category: vo.Category, CreatedAt: &now, UpdatedAt: &now}
	if vo.Icon != nil { e.Icon = vo.Icon }
	if vo.Color != nil { e.Color = vo.Color }
	if vo.Description != nil { e.Description = vo.Description }
	if vo.SortCode != 0 { e.SortCode = vo.SortCode }
	if vo.IsVisible != "" { e.IsVisible = vo.IsVisible }
	if vo.Status != "" { e.Status = vo.Status }
	if userID != "" { e.CreatedBy = &userID; e.UpdatedBy = &userID }
	if err := db.DB.WithContext(ctx).Create(&e).Error; err != nil { panic(exception.NewBusinessError("添加模块失败: "+err.Error(), 500)) }
}

func ModuleModify(c *gin.Context, vo *ModuleVO, userID string) {
	ctx := context.Background()
	var entity SysModule
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", vo.ID).Error; err != nil { panic(exception.NewBusinessError("模块不存在", 400)) }

	up := map[string]interface{}{"code": vo.Code, "name": vo.Name, "category": vo.Category, "updated_at": time.Now()}
	if userID != "" { up["updated_by"] = userID }
	if vo.SortCode != 0 { up["sort_code"] = vo.SortCode } else { up["sort_code"] = 0 }
	if vo.Icon != nil { up["icon"] = *vo.Icon } else { up["icon"] = nil }
	if vo.Color != nil { up["color"] = *vo.Color } else { up["color"] = nil }
	if vo.Description != nil { up["description"] = *vo.Description } else { up["description"] = nil }
	if vo.IsVisible != "" { up["is_visible"] = vo.IsVisible }
	if vo.Status != "" { up["status"] = vo.Status }
	if err := db.DB.WithContext(ctx).Model(&SysModule{}).Where("id = ?", vo.ID).Updates(up).Error; err != nil { panic(exception.NewBusinessError("编辑模块失败: "+err.Error(), 500)) }
}

func ModuleRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	if err := db.DB.WithContext(context.Background()).Where("id IN ?", ids).Delete(&SysModule{}).Error; err != nil { panic(exception.NewBusinessError("删除模块失败: "+err.Error(), 500)) }
}

func ResourcePage(c *gin.Context, param *ResourcePageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 { param.Current = 1 }
	if param.Size < 1 { param.Size = 10 }
	if param.Size > 100 { param.Size = 100 }

	var total int64
	db.DB.WithContext(ctx).Model(&SysResource{}).Count(&total)

	var records []SysResource
	db.DB.WithContext(ctx).Order("sort_code ASC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)
	return result.PageDataResult(c, records, total, param.Current, param.Size)
}

func ResourceTree(c *gin.Context, category string) []map[string]interface{} {
	ctx := context.Background()
	q := db.DB.WithContext(ctx).Model(&SysResource{}).Order("sort_code ASC")
	if category != "" { q = q.Where("category = ?", category) }
	var all []SysResource
	q.Find(&all)

	cm := make(map[string][]SysResource)
	for _, r := range all {
		pid := ""; if r.ParentID != nil && *r.ParentID != "" && *r.ParentID != "0" { pid = *r.ParentID }
		cm[pid] = append(cm[pid], r)
	}
	return buildRT(cm, "")
}

func buildRT(cm map[string][]SysResource, pid string) []map[string]interface{} {
	cs := cm[pid]
	r := make([]map[string]interface{}, 0, len(cs))
	for _, c := range cs {
		n := resToNode(&c)
		n["children"] = buildRT(cm, c.ID)
		r = append(r, n)
	}
	return r
}

func resToNode(r *SysResource) map[string]interface{} {
	n := map[string]interface{}{
		"id": r.ID, "code": r.Code, "name": r.Name, "category": r.Category, "type": r.Type,
		"route_path": r.RoutePath, "component_path": r.ComponentPath, "redirect_path": r.RedirectPath,
		"icon": r.Icon, "color": r.Color, "is_visible": r.IsVisible, "is_cache": r.IsCache,
		"is_affix": r.IsAffix, "is_breadcrumb": r.IsBreadcrumb, "external_url": r.ExternalURL,
		"sort_code": r.SortCode, "status": r.Status,
	}
	if r.ParentID != nil { n["parent_id"] = *r.ParentID } else { n["parent_id"] = nil }
	if r.Description != nil { n["description"] = *r.Description }
	if r.Extra != nil { n["extra"] = *r.Extra }
	if r.CreatedAt != nil { n["created_at"] = r.CreatedAt.Format("2006-01-02 15:04:05") }
	if r.CreatedBy != nil { n["created_by"] = *r.CreatedBy }
	if r.UpdatedAt != nil { n["updated_at"] = r.UpdatedAt.Format("2006-01-02 15:04:05") }
	if r.UpdatedBy != nil { n["updated_by"] = *r.UpdatedBy }
	return n
}

func ResourceCreate(c *gin.Context, vo *ResourceVO, userID string) {
	ctx := context.Background()
	now := time.Now()
	e := SysResource{
		ID: utils.GenerateID(), Code: vo.Code, Name: vo.Name, Category: vo.Category,
		Type: vo.Type, SortCode: vo.SortCode, Status: string(enums.StatusEnabled), CreatedAt: &now, UpdatedAt: &now,
	}
	if vo.ParentID != nil { e.ParentID = vo.ParentID }
	if vo.Description != nil { e.Description = vo.Description }
	if vo.RoutePath != nil { e.RoutePath = vo.RoutePath }
	if vo.ComponentPath != nil { e.ComponentPath = vo.ComponentPath }
	if vo.RedirectPath != nil { e.RedirectPath = vo.RedirectPath }
	if vo.Icon != nil { e.Icon = vo.Icon }
	if vo.Color != nil { e.Color = vo.Color }
	if vo.IsVisible != "" { e.IsVisible = vo.IsVisible }
	if vo.IsCache != "" { e.IsCache = vo.IsCache }
	if vo.IsAffix != "" { e.IsAffix = vo.IsAffix }
	if vo.IsBreadcrumb != "" { e.IsBreadcrumb = vo.IsBreadcrumb }
	if vo.ExternalURL != nil { e.ExternalURL = vo.ExternalURL }
	if vo.Extra != nil { e.Extra = vo.Extra }
	if vo.Status != "" { e.Status = vo.Status }
	if userID != "" { e.CreatedBy = &userID; e.UpdatedBy = &userID }
	if err := db.DB.WithContext(ctx).Create(&e).Error; err != nil { panic(exception.NewBusinessError("添加资源失败: "+err.Error(), 500)) }
}

func ResourceModify(c *gin.Context, vo *ResourceVO, userID string) {
	ctx := context.Background()
	var old SysResource
	if err := db.DB.WithContext(ctx).First(&old, "id = ?", vo.ID).Error; err != nil { panic(exception.NewBusinessError("资源不存在", 400)) }

	oldExtra := old.Extra
	up := map[string]interface{}{
		"code": vo.Code, "name": vo.Name, "category": vo.Category, "type": vo.Type,
		"sort_code": vo.SortCode, "updated_at": time.Now(),
	}
	if userID != "" { up["updated_by"] = userID }
	if vo.ParentID != nil { up["parent_id"] = *vo.ParentID } else { up["parent_id"] = nil }
	if vo.Description != nil { up["description"] = *vo.Description } else { up["description"] = nil }
	if vo.RoutePath != nil { up["route_path"] = *vo.RoutePath } else { up["route_path"] = nil }
	if vo.ComponentPath != nil { up["component_path"] = *vo.ComponentPath } else { up["component_path"] = nil }
	if vo.RedirectPath != nil { up["redirect_path"] = *vo.RedirectPath } else { up["redirect_path"] = nil }
	if vo.Icon != nil { up["icon"] = *vo.Icon } else { up["icon"] = nil }
	if vo.Color != nil { up["color"] = *vo.Color } else { up["color"] = nil }
	if vo.IsVisible != "" { up["is_visible"] = vo.IsVisible }
	if vo.IsCache != "" { up["is_cache"] = vo.IsCache }
	if vo.IsAffix != "" { up["is_affix"] = vo.IsAffix }
	if vo.IsBreadcrumb != "" { up["is_breadcrumb"] = vo.IsBreadcrumb }
	if vo.ExternalURL != nil { up["external_url"] = *vo.ExternalURL } else { up["external_url"] = nil }
	if vo.Extra != nil { up["extra"] = *vo.Extra } else { up["extra"] = nil }
	if vo.Status != "" { up["status"] = vo.Status }

	if err := db.DB.WithContext(ctx).Model(&SysResource{}).Where("id = ?", vo.ID).Updates(up).Error; err != nil { panic(exception.NewBusinessError("编辑资源失败: "+err.Error(), 500)) }

	if vo.Extra != nil || oldExtra != nil { syncPerm(vo.ID, oldExtra, vo.Extra) }
}

func ResourceRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	ctx := context.Background()
	all := collectDescendant(ids)

	tx := db.DB.WithContext(ctx).Begin()
	tx.Exec("DELETE FROM rel_role_resource WHERE resource_id IN ?", all)
	tx.Exec("DELETE FROM rel_role_permission WHERE role_id IN (SELECT role_id FROM rel_role_resource WHERE resource_id IN ?)", all)
	tx.Where("id IN ?", all).Delete(&SysResource{})
	if err := tx.Commit().Error; err != nil { panic(exception.NewBusinessError("提交事务失败: "+err.Error(), 500)) }
}

func ResourceDetail(c *gin.Context, id string) *ResourceVO {
	if id == "" { return nil }
	ctx := context.Background()
	var e SysResource
	if err := db.DB.WithContext(ctx).First(&e, "id = ?", id).Error; err != nil { return nil }
	return resFromEnt(&e)
}

func ResourceMenu(c *gin.Context) []map[string]interface{} {
	ctx := context.Background()
	var all []SysResource
	db.DB.WithContext(ctx).Model(&SysResource{}).Order("sort_code ASC").Find(&all)
	cm := make(map[string][]SysResource)
	for _, r := range all {
		pid := ""; if r.ParentID != nil && *r.ParentID != "" && *r.ParentID != "0" { pid = *r.ParentID }
		cm[pid] = append(cm[pid], r)
	}

	roots := cm[""]
	r := make([]map[string]interface{}, 0, len(roots))
	for _, rt := range roots {
		n := menuNode(&rt)
		n["children"] = buildMenuTree(cm, rt.ID)
		r = append(r, n)
	}
	return r
}

func buildMenuTree(cm map[string][]SysResource, pid string) []map[string]interface{} {
	cs := cm[pid]
	if len(cs) == 0 { return []map[string]interface{}{} }
	r := make([]map[string]interface{}, 0, len(cs))
	for _, c := range cs {
		n := menuNode(&c)
		n["children"] = buildMenuTree(cm, c.ID)
		r = append(r, n)
	}
	return r
}

func menuNode(r *SysResource) map[string]interface{} {
	n := map[string]interface{}{
		"id": r.ID, "code": r.Code, "name": r.Name, "type": r.Type, "category": r.Category,
		"route_path": r.RoutePath, "component_path": r.ComponentPath, "redirect_path": r.RedirectPath,
		"icon": r.Icon, "color": r.Color, "is_visible": r.IsVisible, "is_cache": r.IsCache,
		"is_affix": r.IsAffix, "is_breadcrumb": r.IsBreadcrumb, "external_url": r.ExternalURL,
		"sort_code": r.SortCode, "status": r.Status,
	}
	if r.ParentID != nil { n["parent_id"] = *r.ParentID } else { n["parent_id"] = nil }
	if r.Description != nil { n["description"] = *r.Description }
	return n
}

func isInPath(category, id, entityID string) bool {
	if id == "" { return false }
	ctx := context.Background()
	current := entityID
	for current != "" && current != "0" {
		if current == id { return true }
		var e SysResource
		if err := db.DB.WithContext(ctx).First(&e, "id = ?", current).Error; err != nil { return false }
		if e.ParentID == nil || *e.ParentID == "" || *e.ParentID == "0" { break }
		current = *e.ParentID
	}
	return false
}

func collectDescendant(ids []string) []string {
	ctx := context.Background()
	m := make(map[string]bool)
	for _, id := range ids { m[id] = true }
	q := make([]string, len(ids)); copy(q, ids)
	for len(q) > 0 {
		pid := q[len(q)-1]; q = q[:len(q)-1]
		var children []SysResource
		db.DB.WithContext(ctx).Where("parent_id = ?", pid).Find(&children)
		for _, c := range children {
			if !m[c.ID] { m[c.ID] = true; q = append(q, c.ID) }
		}
	}
	r := make([]string, 0, len(m)); for id := range m { r = append(r, id) }; return r
}

type relRoleResource struct {
	ID         string
	RoleID     string
	ResourceID string
}
type relRolePermission struct {
	ID             string
	RoleID         string
	PermissionCode string
}

func syncPerm(resourceID string, oldExtra, newExtra *string) {
	ctx := context.Background()
	oldCode := extractPermCode(oldExtra)
	newCode := extractPermCode(newExtra)
	if oldCode == newCode { return }

	tx := db.DB.WithContext(ctx).Begin()

	var roleResources []relRoleResource
	tx.Table("rel_role_resource").Where("resource_id = ?", resourceID).Find(&roleResources)
	if len(roleResources) == 0 { tx.Rollback(); return }

	roleIDs := make([]string, len(roleResources))
	for i, rr := range roleResources { roleIDs[i] = rr.RoleID }

	if oldCode != "" {
		tx.Exec("DELETE FROM rel_role_permission WHERE role_id IN ? AND permission_code = ?", roleIDs, oldCode)
	}

	if newCode != "" {
		for _, rid := range roleIDs {
			var cnt int64
			tx.Table("rel_role_permission").Where("role_id = ? AND permission_code = ?", rid, newCode).Count(&cnt)
			if cnt == 0 {
				tx.Exec("INSERT INTO rel_role_permission (id, role_id, permission_code) VALUES (?, ?, ?)", utils.GenerateID(), rid, newCode)
			}
		}
	}

	if err := tx.Commit().Error; err != nil { log.Printf("[RESOURCE] Failed to commit transaction: %v", err) }
}

func extractPermCode(extra *string) string {
	if extra == nil || *extra == "" { return "" }
	var m map[string]interface{}
	if err := json.Unmarshal([]byte(*extra), &m); err != nil { return "" }
	code, _ := m["permission_code"].(string)
	return code
}

func resFromEnt(r *SysResource) *ResourceVO {
	return &ResourceVO{
		ID: r.ID, Code: r.Code, Name: r.Name, Category: r.Category, Type: r.Type,
		Description: r.Description, ParentID: r.ParentID, RoutePath: r.RoutePath,
		ComponentPath: r.ComponentPath, RedirectPath: r.RedirectPath, Icon: r.Icon,
		Color: r.Color, IsVisible: r.IsVisible, IsCache: r.IsCache, IsAffix: r.IsAffix,
		IsBreadcrumb: r.IsBreadcrumb, ExternalURL: r.ExternalURL, Extra: r.Extra,
		Status: r.Status, SortCode: r.SortCode, CreatedAt: r.CreatedAt, CreatedBy: r.CreatedBy,
		UpdatedAt: r.UpdatedAt, UpdatedBy: r.UpdatedBy,
	}
}

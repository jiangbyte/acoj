package group

import (
	"context"
	"sort"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

func formatTime(t *time.Time) string { if t == nil { return "" }; return t.Format("2006-01-02 15:04:05") }

func entToVO(entity *SysGroup) *GroupVO {
	if entity == nil { return nil }
	return &GroupVO{
		ID: entity.ID, Code: entity.Code, Name: entity.Name, Category: entity.Category,
		ParentID: entity.ParentID, OrgID: entity.OrgID, Description: entity.Description,
		Status: entity.Status, SortCode: entity.SortCode, Extra: entity.Extra,
		CreatedAt: formatTime(entity.CreatedAt), CreatedBy: entity.CreatedBy,
		UpdatedAt: formatTime(entity.UpdatedAt), UpdatedBy: entity.UpdatedBy,
	}
}

func groupToVOMap(entity *SysGroup) map[string]interface{} {
	n := map[string]interface{}{
		"id": entity.ID, "code": entity.Code, "name": entity.Name, "category": entity.Category,
		"org_id": entity.OrgID, "status": entity.Status, "sort_code": entity.SortCode,
		"children": make([]map[string]interface{}, 0),
	}
	if entity.ParentID != nil { n["parent_id"] = *entity.ParentID }
	if entity.Description != nil { n["description"] = *entity.Description }
	if entity.Extra != nil { n["extra"] = *entity.Extra }
	return n
}

func getParentIDKey(parentID *string) string {
	if parentID == nil || *parentID == "" || *parentID == "0" { return "" }
	return *parentID
}

func sortTreeNodes(nodes []map[string]interface{}) {
	sort.Slice(nodes, func(i, j int) bool { si, _ := nodes[i]["sort_code"].(int); sj, _ := nodes[j]["sort_code"].(int); return si < sj })
	for _, n := range nodes { if children, ok := n["children"].([]map[string]interface{}); ok { sortTreeNodes(children) } }
}

func Page(c *gin.Context, param *GroupPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 { param.Current = 1 }
	if param.Size < 1 { param.Size = 10 }
	if param.Size > 100 { param.Size = 100 }

	query := db.DB.WithContext(ctx).Model(&SysGroup{})
	if param.Keyword != "" { query = query.Where("name LIKE ?", "%"+param.Keyword+"%") }
	if param.Category != "" { query = query.Where("category = ?", param.Category) }
	if param.OrgID != "" { query = query.Where("org_id = ?", param.OrgID) }

	var total int64
	query.Count(&total)

	var records []SysGroup
	query.Order("sort_code ASC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)

	vos := make([]*GroupVO, len(records))
	for i, r := range records { vos[i] = entToVO(&r) }
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func Tree(c *gin.Context, param *GroupTreeParam) []map[string]interface{} {
	ctx := context.Background()
	query := db.DB.WithContext(ctx).Model(&SysGroup{}).Order("sort_code ASC")
	if param.Category != "" { query = query.Where("category = ?", param.Category) }
	if param.OrgID != "" { query = query.Where("org_id = ?", param.OrgID) }

	var all []SysGroup
	query.Find(&all)
	if len(all) == 0 { return make([]map[string]interface{}, 0) }

	nodeMap := make(map[string]map[string]interface{}, len(all))
	for _, e := range all { entry := e; nodeMap[entry.ID] = groupToVOMap(&entry) }

	roots := make([]map[string]interface{}, 0)
	for _, e := range all {
		node := nodeMap[e.ID]
		pid := getParentIDKey(e.ParentID)
		if pid == "" { roots = append(roots, node) } else if parent, ok := nodeMap[pid]; ok {
			parent["children"] = append(parent["children"].([]map[string]interface{}), node)
		}
	}
	sortTreeNodes(roots)
	return roots
}

func Create(c *gin.Context, vo *GroupVO, userID string) {
	ctx := context.Background()
	now := time.Now()
	entity := SysGroup{
		ID: utils.GenerateID(), Code: vo.Code, Name: vo.Name, Category: vo.Category,
		OrgID: vo.OrgID, Status: "ENABLED", SortCode: vo.SortCode, CreatedAt: &now, UpdatedAt: &now,
	}
	if vo.ParentID != nil { entity.ParentID = vo.ParentID }
	if vo.Description != nil { entity.Description = vo.Description }
	if vo.Extra != nil { entity.Extra = vo.Extra }
	if userID != "" { entity.CreatedBy = &userID; entity.UpdatedBy = &userID }
	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil { panic(exception.NewBusinessError("添加用户组失败: "+err.Error(), 500)) }
}

func Modify(c *gin.Context, vo *GroupVO, userID string) {
	ctx := context.Background()
	var entity SysGroup
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", vo.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound { panic(exception.NewBusinessError("数据不存在", 400)) }
		panic(exception.NewBusinessError("查询用户组失败: "+err.Error(), 500))
	}

	up := map[string]interface{}{
		"code": vo.Code, "name": vo.Name, "category": vo.Category, "org_id": vo.OrgID,
		"sort_code": vo.SortCode, "updated_at": time.Now(),
	}
	if vo.ParentID != nil { up["parent_id"] = *vo.ParentID } else { up["parent_id"] = nil }
	if vo.Description != nil { up["description"] = *vo.Description } else { up["description"] = nil }
	if vo.Extra != nil { up["extra"] = *vo.Extra } else { up["extra"] = nil }
	if userID != "" { up["updated_by"] = userID }

	if err := db.DB.WithContext(ctx).Model(&SysGroup{}).Where("id = ?", vo.ID).Updates(up).Error; err != nil { panic(exception.NewBusinessError("编辑用户组失败: "+err.Error(), 500)) }
}

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	ctx := context.Background()
	allIDs := collectDescendantGroupIDs(ids)

	var userCount int64
	db.DB.WithContext(ctx).Table("sys_user").Where("group_id IN ?", allIDs).Count(&userCount)
	if userCount > 0 { panic(exception.NewBusinessError("用户组存在关联用户，无法删除", 400)) }

	if err := db.DB.WithContext(ctx).Where("id IN ?", allIDs).Delete(&SysGroup{}).Error; err != nil { panic(exception.NewBusinessError("删除用户组失败: "+err.Error(), 500)) }
}

func Detail(c *gin.Context, id string) *GroupVO {
	if id == "" { return nil }
	ctx := context.Background()
	var entity SysGroup
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound { return nil }
		panic(exception.NewBusinessError("查询用户组详情失败: "+err.Error(), 500))
	}
	return entToVO(&entity)
}

func Options(c *gin.Context) []*GroupVO {
	ctx := context.Background()
	var records []SysGroup
	db.DB.WithContext(ctx).Order("sort_code ASC").Find(&records)
	vos := make([]*GroupVO, len(records))
	for i, r := range records { vos[i] = entToVO(&r) }
	return vos
}

func collectDescendantGroupIDs(ids []string) []string {
	ctx := context.Background()
	allIDs := make(map[string]bool)
	for _, id := range ids { allIDs[id] = true }
	q := make([]string, len(ids)); copy(q, ids)
	for len(q) > 0 {
		pid := q[len(q)-1]; q = q[:len(q)-1]
		var children []SysGroup
		db.DB.WithContext(ctx).Where("parent_id = ?", pid).Find(&children)
		for _, c := range children { if !allIDs[c.ID] { allIDs[c.ID] = true; q = append(q, c.ID) } }
	}
	r := make([]string, 0, len(allIDs)); for id := range allIDs { r = append(r, id) }; return r
}

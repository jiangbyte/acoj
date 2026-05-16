package group

import (
	"context"
	"sort"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysgroup"
	"hei-gin/ent/gen/sysorg"
	"hei-gin/ent/gen/sysposition"
	"hei-gin/ent/gen/sysuser"

	"github.com/gin-gonic/gin"
)

// GroupPage returns a paginated list of user groups.
func GroupPage(c *gin.Context, param *GroupPageParam) gin.H {
	ctx := context.Background()

	// CRITICAL: if both param.ParentID and param.OrgID are empty, return empty records
	if param.ParentID == "" && param.OrgID == "" {
		return result.PageDataResult(c, []*GroupVO{}, 0, 0, 0)
	}

	// Set defaults
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	offset := (param.Current - 1) * param.Size

	// Build query
	query := db.Client.SysGroup.Query()

	// ParentID filter: match records whose ParentID equals param.ParentID, OR whose ID equals param.ParentID
	if param.ParentID != "" {
		query = query.Where(sysgroup.Or(sysgroup.ParentID(param.ParentID), sysgroup.ID(param.ParentID)))
	}
	// OrgID filter
	if param.OrgID != "" {
		query = query.Where(sysgroup.OrgIDEQ(param.OrgID))
	}
	// Keyword filter: fuzzy match name
	if param.Keyword != "" {
		query = query.Where(sysgroup.NameContains(param.Keyword))
	}

	// Count total
	total, err := query.Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户组列表失败: "+err.Error(), 500))
	}

	// Query records ordered by sort_code ASC
	records, err := query.Clone().
		Order(sysgroup.BySortCode()).
		Limit(param.Size).
		Offset(offset).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户组列表失败: "+err.Error(), 500))
	}

	vos := make([]*GroupVO, 0, len(records))
	for _, r := range records {
		vo := entToVO(r)
		enrichOrgNames(vo)
		vos = append(vos, vo)
	}

	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

// GroupTree returns the user group tree filtered by org and keyword.
func GroupTree(c *gin.Context, param *GroupTreeParam) []map[string]interface{} {
	if param.OrgID == "" {
		return nil
	}

	ctx := context.Background()

	query := db.Client.SysGroup.Query().Order(sysgroup.BySortCode())
	query = query.Where(sysgroup.OrgIDEQ(param.OrgID))
	if param.Keyword != "" {
		query = query.Where(sysgroup.NameContains(param.Keyword))
	}

	all, err := query.All(ctx)
	if err != nil {
		return nil
	}

	// Build node map
	nodeMap := make(map[string]map[string]interface{})
	for _, g := range all {
		node := map[string]interface{}{
			"id":        g.ID,
			"code":      g.Code,
			"name":      g.Name,
			"category":  g.Category,
			"parent_id": g.ParentID,
			"org_id":    g.OrgID,
			"status":    g.Status,
			"sort_code": g.SortCode,
			"children":  make([]map[string]interface{}, 0),
		}
		nodeMap[g.ID] = node
	}

	// Wire parent-child relationships
	roots := make([]map[string]interface{}, 0)
	for _, node := range nodeMap {
		pid := node["parent_id"].(*string)
		if pid != nil && *pid != "" {
			if parent, ok := nodeMap[*pid]; ok {
				parentChildren := parent["children"].([]map[string]interface{})
				parent["children"] = append(parentChildren, node)
			} else {
				roots = append(roots, node)
			}
		} else {
			roots = append(roots, node)
		}
	}

	sortTreeNodes(roots)
	return roots
}

// GroupUnionTree returns a combined org + user group tree.
func GroupUnionTree(c *gin.Context) []map[string]interface{} {
	ctx := context.Background()

	// Query all orgs and groups
	allOrgs, err := db.Client.SysOrg.Query().Order(sysorg.BySortCode()).All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询组织列表失败: "+err.Error(), 500))
	}

	allGroups, err := db.Client.SysGroup.Query().Order(sysgroup.BySortCode()).All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户组列表失败: "+err.Error(), 500))
	}

	// Build org node map
	orgNodes := make(map[string]map[string]interface{})
	for _, o := range allOrgs {
		node := map[string]interface{}{
			"id":        o.ID,
			"code":      o.Code,
			"name":      o.Name,
			"category":  o.Category,
			"parent_id": o.ParentID,
			"status":    o.Status,
			"sort_code": o.SortCode,
			"_type":     "org",
			"children":  make([]map[string]interface{}, 0),
		}
		orgNodes[o.ID] = node
	}

	// Build group node map
	groupNodes := make(map[string]map[string]interface{})
	for _, g := range allGroups {
		node := map[string]interface{}{
			"id":        g.ID,
			"code":      g.Code,
			"name":      g.Name,
			"category":  g.Category,
			"parent_id": g.ParentID,
			"org_id":    g.OrgID,
			"status":    g.Status,
			"sort_code": g.SortCode,
			"_type":     "group",
			"children":  make([]map[string]interface{}, 0),
		}
		groupNodes[g.ID] = node
	}

	// Wire group internal parent-child relationships
	for _, node := range groupNodes {
		pid := node["parent_id"].(*string)
		if pid != nil && *pid != "" {
			if parent, ok := groupNodes[*pid]; ok {
				parentChildren := parent["children"].([]map[string]interface{})
				parent["children"] = append(parentChildren, node)
			}
		}
	}

	// Collect orphan groups (no parent or parent not in groupNodes), group by org_id
	orphanGroups := make(map[string][]map[string]interface{})
	for _, node := range groupNodes {
		pid := node["parent_id"].(*string)
		if pid == nil || *pid == "" || groupNodes[*pid] == nil {
			oid := ""
			if v, ok := node["org_id"]; ok {
				oid = v.(string)
			}
			orphanGroups[oid] = append(orphanGroups[oid], node)
		}
	}

	// Attach orphan groups as children under matching org node
	for oid, children := range orphanGroups {
		if orgNode, ok := orgNodes[oid]; ok {
			existingChildren := orgNode["children"].([]map[string]interface{})
			orgNode["children"] = append(orgNode["children"].([]map[string]interface{}), children...)
			_ = existingChildren // existingChildren already merged via append
		}
	}

	// Wire org internal parent-child relationships
	roots := make([]map[string]interface{}, 0)
	for _, node := range orgNodes {
		pid := node["parent_id"].(*string)
		if pid != nil && *pid != "" {
			if parent, ok := orgNodes[*pid]; ok {
				parentChildren := parent["children"].([]map[string]interface{})
				parent["children"] = append(parentChildren, node)
			} else {
				roots = append(roots, node)
			}
		} else {
			roots = append(roots, node)
		}
	}

	sortTreeNodes(roots)
	return roots
}

// GroupCreate creates a new user group.
func GroupCreate(c *gin.Context, vo *GroupVO, userID string) {
	ctx := context.Background()
	now := time.Now()

	builder := db.Client.SysGroup.Create().
		SetID(utils.GenerateID()).
		SetCode(vo.Code).
		SetName(vo.Name).
		SetCategory(vo.Category).
		SetOrgID(vo.OrgID).
		SetSortCode(vo.SortCode).
		SetCreatedAt(now).
		SetUpdatedAt(now)

	if vo.ParentID != nil {
		builder.SetNillableParentID(vo.ParentID)
	}
	if vo.Description != nil {
		builder.SetNillableDescription(vo.Description)
	}
	if vo.Status != "" {
		builder.SetStatus(vo.Status)
	}
	if vo.Extra != nil {
		builder.SetNillableExtra(vo.Extra)
	}
	if userID != "" {
		builder.SetCreatedBy(userID).SetUpdatedBy(userID)
	}

	_, err := builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("添加用户组失败: "+err.Error(), 500))
	}
}

// GroupModify updates an existing user group.
func GroupModify(c *gin.Context, vo *GroupVO, userID string) {
	ctx := context.Background()

	if vo.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}

	// Verify the group exists
	entity, err := db.Client.SysGroup.Get(ctx, vo.ID)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("数据不存在", 404))
		}
		panic(exception.NewBusinessError("查询用户组失败: "+err.Error(), 500))
	}

	// Check circular parent reference if ParentID changed
	if vo.ParentID != nil {
		newParentID := *vo.ParentID
		oldParentID := ""
		if entity.ParentID != nil {
			oldParentID = *entity.ParentID
		}
		if newParentID != oldParentID {
			checkCircularParent(vo.ID, newParentID)
		}
	}

	now := time.Now()
	builder := db.Client.SysGroup.UpdateOneID(vo.ID).
		SetCode(vo.Code).
		SetName(vo.Name).
		SetCategory(vo.Category).
		SetOrgID(vo.OrgID).
		SetSortCode(vo.SortCode).
		SetUpdatedAt(now)

	if vo.ParentID != nil {
		if *vo.ParentID == "" {
			builder.ClearParentID()
		} else {
			builder.SetParentID(*vo.ParentID)
		}
	}
	if vo.Description != nil {
		builder.SetNillableDescription(vo.Description)
	}
	if vo.Status != "" {
		builder.SetStatus(vo.Status)
	}
	if vo.Extra != nil {
		builder.SetNillableExtra(vo.Extra)
	}
	if userID != "" {
		builder.SetUpdatedBy(userID)
	}

	_, err = builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("编辑用户组失败: "+err.Error(), 500))
	}
}

// GroupRemove deletes user groups by IDs, including all descendants.
func GroupRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}

	ctx := context.Background()

	// Collect all descendant IDs recursively
	allIDs := collectDescendantIDs(ids)

	// Check if any user has GroupID in allIDs
	count, err := db.Client.SysUser.Query().Where(sysuser.GroupIDIn(allIDs...)).Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户关联失败: "+err.Error(), 500))
	}
	if count > 0 {
		panic(exception.NewBusinessError("用户组存在关联用户，无法删除", 400))
	}

	// Disconnect users: clear group_id reference
	_, err = db.Client.SysUser.Update().Where(sysuser.GroupIDIn(allIDs...)).ClearGroupID().Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("断开用户关联失败: "+err.Error(), 500))
	}

	// Disconnect positions: clear group_id reference
	_, err = db.Client.SysPosition.Update().Where(sysposition.GroupIDIn(allIDs...)).ClearGroupID().Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("断开职位关联失败: "+err.Error(), 500))
	}

	// Delete groups
	_, err = db.Client.SysGroup.Delete().Where(sysgroup.IDIn(allIDs...)).Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除用户组失败: "+err.Error(), 500))
	}
}

// GroupDetail returns a single user group by ID.
func GroupDetail(c *gin.Context, id string) *GroupVO {
	if id == "" {
		return nil
	}

	ctx := context.Background()
	entity, err := db.Client.SysGroup.Get(ctx, id)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil
		}
		panic(exception.NewBusinessError("查询用户组详情失败: "+err.Error(), 500))
	}

	vo := entToVO(entity)
	enrichOrgNames(vo)
	return vo
}

// entToVO converts an ent SysGroup entity to a GroupVO.
func entToVO(entity *ent.SysGroup) *GroupVO {
	vo := &GroupVO{
		ID:       entity.ID,
		Code:     entity.Code,
		Name:     entity.Name,
		Category: entity.Category,
		OrgID:    entity.OrgID,
		Status:   entity.Status,
		SortCode: entity.SortCode,
	}

	if entity.ParentID != nil {
		vo.ParentID = entity.ParentID
	}
	if entity.Description != nil {
		vo.Description = entity.Description
	}
	if entity.Extra != nil {
		vo.Extra = entity.Extra
	}
	if entity.CreatedAt != nil {
		vo.CreatedAt = entity.CreatedAt.Format("2006-01-02 15:04:05")
	}
	if entity.CreatedBy != nil {
		vo.CreatedBy = entity.CreatedBy
	}
	if entity.UpdatedAt != nil {
		vo.UpdatedAt = entity.UpdatedAt.Format("2006-01-02 15:04:05")
	}
	if entity.UpdatedBy != nil {
		vo.UpdatedBy = entity.UpdatedBy
	}

	return vo
}

// enrichOrgNames populates the OrgNames field on a GroupVO by resolving the org name path.
func enrichOrgNames(vo *GroupVO) {
	if vo.OrgID == "" {
		return
	}
	vo.OrgNames = resolveOrgPath(vo.OrgID)
}

// resolveOrgPath resolves the full org name path from root to the given org ID.
func resolveOrgPath(orgID string) []string {
	if orgID == "" {
		return nil
	}

	ctx := context.Background()
	allOrgs, err := db.Client.SysOrg.Query().All(ctx)
	if err != nil {
		return nil
	}

	orgMap := make(map[string]*ent.SysOrg)
	for _, o := range allOrgs {
		orgMap[o.ID] = o
	}

	var path []string
	current := orgID
	for current != "" {
		org, ok := orgMap[current]
		if !ok {
			break
		}
		path = append([]string{org.Name}, path...)
		if org.ParentID == nil || *org.ParentID == "" {
			break
		}
		current = *org.ParentID
	}
	return path
}

// sortTreeNodes recursively sorts tree nodes by sort_code.
func sortTreeNodes(nodes []map[string]interface{}) {
	sort.Slice(nodes, func(i, j int) bool {
		si, _ := nodes[i]["sort_code"].(int)
		sj, _ := nodes[j]["sort_code"].(int)
		return si < sj
	})
	for _, node := range nodes {
		if children, ok := node["children"].([]map[string]interface{}); ok {
			sortTreeNodes(children)
		}
	}
}

// checkCircularParent checks if setting newParentID as the parent of entityID would create a circular reference.
func checkCircularParent(entityID, newParentID string) {
	if entityID == "" || newParentID == "" {
		return
	}

	ctx := context.Background()
	all, err := db.Client.SysGroup.Query().All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户组失败: "+err.Error(), 500))
	}

	parentMap := make(map[string]string)
	for _, g := range all {
		if g.ParentID != nil {
			parentMap[g.ID] = *g.ParentID
		}
	}

	current := newParentID
	for current != "" {
		if current == entityID {
			panic(exception.NewBusinessError("父级不能选择自身或子节点", 400))
		}
		current = parentMap[current]
	}
}

// collectDescendantIDs collects all descendant group IDs for the given IDs using DFS.
func collectDescendantIDs(ids []string) []string {
	ctx := context.Background()
	all, err := db.Client.SysGroup.Query().All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户组失败: "+err.Error(), 500))
	}

	childrenMap := make(map[string][]string)
	for _, g := range all {
		if g.ParentID != nil {
			childrenMap[*g.ParentID] = append(childrenMap[*g.ParentID], g.ID)
		}
	}

	result := make([]string, 0)
	result = append(result, ids...)

	for i := 0; i < len(result); i++ {
		if childIDs, ok := childrenMap[result[i]]; ok {
			result = append(result, childIDs...)
		}
	}

	return result
}

package position

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysposition"
	"hei-gin/ent/gen/sysuser"

	"github.com/gin-gonic/gin"
)

// Page returns a paginated list of positions.
func Page(c *gin.Context, param *PositionPageParam) gin.H {
	ctx := context.Background()

	// Set defaults
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	// When group_id is empty, return empty data immediately
	if param.GroupID == "" {
		return result.PageDataResult(c, []*PositionVO{}, 0, param.Current, param.Size)
	}

	offset := (param.Current - 1) * param.Size

	// Build query with filters
	query := db.Client.SysPosition.Query()
	if param.Keyword != "" {
		query = query.Where(sysposition.NameContains(param.Keyword))
	}
	if param.GroupID != "" {
		query = query.Where(sysposition.GroupID(param.GroupID))
	}
	if param.OrgID != "" {
		query = query.Where(sysposition.OrgID(param.OrgID))
	}

	total, err := query.Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询职位列表失败: "+err.Error(), 500))
	}

	records, err := query.
		Order(sysposition.BySortCode()).
		Limit(param.Size).
		Offset(offset).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询职位列表失败: "+err.Error(), 500))
	}

	vos := make([]*PositionVO, 0, len(records))
	for _, r := range records {
		vo := entToVO(r)
		enrichPositionVO(ctx, vo)
		vos = append(vos, vo)
	}

	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

// enrichPositionVO enriches a PositionVO with org_names and group_names via name path resolution.
func enrichPositionVO(ctx context.Context, vo *PositionVO) {
	if vo.OrgID != nil && *vo.OrgID != "" {
		vo.OrgNames = resolveOrgNamePath(ctx, *vo.OrgID)
	}
	if vo.GroupID != nil && *vo.GroupID != "" {
		vo.GroupNames = resolveGroupNamePath(ctx, *vo.GroupID)
	}
}

// resolveOrgNamePath resolves the org name path from the given ID to root by following parent_id chain.
func resolveOrgNamePath(ctx context.Context, id string) []string {
	var names []string
	current := id
	for current != "" {
		entity, err := db.Client.SysOrg.Get(ctx, current)
		if err != nil {
			break
		}
		names = append(names, entity.Name)
		if entity.ParentID == nil || *entity.ParentID == "" || *entity.ParentID == "0" {
			break
		}
		current = *entity.ParentID
	}
	// Reverse to get root -> leaf order
	for i, j := 0, len(names)-1; i < j; i, j = i+1, j-1 {
		names[i], names[j] = names[j], names[i]
	}
	return names
}

// resolveGroupNamePath resolves the group name path from the given ID to root by following parent_id chain.
func resolveGroupNamePath(ctx context.Context, id string) []string {
	var names []string
	current := id
	for current != "" {
		entity, err := db.Client.SysGroup.Get(ctx, current)
		if err != nil {
			break
		}
		names = append(names, entity.Name)
		if entity.ParentID == nil || *entity.ParentID == "" {
			break
		}
		current = *entity.ParentID
	}
	for i, j := 0, len(names)-1; i < j; i, j = i+1, j-1 {
		names[i], names[j] = names[j], names[i]
	}
	return names
}

// Detail returns a single position by ID.
func Detail(c *gin.Context, id string) *PositionVO {
	if id == "" {
		return nil
	}

	ctx := context.Background()
	entity, err := db.Client.SysPosition.Get(ctx, id)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil
		}
		panic(exception.NewBusinessError("查询职位详情失败: "+err.Error(), 500))
	}

	vo := entToVO(entity)
	enrichPositionVO(ctx, vo)
	return vo
}

// Create creates a new position.
func Create(c *gin.Context, vo *PositionVO, userID string) {
	ctx := context.Background()
	now := time.Now()

	builder := db.Client.SysPosition.Create().
		SetID(utils.GenerateID()).
		SetCode(vo.Code).
		SetName(vo.Name).
		SetCategory(vo.Category).
		SetStatus(vo.Status).
		SetSortCode(vo.SortCode).
		SetCreatedAt(now).
		SetUpdatedAt(now)

	if vo.OrgID != nil {
		builder.SetNillableOrgID(vo.OrgID)
	}
	if vo.GroupID != nil {
		builder.SetNillableGroupID(vo.GroupID)
	}
	if vo.Description != nil {
		builder.SetNillableDescription(vo.Description)
	}
	if vo.Extra != nil {
		builder.SetNillableExtra(vo.Extra)
	}
	if userID != "" {
		builder.SetCreatedBy(userID).SetUpdatedBy(userID)
	}

	_, err := builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("添加职位失败: "+err.Error(), 500))
	}
}

// Modify updates an existing position.
func Modify(c *gin.Context, vo *PositionVO, userID string) {
	ctx := context.Background()

	if vo.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}

	// Verify the position exists
	_, err := db.Client.SysPosition.Get(ctx, vo.ID)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("数据不存在", 400))
		}
		panic(exception.NewBusinessError("查询职位失败: "+err.Error(), 500))
	}

	now := time.Now()
	builder := db.Client.SysPosition.UpdateOneID(vo.ID).
		SetCode(vo.Code).
		SetName(vo.Name).
		SetCategory(vo.Category).
		SetStatus(vo.Status).
		SetSortCode(vo.SortCode).
		SetUpdatedAt(now)

	if vo.OrgID != nil {
		builder.SetNillableOrgID(vo.OrgID)
	} else {
		builder.ClearOrgID()
	}

	if vo.GroupID != nil {
		builder.SetNillableGroupID(vo.GroupID)
	} else {
		builder.ClearGroupID()
	}

	if vo.Description != nil {
		builder.SetNillableDescription(vo.Description)
	}

	if vo.Extra != nil {
		builder.SetNillableExtra(vo.Extra)
	}

	if userID != "" {
		builder.SetUpdatedBy(userID)
	}

	_, err = builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("编辑职位失败: "+err.Error(), 500))
	}
}

// Remove deletes positions by IDs.
func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}

	ctx := context.Background()

	// Check if any user references these positions
	count, err := db.Client.SysUser.Query().Where(sysuser.PositionIDIn(ids...)).Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询关联用户失败: "+err.Error(), 500))
	}
	if count > 0 {
		panic(exception.NewBusinessError("职位存在关联用户，无法删除", 400))
	}

	// Clear position_id references on SysUser
	_, err = db.Client.SysUser.Update().Where(sysuser.PositionIDIn(ids...)).ClearPositionID().Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("清除用户关联失败: "+err.Error(), 500))
	}

	// Delete positions
	_, err = db.Client.SysPosition.Delete().Where(sysposition.IDIn(ids...)).Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除职位失败: "+err.Error(), 500))
	}
}

// entToVO converts an ent SysPosition entity to a PositionVO.
func entToVO(entity *ent.SysPosition) *PositionVO {
	vo := &PositionVO{
		ID:       entity.ID,
		Code:     entity.Code,
		Name:     entity.Name,
		Category: entity.Category,
		Status:   entity.Status,
		SortCode: entity.SortCode,
	}

	if entity.OrgID != nil {
		vo.OrgID = entity.OrgID
	}
	if entity.GroupID != nil {
		vo.GroupID = entity.GroupID
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

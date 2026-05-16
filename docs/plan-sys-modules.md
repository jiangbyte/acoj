# hei-gin sys 模块实现计划（深入版）

## 概述

参考 `hei-fastapi` 的实现，为 `hei-gin` 实现 7 个系统模块：**user、role、group、permission、home、dict、config**。

每个端点和数据响应/请求必须与 FastAPI 版保持一致。

> **已重构**：resource 模块已按 banner 风格重构。

---

## 参考风格

统一采用 **banner 模块风格**（已重构 resource 模块对齐此风格）：

```
modules/sys/{module}/
├── params.go        # 请求/响应结构体（package {module}）
├── service.go       # 业务逻辑 + entToVO 转换（package {module}，包级函数）
└── api/v1/api.go    # 路由注册 + HTTP 处理器（package v1，调用 {module}.Func）
```

### 风格要点

| 方面 | 说明 |
|------|------|
| service 函数签名 | `Func(c *gin.Context, ...) -> data/gin.H`，错误通过 `panic(exception.NewBusinessError)` 抛出 |
| api 处理器 | 绑定参数 → 调用 service → `c.JSON(200, result.Success/Failure(...))` |
| entToVO | 包级函数，将 ent 实体转换为 VO 结构体，处理 `*string`/`*time.Time` 空指针 |
| 路由注册 | `RegisterRoutes(r *gin.Engine)` 在 `api/v1/api.go` 中 |

### ent 实体注意点

所有非主键字段：
- **非空字符串**（Status、SortCode、Code、Name、Category 等）：Go 类型为 `string`，直接赋值
- **可空字段**（Username、Nickname、Email、Avatar 等）：Go 类型为 `*string`，取值时需 nil 检查
- **可空时间**（Birthday、CreatedAt、UpdatedAt 等）：Go 类型为 `*time.Time`，取值时需 nil 检查
- **int 字段**（LoginCount、SortCode）：type `int`，直接赋值

### 时间格式化

```go
func formatTime(t *time.Time) string {
    if t == nil {
        return ""
    }
    return t.Format("2006-01-02 15:04:05")
}

func formatDate(t *time.Time) string {
    if t == nil {
        return ""
    }
    return t.Format("2006-01-02")
}
```

---

## 1. role 模块 (`modules/sys/role/`)

**依赖**: 无。PermissionItem 类型被 user 模块引用。

### Endpoints（10 个）

| 方法 | 路径 | 权限 | SysLog | NoRepeat | 处理器 |
|------|------|------|--------|----------|--------|
| GET | `/api/v1/sys/role/page` | `sys:role:page` | - | - | rolePage |
| POST | `/api/v1/sys/role/create` | `sys:role:create` | 添加角色 | 3000 | roleCreate |
| POST | `/api/v1/sys/role/modify` | `sys:role:modify` | 编辑角色 | - | roleModify |
| POST | `/api/v1/sys/role/remove` | `sys:role:remove` | 删除角色 | - | roleRemove |
| GET | `/api/v1/sys/role/detail` | `sys:role:detail` | - | - | roleDetail |
| POST | `/api/v1/sys/role/grant-permission` | `sys:role:grant-permission` | 分配角色权限 | 3000 | roleGrantPermission |
| POST | `/api/v1/sys/role/grant-resource` | `sys:role:grant-resource` | 分配角色资源 | 3000 | roleGrantResource |
| GET | `/api/v1/sys/role/own-permission` | `sys:role:own-permission` | - | - | roleOwnPermission |
| GET | `/api/v1/sys/role/own-permission-detail` | `sys:role:own-permission` | - | - | roleOwnPermissionDetail |
| GET | `/api/v1/sys/role/own-resource` | `sys:role:own-resource` | - | - | roleOwnResource |

### params.go

```go
package role

type RoleVO struct {
    ID          string  `json:"id,omitempty"`
    Code        string  `json:"code"`
    Name        string  `json:"name"`
    Category    string  `json:"category"`
    Description *string `json:"description,omitempty"`
    Status      string  `json:"status,omitempty"`
    SortCode    int     `json:"sort_code"`
    Extra       *string `json:"extra,omitempty"`
    CreatedAt   string  `json:"created_at,omitempty"`
    CreatedBy   *string `json:"created_by,omitempty"`
    UpdatedAt   string  `json:"updated_at,omitempty"`
    UpdatedBy   *string `json:"updated_by,omitempty"`
}

type RolePageParam struct {
    Current int `json:"current" form:"current"`
    Size    int `json:"size" form:"size"`
}

// PermissionItem — 被 user 模块引用，放在 role 包中
type PermissionItem struct {
    PermissionCode      string  `json:"permission_code"`
    Scope               string  `json:"scope"`
    CustomScopeGroupIds *string `json:"custom_scope_group_ids,omitempty"`
    CustomScopeOrgIds   *string `json:"custom_scope_org_ids,omitempty"`
}

type GrantPermissionParam struct {
    RoleID      string           `json:"role_id"`
    Permissions []PermissionItem `json:"permissions"`
}

type ButtonPermissionScope struct {
    PermissionCode      string  `json:"permission_code"`
    Scope               string  `json:"scope"`
    CustomScopeGroupIds *string `json:"custom_scope_group_ids,omitempty"`
    CustomScopeOrgIds   *string `json:"custom_scope_org_ids,omitempty"`
}

type GrantResourceParam struct {
    RoleID      string                  `json:"role_id"`
    ResourceIDs []string                `json:"resource_ids"`
    Permissions []ButtonPermissionScope `json:"permissions"`
}
```

### service.go — 关键函数

**entToVO**:
```go
func entToVO(entity *gen.SysRole) *RoleVO {
    if entity == nil {
        return nil
    }
    return &RoleVO{
        ID:          entity.ID,
        Code:        entity.Code,
        Name:        entity.Name,
        Category:    entity.Category,
        Description: entity.Description,
        Status:      entity.Status,
        SortCode:    entity.SortCode,
        Extra:       entity.Extra,
        CreatedAt:   formatTime(entity.CreatedAt),
        CreatedBy:   entity.CreatedBy,
        UpdatedAt:   formatTime(entity.UpdatedAt),
        UpdatedBy:   entity.UpdatedBy,
    }
}
```

**Page**:
```go
func RolePage(c *gin.Context, param *RolePageParam) gin.H {
    if param.Current < 1 { param.Current = 1 }
    if param.Size < 1 { param.Size = 10 }

    total, err := db.Client.SysRole.Query().Count(ctx)
    if err != nil { panic(exception.NewBusinessError("查询角色列表失败: "+err.Error(), 500)) }

    records, err := db.Client.SysRole.Query().
        Order(sysrole.ByCreatedAt(entsql.OrderDesc())).
        Limit(param.Size).Offset((param.Current - 1) * param.Size).
        All(ctx)
    if err != nil { panic(exception.NewBusinessError("查询角色列表失败: "+err.Error(), 500)) }

    vos := make([]*RoleVO, len(records))
    for i, r := range records { vos[i] = entToVO(r) }
    return result.PageDataResult(c, vos, total, param.Current, param.Size)
}
```

**Create**:
```go
func RoleCreate(c *gin.Context, vo *RoleVO, userID string) {
    // StripSystemFields 排除 role_ids、group_id 等额外字段（但 RoleVO 不含这些）
    create := db.Client.SysRole.Create().
        SetID(utils.GenerateID()).
        SetCode(vo.Code).SetName(vo.Name).SetCategory(vo.Category).
        SetCreatedAt(now).SetUpdatedAt(now)
    if userID != "" { create.SetCreatedBy(userID).SetUpdatedBy(userID) }
    if vo.SortCode != 0 { create.SetSortCode(vo.SortCode) }
    // ... nullable 字段同理
    _, err := create.Save(ctx)
    if err != nil { panic(...) }
}
```

**Modify**:
```go
func RoleModify(c *gin.Context, vo *RoleVO, userID string) {
    _, err := db.Client.SysRole.Get(ctx, vo.ID)
    if err != nil {
        if gen.IsNotFound(err) { panic(exception.NewBusinessError("数据不存在", 400)) }
        panic(exception.NewBusinessError("查询角色失败: "+err.Error(), 500))
    }
    update := db.Client.SysRole.UpdateOneID(vo.ID).
        SetCode(vo.Code).SetName(vo.Name).SetCategory(vo.Category).
        SetUpdatedAt(time.Now())
    if userID != "" { update.SetUpdatedBy(userID) }
    // nullable 字段用 SetNillableXxx 或条件
    // SortCode: if vo.SortCode != 0 { update.SetSortCode(vo.SortCode) } else { update.SetSortCode(0) }
    // Extra: if vo.Extra != nil { update.SetExtra(*vo.Extra) } else { update.ClearExtra() }
    _, err = update.Save(ctx)
    if err != nil { panic(...) }
}
```

**Remove**:
```go
func RoleRemove(c *gin.Context, ids []string) {
    // 先检查是否有用户关联
    count, err := db.Client.RelUserRole.Query().
        Where(reluserrole.RoleIDIn(ids...)).Count(ctx)
    if err != nil { panic(...) }
    if count > 0 { panic(exception.NewBusinessError("角色存在关联用户，无法删除", 400)) }

    // 删除关联
    db.Client.RelRolePermission.Delete().Where(relrolepermission.RoleIDIn(ids...)).Exec(ctx)
    db.Client.RelRoleResource.Delete().Where(relroleresource.RoleIDIn(ids...)).Exec(ctx)

    // 删除角色
    db.Client.SysRole.Delete().Where(sysrole.IDIn(ids...)).Exec(ctx)
    if err != nil { panic(exception.NewBusinessError("删除角色失败: "+err.Error(), 500)) }
}
```

**GrantPermissions**: 先清空后插入
```go
func RoleGrantPermissions(c *gin.Context, roleID string, permissions []PermissionItem, userID string) {
    db.Client.RelRolePermission.Delete().Where(relrolepermission.RoleID(roleID)).Exec(ctx)
    for _, p := range permissions {
        db.Client.RelRolePermission.Create().
            SetID(utils.GenerateID()).SetRoleID(roleID).SetPermissionCode(p.PermissionCode).
            SetScope(p.Scope).
            SetNillableCustomScopeGroupIds(p.CustomScopeGroupIds).
            SetNillableCustomScopeOrgIds(p.CustomScopeOrgIds).
            Exec(ctx)
    }
}
```

**GrantResources**: 先清空资源关联，写入新资源，再从 resource extra 中提取 permission_code 同步权限
```go
func RoleGrantResources(c *gin.Context, roleID string, param *GrantResourceParam, userID string) {
    // 去重 resource_ids
    resourceIDs := removeDuplicates(param.ResourceIDs)
    
    // 1. 清空资源关联
    db.Client.RelRoleResource.Delete().Where(relroleresource.RoleID(roleID)).Exec(ctx)
    
    // 2. 写入新资源关联
    for _, resid := range resourceIDs {
        db.Client.RelRoleResource.Create().SetID(utils.GenerateID()).
            SetRoleID(roleID).SetResourceID(resid).Exec(ctx)
    }
    
    // 3. 从 resource.extra 提取 permission_code 自动同步
    // 查询有 extra 的资源
    resources, _ := db.Client.SysResource.Query().
        Where(sysresource.IDIn(resourceIDs...), sysresource.ExtraNotNil()).
        All(ctx)
    
    // 建立 scope map
    scopeMap := make(map[string]ButtonPermissionScope)
    for _, p := range param.Permissions {
        scopeMap[p.PermissionCode] = p
    }
    
    // 收集 permission_items，去重
    seen := make(map[string]bool)
    for _, r := range resources {
        if r.Extra == nil || *r.Extra == "" { continue }
        var extraMap map[string]interface{}
        json.Unmarshal([]byte(*r.Extra), &extraMap)
        pcode, _ := extraMap["permission_code"].(string)
        if pcode == "" || seen[pcode] { continue }
        seen[pcode] = true
        
        scope, ok := scopeMap[pcode]
        scopeStr := "ALL"
        var customGroupIds, customOrgIds *string
        if ok {
            scopeStr = scope.Scope
            customGroupIds = scope.CustomScopeGroupIds
            customOrgIds = scope.CustomScopeOrgIds
        }
        
        // 检查是否已存在（add_missing）
        exists, _ := db.Client.RelRolePermission.Query().
            Where(relrolepermission.RoleID(roleID), relrolepermission.PermissionCode(pcode)).Exist(ctx)
        if !exists {
            db.Client.RelRolePermission.Create().SetID(utils.GenerateID()).
                SetRoleID(roleID).SetPermissionCode(pcode).
                SetScope(scopeStr).
                SetNillableCustomScopeGroupIds(customGroupIds).
                SetNillableCustomScopeOrgIds(customOrgIds).
                Exec(ctx)
        }
    }
}
```

**OwnPermission / OwnPermissionDetail / OwnResource**:
```go
func RoleOwnPermissionCodes(c *gin.Context, roleID string) []string { ... }
func RoleOwnPermissionDetails(c *gin.Context, roleID string) []map[string]interface{} { ... }
func RoleOwnResourceIDs(c *gin.Context, roleID string) []string { ... }
```

---

## 2. user 模块 (`modules/sys/user/`)

**依赖**: `role.PermissionItem`、`role.ButtonPermissionScope`

### Endpoints（15 个）

| 方法 | 路径 | 权限 | SysLog | NoRepeat | 处理器 |
|------|------|------|--------|----------|--------|
| GET | `/api/v1/sys/user/page` | `sys:user:page` | - | - | userPage |
| POST | `/api/v1/sys/user/create` | `sys:user:create` | 添加用户 | 3000 | userCreate |
| POST | `/api/v1/sys/user/modify` | `sys:user:modify` | 编辑用户 | - | userModify |
| POST | `/api/v1/sys/user/remove` | `sys:user:remove` | 删除用户 | - | userRemove |
| GET | `/api/v1/sys/user/detail` | `sys:user:detail` | - | - | userDetail |
| POST | `/api/v1/sys/user/grant-role` | `sys:user:grant-role` | 分配用户角色 | 3000 | userGrantRole |
| POST | `/api/v1/sys/user/grant-permission` | `sys:user:grant-permission` | 分配用户权限 | 3000 | userGrantPermission |
| GET | `/api/v1/sys/user/own-permission-detail` | `sys:user:own-permission-detail` | - | - | userOwnPermissionDetail |
| GET | `/api/v1/sys/user/own-roles` | `sys:user:own-roles` | - | - | userOwnRoles |
| GET | `/api/v1/sys/user/current` | HeiCheckLogin | - | - | userCurrent |
| GET | `/api/v1/sys/user/menus` | HeiCheckLogin | - | - | userMenus |
| GET | `/api/v1/sys/user/permissions` | HeiCheckLogin | - | - | userPermissions |
| POST | `/api/v1/sys/user/update-profile` | HeiCheckLogin | 更新个人信息 | 3000 | userUpdateProfile |
| POST | `/api/v1/sys/user/update-avatar` | HeiCheckLogin | 更新头像 | - | userUpdateAvatar |
| POST | `/api/v1/sys/user/update-password` | HeiCheckLogin | 修改密码 | 3000 | userUpdatePassword |

### params.go

```go
package user

import "hei-gin/modules/sys/role"

type UserVO struct {
    ID           string   `json:"id,omitempty"`
    Username     *string  `json:"username,omitempty"`
    Nickname     *string  `json:"nickname,omitempty"`
    Avatar       *string  `json:"avatar,omitempty"`
    Motto        *string  `json:"motto,omitempty"`
    Gender       *string  `json:"gender,omitempty"`
    Birthday     string   `json:"birthday,omitempty"`     // "2006-01-02"
    Email        *string  `json:"email,omitempty"`
    Github       *string  `json:"github,omitempty"`
    Phone        *string  `json:"phone,omitempty"`
    OrgID        *string  `json:"org_id,omitempty"`
    PositionID   *string  `json:"position_id,omitempty"`
    GroupID      *string  `json:"group_id,omitempty"`
    OrgNames     []string `json:"org_names,omitempty"`
    GroupNames   []string `json:"group_names,omitempty"`
    PositionName *string  `json:"position_name,omitempty"`
    Status       string   `json:"status,omitempty"`
    LastLoginAt  string   `json:"last_login_at,omitempty"`
    LastLoginIP  *string  `json:"last_login_ip,omitempty"`
    LoginCount   int      `json:"login_count,omitempty"`
    CreatedAt    string   `json:"created_at,omitempty"`
    CreatedBy    *string  `json:"created_by,omitempty"`
    UpdatedAt    string   `json:"updated_at,omitempty"`
    UpdatedBy    *string  `json:"updated_by,omitempty"`
    RoleIDs      []string `json:"role_ids,omitempty"`
}

type UserPageParam struct {
    Current int    `json:"current" form:"current"`
    Size    int    `json:"size" form:"size"`
    Keyword string `json:"keyword,omitempty" form:"keyword"`
    Status  string `json:"status,omitempty" form:"status"`
}

type GrantRoleParam struct {
    UserID  string   `json:"user_id"`
    RoleIDs []string `json:"role_ids"`
}

type GrantUserPermissionParam struct {
    UserID      string               `json:"user_id"`
    Permissions []role.PermissionItem `json:"permissions,omitempty"`
}

type UpdateProfileParam struct {
    Username *string `json:"username,omitempty"`
    Nickname *string `json:"nickname,omitempty"`
    Motto    *string `json:"motto,omitempty"`
    Gender   *string `json:"gender,omitempty"`
    Birthday string  `json:"birthday,omitempty"`
    Email    *string `json:"email,omitempty"`
    Github   *string `json:"github,omitempty"`
    Phone    *string `json:"phone,omitempty"`
}

type UpdateAvatarParam struct {
    Avatar string `json:"avatar"`
}

type UpdatePasswordParam struct {
    CurrentPassword string `json:"current_password"`
    NewPassword     string `json:"new_password"`
}

// PermissionDetail — for own-permission-detail response
type PermissionDetail struct {
    PermissionCode      string  `json:"permission_code"`
    Scope               string  `json:"scope"`
    CustomScopeGroupIds *string `json:"custom_scope_group_ids,omitempty"`
    CustomScopeOrgIds   *string `json:"custom_scope_org_ids,omitempty"`
}
```

### service.go 关键逻辑

**entToVO**:
```go
func entToVO(entity *gen.SysUser) *UserVO {
    if entity == nil { return nil }
    vo := &UserVO{
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
    return vo
}
```

**Page**: keyword 模糊匹配 username + nickname（使用 ent 的 Contains），status 精确过滤

```go
func UserPage(c *gin.Context, param *UserPageParam) gin.H {
    query := db.Client.SysUser.Query()
    if param.Keyword != "" {
        query = query.Where(sysuser.Or(sysuser.UsernameContains(param.Keyword), sysuser.NicknameContains(param.Keyword)))
    }
    if param.Status != "" {
        query = query.Where(sysuser.StatusEQ(param.Status))
    }
    total, _ := query.Count(ctx)
    records, _ := query.Clone().Order(sysuser.ByCreatedAt(entsql.OrderDesc())).
        Limit(param.Size).Offset((param.Current - 1) * param.Size).All(ctx)

    // 批量获取 role_ids
    userIDs := extractIDs(records)
    roleMap := batchGetRoleIDs(userIDs)
    groupMap := batchGetGroupIDs(userIDs)

    vos := make([]*UserVO, len(records))
    for i, r := range records {
        vos[i] = entToVO(r)
        vos[i].RoleIDs = roleMap[r.ID]
        vos[i].GroupID = groupMap[r.ID] // 用 group_id（不是 map 返回的值）
    }
    batchEnrichNames(vos) // 填充 org_names / group_names / position_name
    return result.PageDataResult(c, vos, total, param.Current, param.Size)
}
```

**批量名称填充**:
```go
func batchEnrichNames(vos []*UserVO) {
    // 收集所有 position_id
    posIDs := make([]string, 0)
    for _, vo := range vos {
        if vo.PositionID != nil && *vo.PositionID != "" {
            posIDs = append(posIDs, *vo.PositionID)
        }
    }
    // 批量查询职位名称 → map
    posMap := make(map[string]string)
    if len(posIDs) > 0 {
        positions, _ := db.Client.SysPosition.Query().
            Where(sysposition.IDIn(posIDs...)).All(ctx)
        for _, p := range positions {
            posMap[p.ID] = p.Name
        }
    }

    // 查询所有组织（用于路径解析）
    orgs, _ := db.Client.SysOrg.Query().All(ctx)
    orgNodeMap := buildNamePathMap(orgs)
    // 查询所有用户组
    groups, _ := db.Client.SysGroup.Query().All(ctx)
    groupNodeMap := buildNamePathMap(groups)

    for _, vo := range vos {
        vo.OrgNames = resolvePathFromMap(vo.OrgID, orgNodeMap)
        vo.GroupNames = resolvePathFromMap(vo.GroupID, groupNodeMap)
        if vo.PositionID != nil {
            name, ok := posMap[*vo.PositionID]
            if ok { vo.PositionName = &name }
        }
    }
}

// buildNamePathMap 构建 {id: {name, parent_id}} 的 map
func buildNamePathMap(entities interface{}) map[string]NamePathNode { ... }

// resolvePathFromMap 从 map 中递归解析名称路径，如 ["总公司", "研发部"]
func resolvePathFromMap(id *string, nodeMap map[string]NamePathNode) []string { ... }
```

**Create**: 检查 username/email 唯一性 → StripSystemFields(extra: role_ids, group_id) → Create → 分配角色 + 设置组
```go
func UserCreate(c *gin.Context, vo *UserVO, userID string) {
    if vo.Username != nil && *vo.Username != "" {
        exists, _ := db.Client.SysUser.Query().Where(sysuser.UsernameEQ(*vo.Username)).Exist(ctx)
        if exists { panic(exception.NewBusinessError("账号已存在", 400)) }
    }
    if vo.Email != nil && *vo.Email != "" {
        exists, _ := db.Client.SysUser.Query().Where(sysuser.EmailEQ(*vo.Email)).Exist(ctx)
        if exists { panic(exception.NewBusinessError("邮箱已存在", 400)) }
    }

    now := time.Now()
    create := db.Client.SysUser.Create().
        SetID(utils.GenerateID()).SetCreatedAt(now).SetUpdatedAt(now)
    // 映射字段...
    if vo.Username != nil { create.SetUsername(*vo.Username) }
    if vo.Nickname != nil { create.SetNickname(*vo.Nickname) }
    // 注意：create 时 Password 可能为空（交由 auth 模块处理）
    if userID != "" { create.SetCreatedBy(userID).SetUpdatedBy(userID) }

    entity, err := create.Save(ctx)
    if err != nil { panic(...) }

    // 分配角色
    if len(vo.RoleIDs) > 0 {
        grantRoles(entity.ID, vo.RoleIDs, userID)
    }
    // 设置组
    if vo.GroupID != nil && *vo.GroupID != "" {
        db.Client.SysUser.UpdateOneID(entity.ID).SetGroupID(*vo.GroupID).Exec(ctx)
    }
}
```

**Modify**: `ApplyUpdate(entity, updateData, extraProtected={'password', 'role_ids', 'group_id'})` → 更新角色/组

**Remove**: 删除关联 → 删除用户
```go
func UserRemove(c *gin.Context, ids []string) {
    db.Client.RelUserRole.Delete().Where(reluserrole.UserIDIn(ids...)).Exec(ctx)
    db.Client.RelUserPermission.Delete().Where(reluserpermission.UserIDIn(ids...)).Exec(ctx)
    db.Client.SysUser.Delete().Where(sysuser.IDIn(ids...)).Exec(ctx)
}
```

**GrantRoles**: 先清空后插入
```go
func UserGrantRoles(c *gin.Context, userID string, roleIDs []string, createdBy string) {
    db.Client.RelUserRole.Delete().Where(reluserrole.UserID(userID)).Exec(ctx)
    for _, rid := range roleIDs {
        db.Client.RelUserRole.Create().SetID(utils.GenerateID()).
            SetUserID(userID).SetRoleID(rid).Exec(ctx)
    }
}
```

**GrantPermissions**: 先清空后插入（含 scope 字段）

**GetCurrentUserMenus**:
```go
func UserMenus(c *gin.Context) []map[string]interface{} {
    userID := auth.GetLoginIDDefaultNull(c)
    if userID == "" { return nil }

    // 检查是否是超级管理员
    roleCodes := getUserRoleCodes(userID)
    isSuperAdmin := contains(roleCodes, constants.SUPER_ADMIN_CODE)

    var resources []*gen.SysResource
    if isSuperAdmin {
        resources, _ = db.Client.SysResource.Query().
            Where(sysresource.CategoryEQ(resourceCategoryBackendMenu),
                sysresource.TypeIn(resourceTypeDir, resourceTypeMenu),
                sysresource.StatusEQ(statusEnabled)).
            Order(sysresource.BySortCode()).All(ctx)
    } else {
        roleIDs := getUserAllRoleIDs(userID)
        if len(roleIDs) == 0 { return nil }
        resourceIDs := getRoleResourceIDs(roleIDs)
        if len(resourceIDs) == 0 { return nil }
        resources, _ = db.Client.SysResource.Query().
            Where(sysresource.IDIn(resourceIDs...),
                sysresource.CategoryEQ(resourceCategoryBackendMenu),
                sysresource.TypeIn(resourceTypeDir, resourceTypeMenu),
                sysresource.StatusEQ(statusEnabled)).
            Order(sysresource.BySortCode()).All(ctx)
    }

    return buildMenuTree(resources)
}

// 资源名常量 — 从 enums 包获取
const (
    resourceCategoryBackendMenu = "BACKEND_MENU"
    resourceTypeDir             = "DIRECTORY"
    resourceTypeMenu            = "MENU"
    statusEnabled               = "ENABLED"
)
```

**UpdatePassword**: SM2 解密 → bcrypt 校验 → bcrypt 加密
```go
func UserUpdatePassword(c *gin.Context, param *UpdatePasswordParam) {
    userID := auth.GetLoginIDDefaultNull(c)
    entity, _ := db.Client.SysUser.Get(ctx, userID)
    if entity == nil { panic(...) }
    if entity.Password == nil || *entity.Password == "" {
        panic(exception.NewBusinessError("未设置密码，无法修改", 400))
    }

    currentPassword := utils.Decrypt(param.CurrentPassword)
    if !bcrypt.CompareHashAndPassword([]byte(*entity.Password), []byte(currentPassword)) {
        panic(exception.NewBusinessError("当前密码不正确", 400))
    }

    newPassword := utils.Decrypt(param.NewPassword)
    hashed, _ := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
    db.Client.SysUser.UpdateOneID(userID).SetPassword(string(hashed)).Exec(ctx)
}
```

---

## 3. group 模块 (`modules/sys/group/`)

### Endpoints（7 个）

| 方法 | 路径 | 权限 | SysLog | NoRepeat | 处理器 |
|------|------|------|--------|----------|--------|
| GET | `/api/v1/sys/group/page` | `sys:group:page` | - | - | groupPage |
| GET | `/api/v1/sys/group/union-tree` | `sys:group:tree` | - | - | groupUnionTree |
| GET | `/api/v1/sys/group/tree` | `sys:group:tree` | - | - | groupTree |
| POST | `/api/v1/sys/group/create` | `sys:group:create` | 添加用户组 | 3000 | groupCreate |
| POST | `/api/v1/sys/group/modify` | `sys:group:modify` | 编辑用户组 | - | groupModify |
| POST | `/api/v1/sys/group/remove` | `sys:group:remove` | 删除用户组 | - | groupRemove |
| GET | `/api/v1/sys/group/detail` | `sys:group:detail` | - | - | groupDetail |

### params.go

```go
package group

type GroupVO struct {
    ID          string   `json:"id,omitempty"`
    Code        string   `json:"code"`
    Name        string   `json:"name"`
    Category    string   `json:"category"`
    ParentID    *string  `json:"parent_id,omitempty"`
    OrgID       string   `json:"org_id"`
    Description *string  `json:"description,omitempty"`
    Status      string   `json:"status,omitempty"`
    SortCode    int      `json:"sort_code"`
    OrgNames    []string `json:"org_names,omitempty"`
    Extra       *string  `json:"extra,omitempty"`
    CreatedAt   string   `json:"created_at,omitempty"`
    CreatedBy   *string  `json:"created_by,omitempty"`
    UpdatedAt   string   `json:"updated_at,omitempty"`
    UpdatedBy   *string  `json:"updated_by,omitempty"`
}

type GroupPageParam struct {
    Current  int    `json:"current" form:"current"`
    Size     int    `json:"size" form:"size"`
    ParentID string `json:"parent_id,omitempty" form:"parent_id"`
    Keyword  string `json:"keyword,omitempty" form:"keyword"`
    OrgID    string `json:"org_id,omitempty" form:"org_id"`
}

type GroupTreeParam struct {
    OrgID   string `json:"org_id,omitempty" form:"org_id"`
    Keyword string `json:"keyword,omitempty" form:"keyword"`
}
```

### service.go 关键逻辑

**Page**: parent_id 和 org_id 都为空时直接返回空数据。

**Tree**: 按 org_id 过滤，keyword 模糊匹配 name，构建树。

**UnionTree**: 联合查询组织 + 用户组，构建树：
```go
func GroupUnionTree(c *gin.Context) []map[string]interface{} {
    orgs, _ := db.Client.SysOrg.Query().Order(sysorg.BySortCode()).All(ctx)
    groups, _ := db.Client.SysGroup.Query().Order(sysgroup.BySortCode()).All(ctx)

    // 构建 org node map
    orgNodes := make(map[string]map[string]interface{})
    for _, o := range orgs {
        node := orgToVOMap(o)
        node["_type"] = "org"
        node["children"] = make([]map[string]interface{}, 0)
        orgNodes[o.ID] = node
    }

    // 构建 group node map
    groupNodes := make(map[string]map[string]interface{})
    for _, g := range groups {
        node := groupToVOMap(g)
        node["_type"] = "group"
        node["children"] = make([]map[string]interface{}, 0)
        groupNodes[g.ID] = node
    }

    // group 内部父子关系
    for _, node := range groupNodes {
        pid := node["parent_id"].(*string)
        if pid != nil && *pid != "" {
            if parent, ok := groupNodes[*pid]; ok {
                parent["children"] = append(parent["children"].([]map[string]interface{}), node)
            }
        }
    }

    // 找出没有父节点的 group，按 org_id 挂到 org 下
    orphanGroups := make(map[string][]map[string]interface{})
    for _, node := range groupNodes {
        pid := node["parent_id"].(*string)
        if pid == nil || *pid == "" || groupNodes[*pid] == nil {
            oid := ""
            if node["org_id"] != nil { oid = node["org_id"].(string) }
            orphanGroups[oid] = append(orphanGroups[oid], node)
        }
    }

    // 将孤儿 group 挂到对应 org 下
    for oid, children := range orphanGroups {
        if orgNode, ok := orgNodes[oid]; ok {
            existingChildren := orgNode["children"].([]map[string]interface{})
            orgNode["children"] = append(children, existingChildren...)
        }
    }

    // org 内部父子关系
    roots := make([]map[string]interface{}, 0)
    for _, node := range orgNodes {
        pid := node["parent_id"].(*string)
        if pid != nil && *pid != "" {
            if parent, ok := orgNodes[*pid]; ok {
                parent["children"] = append(parent["children"].([]map[string]interface{}), node)
            }
        } else {
            roots = append(roots, node)
        }
    }

    sortTree(roots)
    return roots
}
```

**Remove**: 收集子节点 → 检查用户关联 → 断开用户/职位引用 → 删除
```go
func GroupRemove(c *gin.Context, ids []string) {
    allIDs := collectDescendantGroupIDs(ids)
    
    count, _ := db.Client.SysUser.Query().Where(sysuser.GroupIDIn(allIDs...)).Count(ctx)
    if count > 0 { panic(exception.NewBusinessError("用户组存在关联用户，无法删除", 400)) }

    // 断开用户的 group_id 引用
    db.Client.SysUser.Update().Where(sysuser.GroupIDIn(allIDs...)).ClearGroupID().Exec(ctx)
    // 断开职位的 group_id 引用（如果 SysPosition 存在 GroupID 字段）

    db.Client.SysGroup.Delete().Where(sysgroup.IDIn(allIDs...)).Exec(ctx)
}
```

---

## 4. dict 模块 (`modules/sys/dict/`)

### Endpoints（9 个）

| 方法 | 路径 | 权限 | SysLog | NoRepeat | 处理器 |
|------|------|------|--------|----------|--------|
| GET | `/api/v1/sys/dict/page` | `sys:dict:page` | - | - | dictPage |
| GET | `/api/v1/sys/dict/list` | `sys:dict:list` | - | - | dictList |
| GET | `/api/v1/sys/dict/tree` | `sys:dict:tree` | - | - | dictTree |
| POST | `/api/v1/sys/dict/create` | `sys:dict:create` | 添加字典 | 3000 | dictCreate |
| POST | `/api/v1/sys/dict/modify` | `sys:dict:modify` | 编辑字典 | - | dictModify |
| POST | `/api/v1/sys/dict/remove` | `sys:dict:remove` | 删除字典 | - | dictRemove |
| GET | `/api/v1/sys/dict/detail` | `sys:dict:detail` | - | - | dictDetail |
| GET | `/api/v1/sys/dict/get-label` | `sys:dict:get-label` | - | - | dictGetLabel |
| GET | `/api/v1/sys/dict/get-children` | `sys:dict:get-children` | - | - | dictGetChildren |

### Redis 缓存

```
hei:dict:tree      = {code: [{label, value, color}, ...], ...}  → 扁平查询用
hei:dict:fulltree   = [{id, code, label, value, ..., children: [...]}, ...]  → 树查询用
```

操作后通过 `syncDictCache()` 刷新到 Redis。

### service.go 关键逻辑

**Page**: 支持 parent_id（含自身）、category、keyword 过滤；sort_code ASC 排序
```go
func DictPage(c *gin.Context, param *DictPageParam) gin.H {
    query := db.Client.SysDict.Query()
    if param.ParentID != "" {
        query = query.Where(sysdict.Or(sysdict.ParentID(param.ParentID), sysdict.ID(param.ParentID)))
    }
    if param.Category != "" { query = query.Where(sysdict.CategoryEQ(param.Category)) }
    if param.Keyword != "" { query = query.Where(sysdict.LabelContains(param.Keyword)) }
    // ...
}
```

**Create/Modify**: 检查重复 label/value → 检查循环引用 → CRUD → syncCache
```go
func dictCheckDuplicate(parentID string, label, value *string, excludeID string) {
    if label != nil && *label != "" {
        q := db.Client.SysDict.Query().Where(sysdict.ParentID(parentID), sysdict.LabelEQ(*label))
        if excludeID != "" { q = q.Where(sysdict.IDNEQ(excludeID)) }
        count, _ := q.Count(ctx)
        if count > 0 { panic(exception.NewBusinessError("同一父字典下已存在相同标签: "+*label, 400)) }
    }
    if value != nil && *value != "" {
        q := db.Client.SysDict.Query().Where(sysdict.ParentID(parentID), sysdict.ValueEQ(*value))
        if excludeID != "" { q = q.Where(sysdict.IDNEQ(excludeID)) }
        count, _ := q.Count(ctx)
        if count > 0 { panic(exception.NewBusinessError("同一父字典下已存在相同值: "+*value, 400)) }
    }
}
```

**Remove**: 收集子孙节点一起删除 → syncCache
```go
func dictCollectDescendantIDs(ids []string) []string {
    all, _ := db.Client.SysDict.Query().All(ctx)
    childrenMap := make(map[string][]string)
    for _, d := range all {
        pid := ""
        if d.ParentID != nil { pid = *d.ParentID }
        childrenMap[pid] = append(childrenMap[pid], d.ID)
    }
    // DFS 收集...
}
```

**syncDictCache**: CRUD 后调用，刷新两个 Redis 缓存
```go
func syncDictCache() {
    records, _ := db.Client.SysDict.Query().Order(sysdict.BySortCode()).All(ctx)
    
    // 1. 构建 flat cache: {code: [{label, value, color}, ...]}
    childrenByParent := make(map[string][]*gen.SysDict)
    for _, r := range records {
        pid := ""
        if r.ParentID != nil { pid = *r.ParentID }
        childrenByParent[pid] = append(childrenByParent[pid], r)
    }
    
    flatCache := make(map[string][]map[string]interface{})
    for _, r := range records {
        if r.ParentID == nil || *r.ParentID == "" {
            children := childrenByParent[r.ID]
            items := make([]map[string]interface{}, 0)
            for _, c := range children {
                item := make(map[string]interface{})
                if c.Label != nil { item["label"] = *c.Label }
                if c.Value != nil { item["value"] = *c.Value }
                if c.Color != nil { item["color"] = *c.Color }
                items = append(items, item)
            }
            flatCache[r.Code] = items
        }
    }
    jsonBytes, _ := json.Marshal(flatCache)
    db.Redis.Set(ctx, constants.DICT_CACHE_KEY, string(jsonBytes), 0)
    
    // 2. 构建 tree cache
    fullTree := buildFullTree(records)
    treeBytes, _ := json.Marshal(fullTree)
    db.Redis.Set(ctx, constants.DICT_TREE_CACHE_KEY, string(treeBytes), 0)
}
```

**GetLabel**: 根据 type_code 找根节点 → 匹配 value → 返回 label
**GetChildren**: 根据 type_code 找根节点 → 返回子节点列表

---

## 5. config 模块 (`modules/sys/config/`)

### Endpoints（8 个）

| 方法 | 路径 | 权限 | SysLog | NoRepeat | 处理器 |
|------|------|------|--------|----------|--------|
| GET | `/api/v1/sys/config/page` | `sys:config:page` | - | - | configPage |
| GET | `/api/v1/sys/config/list-by-category` | `sys:config:list` | - | - | configListByCategory |
| POST | `/api/v1/sys/config/create` | `sys:config:create` | 添加配置 | 3000 | configCreate |
| POST | `/api/v1/sys/config/modify` | `sys:config:modify` | 编辑配置 | - | configModify |
| POST | `/api/v1/sys/config/remove` | `sys:config:remove` | 删除配置 | - | configRemove |
| GET | `/api/v1/sys/config/detail` | `sys:config:detail` | - | - | configDetail |
| POST | `/api/v1/sys/config/edit-batch` | `sys:config:edit` | 批量编辑配置 | 3000 | configEditBatch |
| POST | `/api/v1/sys/config/edit-by-category` | `sys:config:edit` | 按分类批量编辑配置 | 3000 | configEditByCategory |

### Redis 缓存

```
sys-config:{config_key} = config_value  // 单个值的缓存
```

Modify/Remove/EditBatch/EditByCategory 后删除对应 key 的缓存。

### service.go 关键逻辑

**Modify**: 更新后 `db.Redis.Del(ctx, "sys-config:"+configKey)`
**Remove**: 先收集所有 config_key，批量删除 Redis 缓存，再删除数据库
**EditBatch**: 按 id 批量更新，清除所有涉及的 Redis 缓存
**EditByCategory**: 按 category+config_key 匹配更新 config_value，清除 Redis 缓存
```go
func ConfigEditByCategory(c *gin.Context, param *ConfigCategoryEditParam, userID string) {
    keys := make([]string, len(param.Configs))
    for i, vo := range param.Configs { keys[i] = vo.ConfigKey }
    
    // 查询已存在的配置
    entities, _ := db.Client.SysConfig.Query().
        Where(sysconfig.CategoryEQ(param.Category), sysconfig.ConfigKeyIn(keys...)).All(ctx)
    entityMap := make(map[string]*gen.SysConfig)
    for _, e := range entities {
        if e.ConfigKey != nil { entityMap[*e.ConfigKey] = e }
    }
    
    now := time.Now()
    keysToClear := make([]string, 0)
    for _, vo := range param.Configs {
        entity, ok := entityMap[vo.ConfigKey]
        if !ok { panic(exception.NewBusinessError(
            fmt.Sprintf("分类 [%s] 下不存在配置: %s", param.Category, vo.ConfigKey), 400)) }
        
        entity.ConfigValue = &vo.ConfigValue
        entity.UpdatedAt = &now
        if userID != "" { entity.UpdatedBy = &userID }
        db.Client.SysConfig.UpdateOneID(entity.ID).
            SetConfigValue(vo.ConfigValue).SetUpdatedAt(now).
            SetNillableUpdatedBy(&userID).Exec(ctx)
        
        keysToClear = append(keysToClear, *entity.ConfigKey)
    }
    
    // 清除 Redis 缓存
    for _, k := range keysToClear {
        db.Redis.Del(ctx, "sys-config:"+k)
    }
}
```

---

## 6. permission 模块 (`modules/sys/permission/`)

**注意**：此模块是轻量只读模块，从 Redis 读取由 `HeiCheckPermission` 中间件自动发现的权限数据。所有权限定义通过 Gin 的中间件注册机制在启动时自动扫描并缓存到 Redis 的 `PERMISSION_CACHE_KEY`。

### Endpoints（2 个）

| 方法 | 路径 | 权限 | 处理器 |
|------|------|------|--------|
| GET | `/api/v1/sys/permission/modules` | `sys:permission:modules` | permListModules |
| GET | `/api/v1/sys/permission/by-module` | `sys:permission:by-module` | permByModule |

### service.go

```go
package permission

import (
    "context"
    "encoding/json"
    
    "hei-gin/core/constants"
    "hei-gin/core/db"
    "hei-gin/core/exception"
)

var ctx = context.Background()

// ListModules 从 Redis 获取权限模块列表
func ListModules(c *gin.Context) []string {
    data, err := db.Redis.Get(ctx, constants.PERMISSION_CACHE_KEY).Result()
    if err != nil {
        return []string{}
    }
    var tree map[string]interface{}
    if err := json.Unmarshal([]byte(data), &tree); err != nil {
        return []string{}
    }
    modules := make([]string, 0, len(tree))
    for k := range tree {
        modules = append(modules, k)
    }
    sort.Strings(modules)
    return modules
}

// ListByModule 从 Redis 获取指定模块的权限列表
func ListByModule(c *gin.Context, module string) []interface{} {
    data, err := db.Redis.Get(ctx, constants.PERMISSION_CACHE_KEY).Result()
    if err != nil {
        return []interface{}{}
    }
    var tree map[string]interface{}
    if err := json.Unmarshal([]byte(data), &tree); err != nil {
        return []interface{}{}
    }
    modulePerms, ok := tree[module].(map[string]interface{})
    if !ok {
        return []interface{}{}
    }
    perms := make([]interface{}, 0, len(modulePerms))
    for _, v := range modulePerms {
        perms = append(perms, v)
    }
    return perms
}
```

**数据格式**（Redis 中 `hei:permission:keys` 的 JSON 结构）:
```json
{
  "sys:user": {
    "sys:user:page": {"code": "sys:user:page", "module": "sys:user", "name": "获取用户分页"},
    "sys:user:create": {"code": "sys:user:create", "module": "sys:user", "name": "添加用户"}
  },
  "sys:role": {
    "sys:role:page": {"code": "sys:role:page", "module": "sys:role", "name": "获取角色分页"}
  }
}
```

---

## 7. home 模块 (`modules/sys/home/`)

### Endpoints（4 个）

| 方法 | 路径 | 权限 | SysLog | 处理器 |
|------|------|------|--------|--------|
| GET | `/api/v1/sys/home` | HeiCheckLogin | - | homeGet |
| POST | `/api/v1/sys/home/quick-actions/add` | HeiCheckLogin | 添加快捷方式 | homeAddQuickAction |
| POST | `/api/v1/sys/home/quick-actions/remove` | HeiCheckLogin | 移除快捷方式 | homeRemoveQuickAction |
| POST | `/api/v1/sys/home/quick-actions/sort` | HeiCheckLogin | 排序快捷方式 | homeSortQuickActions |

**注意**: home 模块不走 HeiCheckPermission，只走 HeiCheckLogin。

### params.go

```go
package home

type QuickActionVO struct {
    ID         string `json:"id,omitempty"`
    ResourceID string `json:"resource_id"`
    ParentID   string `json:"parent_id,omitempty"`
    Type       string `json:"type"`
    Name       string `json:"name"`
    Icon       string `json:"icon"`
    RoutePath  string `json:"route_path"`
    SortCode   int    `json:"sort_code"`
}

type HomeNotice struct {
    ID        string `json:"id"`
    Title     string `json:"title"`
    Level     string `json:"level"`
    CreatedAt string `json:"created_at,omitempty"`
}

type HomeStats struct {
    TotalUsers int `json:"total_users"`
}

type HomeVO struct {
    QuickActions       []QuickActionVO `json:"quick_actions"`
    AvailableResources []QuickActionVO `json:"available_resources"`
    Notices            []HomeNotice    `json:"notices"`
    Stats              HomeStats       `json:"stats"`
}

type AddQuickActionParam struct {
    ResourceID string `json:"resource_id"`
}

type RemoveQuickActionParam struct {
    ID string `json:"id"`
}

type SortQuickActionParam struct {
    IDs []string `json:"ids"`
}
```

### service.go 关键逻辑

**Home**: 查询快捷方式 + 可用资源 + 通知 + 统计
```go
func HomeGet(c *gin.Context) *HomeVO {
    userID := auth.GetLoginIDDefaultNull(c)
    
    quickActions := make([]QuickActionVO, 0)
    availableResources := make([]QuickActionVO, 0)
    
    if userID != "" {
        quickActions = findQuickActionsByUserID(userID)
        availableResources = getAvailableResources(userID)
    }
    
    notices := getNotices()
    stats := getStats()
    
    return &HomeVO{
        QuickActions:       quickActions,
        AvailableResources: availableResources,
        Notices:            notices,
        Stats:              stats,
    }
}
```

**AddQuickAction**: 检查是否已存在 → 计算 sort_code → 创建
```go
func HomeAddQuickAction(c *gin.Context, param *AddQuickActionParam) {
    userID := auth.GetLoginIDDefaultNull(c)
    if userID == "" { return }
    
    exists, _ := db.Client.SysQuickAction.Query().
        Where(sysquickaction.UserID(userID), sysquickaction.ResourceID(param.ResourceID)).Exist(ctx)
    if exists { return }
    
    count, _ := db.Client.SysQuickAction.Query().
        Where(sysquickaction.UserID(userID)).Count(ctx)
    
    db.Client.SysQuickAction.Create().
        SetID(utils.GenerateID()).
        SetUserID(userID).
        SetResourceID(param.ResourceID).
        SetSortCode((count + 1) * 10).
        Exec(ctx)
}
```

**RemoveQuickAction**: 按 ID 删除
**SortQuickActions**: 按传入 IDs 顺序重新设置 sort_code
```go
func HomeSortQuickActions(c *gin.Context, param *SortQuickActionParam) {
    userID := auth.GetLoginIDDefaultNull(c)
    if userID == "" { return }
    
    for idx, id := range param.IDs {
        db.Client.SysQuickAction.UpdateOneID(id).
            SetSortCode((idx + 1) * 10).Exec(ctx)
    }
}
```

**查询快捷方式**（跨表 JOIN 查询）:
```go
func findQuickActionsByUserID(userID string) []QuickActionVO {
    // ent 不支持跨表 JOIN，需要两次查询或使用 With 预加载
    // 方案：先查快捷方式，再用 resource IDs 查资源
    actions, _ := db.Client.SysQuickAction.Query().
        Where(sysquickaction.UserID(userID)).
        Order(sysquickaction.BySortCode(), sysquickaction.ByCreatedAt()).
        All(ctx)
    
    if len(actions) == 0 { return []QuickActionVO{} }
    
    resourceIDs := make([]string, len(actions))
    for i, a := range actions { resourceIDs[i] = a.ResourceID }
    
    resources, _ := db.Client.SysResource.Query().
        Where(sysresource.IDIn(resourceIDs...)).All(ctx)
    resourceMap := make(map[string]*gen.SysResource)
    for _, r := range resources { resourceMap[r.ID] = r }
    
    vos := make([]QuickActionVO, len(actions))
    for i, a := range actions {
        r := resourceMap[a.ResourceID]
        parentID := ""
        if r != nil && r.ParentID != nil { parentID = *r.ParentID }
        vo := QuickActionVO{
            ID: a.ID, ResourceID: a.ResourceID, SortCode: a.SortCode,
        }
        if r != nil {
            vo.Name = r.Name
            if r.Icon != nil { vo.Icon = *r.Icon }
            if r.RoutePath != nil { vo.RoutePath = *r.RoutePath }
            vo.Type = r.Type
        }
        vo.ParentID = parentID
        vos[i] = vo
    }
    return vos
}
```

---

## 路由注册

在 `core/app/router.go` 中注册：

```go
package app

import (
    "github.com/gin-gonic/gin"

    "hei-gin/modules/sys/auth"
    bannerApi "hei-gin/modules/sys/banner/api/v1"
    resourceApi "hei-gin/modules/sys/resource/api/v1"
    roleApi "hei-gin/modules/sys/role/api/v1"
    userApi "hei-gin/modules/sys/user/api/v1"
    groupApi "hei-gin/modules/sys/group/api/v1"
    dictApi "hei-gin/modules/sys/dict/api/v1"
    configApi "hei-gin/modules/sys/config/api/v1"
    homeApi "hei-gin/modules/sys/home/api/v1"
    permissionApi "hei-gin/modules/sys/permission/api/v1"
)

func SetupRouters(r *gin.Engine) {
    r.GET("/", HealthHandler)
    auth.RegisterRoutes(r)
    bannerApi.RegisterRoutes(r)
    resourceApi.RegisterRoutes(r)
    roleApi.RegisterRoutes(r)
    userApi.RegisterRoutes(r)
    groupApi.RegisterRoutes(r)
    dictApi.RegisterRoutes(r)
    configApi.RegisterRoutes(r)
    homeApi.RegisterRoutes(r)
    permissionApi.RegisterRoutes(r)
}
```

---

## 实施顺序

| 顺序 | 模块 | 说明 |
|------|------|------|
| 1 | **role** | 无外部依赖，提供 `PermissionItem` 类型供其他模块引用 |
| 2 | **user** | 依赖 role 的 `PermissionItem`，涉及 RelUserRole、RelUserPermission |
| 3 | **group** | 依赖 org 模块（直接通过 ent 查询 SysOrg） |
| 4 | **dict** | 独立模块，带 Redis 缓存同步 |
| 5 | **config** | 独立模块，带 Redis 缓存清除 |
| 6 | **permission** | 轻量只读模块，从 Redis 读取权限缓存 |
| 7 | **home** | 依赖 user/notice/resource 查询 |

---

## 各模块通用注意点

### 错误信息对照（与 FastAPI 保持一致）

| 场景 | 错误消息 | HTTP 状态码 |
|------|----------|-------------|
| 数据不存在 | `"数据不存在"` | 400 |
| 账号已存在 | `"账号已存在"` | 400 |
| 邮箱已存在 | `"邮箱已存在"` | 400 |
| 当前密码不正确 | `"当前密码不正确"` | 400 |
| 未设置密码 | `"未设置密码，无法修改"` | 400 |
| 角色存在关联用户 | `"角色存在关联用户，无法删除"` | 400 |
| 用户组存在关联用户 | `"用户组存在关联用户，无法删除"` | 400 |
| 循环父级引用 | `"父级不能选择自身或子节点"` | 400 |
| 字典重复标签 | `"同一父字典下已存在相同标签: {label}"` | 400 |
| 字典重复值 | `"同一父字典下已存在相同值: {value}"` | 400 |
| 配置不存在(edit-batch) | `"配置不存在: {id}"` | 400 |
| 配置不存在(edit-by-category) | `"分类 [{category}] 下不存在配置: {key}"` | 400 |

### 分页默认值

```go
if param.Current < 1 { param.Current = 1 }
if param.Size < 1 { param.Size = 10 }
```

### 时间格式化

```go
const TimeFormat = "2006-01-02 15:04:05"
const DateFormat = "2006-01-02"

func formatTime(t *time.Time) string {
    if t == nil { return "" }
    return t.Format(TimeFormat)
}

func formatDate(t *time.Time) string {
    if t == nil { return "" }
    return t.Format(DateFormat)
}
```

### 响应结构

所有响应通过 `result.Success(c, data)` / `result.Failure(c, msg, code, data)` / `result.PageDataResult(c, records, total, page, size)` 输出。

### 分页响应

```json
{
  "code": 200, "message": "请求成功", "success": true, "trace_id": "xxx",
  "data": {
    "records": [...], "total": 10, "page": 1, "size": 10, "pages": 1
  }
}
```

### 空详情返回

当 `detail` 查询结果为空时返回 `result.Success(c, nil)`:
```json
{"code": 200, "message": "请求成功", "data": null, "success": true, "trace_id": "xxx"}
```

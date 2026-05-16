# hei-gin sys 模块实现计划（第二阶段）— 深入版

## 概述

参考 `hei-fastapi` 的实现，为 `hei-gin` 实现剩余 7 个系统模块：**notice、position、org、file、log、session、analyze**。

每个端点和数据响应/请求必须与 FastAPI 版保持一致。本文档包含完整的端点定义、请求/响应模型、业务逻辑细节和实现注意事项。

> **ent 模型已生成**：`ent/gen/` 中已有 `SysFile`、`SysLog`、`SysNotice`、`SysOrg`、`SysPosition`。session 和 analyze 模块无独立 DB 表。

---

## 参考风格

统一采用 **banner 模块风格**：

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
| 路由注册 | `RegisterRoutes(r *gin.Engine)` 在 `api/v1/api.go` 中 |
| 中间件链 | `middleware.HeiCheckPermission([]string{"sys:module:action"})` + `log.SysLog("操作名")` + `middleware.NoRepeat(3000)` |

### 响应结构

```go
// 普通响应
result.Success(c, data) → {"code":200, "message":"请求成功", "data": data, "success":true, "trace_id":"xxx"}
result.Failure(c, msg, code, data) → {"code":code, "message":msg, "data":data, "success":false, "trace_id":"xxx"}

// 分页响应
result.PageDataResult(c, records, total, page, size) → {
  "code":200, "message":"请求成功", "success":true, "trace_id":"xxx",
  "data": {"records":[...], "total":N, "page":1, "size":10, "pages":1}
}

// 空详情返回
result.Success(c, nil) → {"code":200, "message":"请求成功", "data":null, "success":true, "trace_id":"xxx"}
```

### 时间格式化

```go
func formatTime(t *time.Time) string {
    if t == nil { return "" }
    return t.Format("2006-01-02 15:04:05")
}
func formatDate(t *time.Time) string {
    if t == nil { return "" }
    return t.Format("2006-01-02")
}
```

---

## 实施顺序

| 顺序 | 模块 | 说明 |
|------|------|------|
| 1 | **notice** | 标准 CRUD，无外部依赖，最简入手 |
| 2 | **position** | 标准 CRUD + 名称填充，需补全 FastAPI 缺失的 modify |
| 3 | **org** | 树形结构 + 循环引用检查 + 级联删除 |
| 4 | **file** | 含存储引擎（上传/下载），较复杂，可先简化 |
| 5 | **log** | CRUD + 4 个图表端点 + 更新 SysLog 中间件 |
| 6 | **session** | 纯 Redis 操作，使用 `auth.Kickout`/`auth.KickoutToken` |
| 7 | **analyze** | 聚合查询多表，1 个端点，无权限要求 |

---

## 1. notice 模块 (`modules/sys/notice/`)

### Endpoints（5 个）

| 方法 | 路径 | 权限 | SysLog | NoRepeat | 处理器 |
|------|------|------|--------|----------|--------|
| GET | `/api/v1/sys/notice/page` | `sys:notice:page` | - | - | noticePage |
| POST | `/api/v1/sys/notice/create` | `sys:notice:create` | 添加通知 | 3000 | noticeCreate |
| POST | `/api/v1/sys/notice/modify` | `sys:notice:modify` | 编辑通知 | - | noticeModify |
| POST | `/api/v1/sys/notice/remove` | `sys:notice:remove` | 删除通知 | - | noticeRemove |
| GET | `/api/v1/sys/notice/detail` | `sys:notice:detail` | - | - | noticeDetail |

### params.go

```go
package notice

type NoticeVO struct {
    ID        string  `json:"id,omitempty"`
    Title     string  `json:"title"`
    Category  string  `json:"category"`
    Type      string  `json:"type"`
    Summary   *string `json:"summary,omitempty"`
    Content   *string `json:"content,omitempty"`
    Cover     *string `json:"cover,omitempty"`
    Level     string  `json:"level,omitempty"`
    ViewCount int     `json:"view_count,omitempty"`
    IsTop     string  `json:"is_top,omitempty"`
    Position  *string `json:"position,omitempty"`
    Status    string  `json:"status,omitempty"`
    SortCode  int     `json:"sort_code,omitempty"`
    CreatedAt string  `json:"created_at,omitempty"`
    CreatedBy *string `json:"created_by,omitempty"`
    UpdatedAt string  `json:"updated_at,omitempty"`
    UpdatedBy *string `json:"updated_by,omitempty"`
}

type NoticePageParam struct {
    Current int `json:"current" form:"current"`
    Size    int `json:"size" form:"size"`
}
```

**注意**: FastAPI 版 `NoticeVO` 中 `title`、`category`、`type` 是**必填字段**（非指针），对应 ent 中也是非空 string 类型。`level` 默认值 `"NORMAL"`、`status` 默认值 `"ENABLED"`、`is_top` 默认值 `"NO"`、`view_count` 默认值 `0`、`sort_code` 默认值 `0`。

### service.go 关键实现

**entToVO**:
```go
func entToVO(entity *gen.SysNotice) *NoticeVO {
    if entity == nil { return nil }
    return &NoticeVO{
        ID:        entity.ID,
        Title:     entity.Title,
        Category:  entity.Category,
        Type:      entity.Type,
        Summary:   entity.Summary,
        Content:   entity.Content,
        Cover:     entity.Cover,
        Level:     entity.Level,
        ViewCount: entity.ViewCount,
        IsTop:     entity.IsTop,
        Position:  entity.Position,
        Status:    entity.Status,
        SortCode:  entity.SortCode,
        CreatedAt: formatTime(entity.CreatedAt),
        CreatedBy: entity.CreatedBy,
        UpdatedAt: formatTime(entity.UpdatedAt),
        UpdatedBy: entity.UpdatedBy,
    }
}
```

**Page**: `db.Client.SysNotice.Query().Order(sysnotice.ByCreatedAt(entsql.OrderDesc())).Limit(size).Offset(offset).All(ctx)`

**Create**:
```go
func Create(c *gin.Context, vo *NoticeVO, userID string) {
    builder := db.Client.SysNotice.Create().
        SetID(utils.GenerateID()).
        SetTitle(vo.Title).SetCategory(vo.Category).SetType(vo.Type).
        SetLevel(vo.Level).SetIsTop(vo.IsTop).SetStatus(vo.Status).
        SetViewCount(vo.ViewCount).SetSortCode(vo.SortCode).
        SetCreatedAt(now).SetUpdatedAt(now)
    if vo.Summary != nil { builder.SetNillableSummary(vo.Summary) }
    if vo.Content != nil { builder.SetNillableContent(vo.Content) }
    if vo.Cover != nil { builder.SetNillableCover(vo.Cover) }
    if vo.Position != nil { builder.SetNillablePosition(vo.Position) }
    if userID != "" { builder.SetCreatedBy(userID).SetUpdatedBy(userID) }
    builder.Save(ctx)
}
```

**Modify**: 先查存在性（不存在 panic `"数据不存在"` 400），再 UpdateOneID 更新。

**Remove**: `db.Client.SysNotice.Delete().Where(sysnotice.IDIn(ids...)).Exec(ctx)`

**Detail**: `db.Client.SysNotice.Get(ctx, id)` → entToVO

### API 处理器

```go
func createHandler(c *gin.Context) {
    var vo notice.NoticeVO
    if err := c.ShouldBindJSON(&vo); err != nil {
        c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
        return
    }
    userID := auth.GetLoginIDDefaultNull(c)
    notice.Create(c, &vo, userID)
    c.JSON(200, result.Success(c, nil))
}

func removeHandler(c *gin.Context) {
    var param struct { IDs []string `json:"ids"` }
    if err := c.ShouldBindJSON(&param); err != nil { ... }
    notice.Remove(c, param.IDs)
    c.JSON(200, result.Success(c, nil))
}
```

---

## 2. position 模块 (`modules/sys/position/`)

### Endpoints（5 个）

| 方法 | 路径 | 权限 | SysLog | NoRepeat | 处理器 |
|------|------|------|--------|----------|--------|
| GET | `/api/v1/sys/position/page` | `sys:position:page` | 查看职位列表 | - | positionPage |
| POST | `/api/v1/sys/position/create` | `sys:position:create` | 添加职位 | 3000 | positionCreate |
| POST | `/api/v1/sys/position/modify` | `sys:position:modify` | 编辑职位 | - | positionModify |
| POST | `/api/v1/sys/position/remove` | `sys:position:remove` | 删除职位 | - | positionRemove |
| GET | `/api/v1/sys/position/detail` | `sys:position:detail` | - | - | positionDetail |

**关键发现**: FastAPI 版 page 端点有 `@SysLog("查看职位列表")`，Gin 版需要保留此 SysLog。FastAPI 版 service 没有 `modify` 方法（API 中有调用但会运行时错误），Gin 版需要自己实现。

**Page 关键逻辑**: FastAPI 版当 `group_id` 为空时直接返回空数据：
```python
def page(self, param: PositionPageParam) -> dict:
    if not param.group_id:
        return page_data(records=[], total=0, page=param.current, size=param.size)
    # ...
```

### params.go

```go
package position

type PositionVO struct {
    ID          string   `json:"id,omitempty"`
    Code        string   `json:"code"`
    Name        string   `json:"name"`
    Category    string   `json:"category"`
    OrgID       *string  `json:"org_id,omitempty"`
    GroupID     *string  `json:"group_id,omitempty"`
    Description *string  `json:"description,omitempty"`
    Status      string   `json:"status,omitempty"`
    SortCode    int      `json:"sort_code,omitempty"`
    OrgNames    []string `json:"org_names,omitempty"`
    GroupNames  []string `json:"group_names,omitempty"`
    Extra       *string  `json:"extra,omitempty"`
    CreatedAt   string   `json:"created_at,omitempty"`
    CreatedBy   *string  `json:"created_by,omitempty"`
    UpdatedAt   string   `json:"updated_at,omitempty"`
    UpdatedBy   *string  `json:"updated_by,omitempty"`
}

type PositionPageParam struct {
    Current int    `json:"current" form:"current"`
    Size    int    `json:"size" form:"size"`
    Keyword string `json:"keyword,omitempty" form:"keyword"`
    GroupID string `json:"group_id,omitempty" form:"group_id"`
    OrgID   string `json:"org_id,omitempty" form:"org_id"`
}
```

### service.go 关键实现

**Page**: 
- `group_id` 为空 → 直接返回空数据
- 支持 `keyword`（`name` LIKE）、`group_id`、`org_id` 过滤
- 按 `sort_code ASC` 排序
- 结果批量填充 `org_names`、`group_names`（从 SysOrg/SysGroup 表查询名称路径）

**名称路径填充**:
```go
func enrichPositionVO(vo *PositionVO) {
    if vo.OrgID != nil && *vo.OrgID != "" {
        org, _ := db.Client.SysOrg.Get(ctx, *vo.OrgID)
        // 递归解析父节点获取名称路径
    }
    if vo.GroupID != nil && *vo.GroupID != "" {
        group, _ := db.Client.SysGroup.Get(ctx, *vo.GroupID)
        // 递归解析父节点获取名称路径
    }
}
```

**名称路径解析**（参考 group/user 模块的 `buildNamePathMap`）:
```go
func resolveNamePath(id *string, nodeMap map[string]NamePathNode) []string {
    if id == nil || *id == "" { return nil }
    var path []string
    current := *id
    for current != "" {
        node, ok := nodeMap[current]
        if !ok { break }
        path = append([]string{node.Name}, path...)
        current = node.ParentID
    }
    return path
}
```

**Create**: 标准创建，`strip_system_fields` 排除系统字段后创建 ent 实体

**Modify**: 需要自行实现（FastAPI 缺失）:
```go
func Modify(c *gin.Context, vo *PositionVO, userID string) {
    entity, err := db.Client.SysPosition.Get(ctx, vo.ID)
    if err != nil {
        if gen.IsNotFound(err) { panic(exception.NewBusinessError("数据不存在", 400)) }
        panic(...)
    }
    builder := db.Client.SysPosition.UpdateOneID(vo.ID).
        SetCode(vo.Code).SetName(vo.Name).SetCategory(vo.Category).
        SetSortCode(vo.SortCode).SetUpdatedAt(time.Now())
    if vo.OrgID != nil { builder.SetNillableOrgID(vo.OrgID) } else { builder.ClearOrgID() }
    if vo.GroupID != nil { builder.SetNillableGroupID(vo.GroupID) } else { builder.ClearGroupID() }
    if vo.Description != nil { builder.SetNillableDescription(vo.Description) }
    if vo.Status != "" { builder.SetStatus(vo.Status) }
    if vo.Extra != nil { builder.SetNillableExtra(vo.Extra) }
    if userID != "" { builder.SetUpdatedBy(userID) }
    builder.Save(ctx)
}
```

**Remove**: 删除前检查 SysUser 是否关联：
```go
func Remove(c *gin.Context, ids []string) {
    count, err := db.Client.SysUser.Query().Where(sysuser.PositionIDIn(ids...)).Count(ctx)
    if count > 0 { panic(exception.NewBusinessError("职位存在关联用户，无法删除", 400)) }
    // 断开用户 position_id 引用
    db.Client.SysUser.Update().Where(sysuser.PositionIDIn(ids...)).ClearPositionID().Exec(ctx)
    // 删除职位
    db.Client.SysPosition.Delete().Where(sysposition.IDIn(ids...)).Exec(ctx)
}
```

**Detail**: 标准查询 + 填充 org_names/group_names

---

## 3. org 模块 (`modules/sys/org/`)

### Endpoints（6 个）

| 方法 | 路径 | 权限 | SysLog | NoRepeat | 处理器 |
|------|------|------|--------|----------|--------|
| GET | `/api/v1/sys/org/page` | `sys:org:page` | - | - | orgPage |
| GET | `/api/v1/sys/org/tree` | `sys:org:tree` | - | - | orgTree |
| POST | `/api/v1/sys/org/create` | `sys:org:create` | 添加组织 | 3000 | orgCreate |
| POST | `/api/v1/sys/org/modify` | `sys:org:modify` | 编辑组织 | - | orgModify |
| POST | `/api/v1/sys/org/remove` | `sys:org:remove` | 删除组织 | - | orgRemove |
| GET | `/api/v1/sys/org/detail` | `sys:org:detail` | - | - | orgDetail |

### params.go

```go
package org

type OrgVO struct {
    ID          string  `json:"id,omitempty"`
    Code        string  `json:"code"`
    Name        string  `json:"name"`
    Category    string  `json:"category"`
    ParentID    *string `json:"parent_id,omitempty"`
    Description *string `json:"description,omitempty"`
    Status      string  `json:"status,omitempty"`
    SortCode    int     `json:"sort_code,omitempty"`
    Extra       *string `json:"extra,omitempty"`
    CreatedAt   string  `json:"created_at,omitempty"`
    CreatedBy   *string `json:"created_by,omitempty"`
    UpdatedAt   string  `json:"updated_at,omitempty"`
    UpdatedBy   *string `json:"updated_by,omitempty"`
}

type OrgPageParam struct {
    Current  int    `json:"current" form:"current"`
    Size     int    `json:"size" form:"size"`
    ParentID string `json:"parent_id,omitempty" form:"parent_id"`
    Keyword  string `json:"keyword,omitempty" form:"keyword"`
}

type OrgTreeParam struct {
    Category string `json:"category,omitempty" form:"category"`
}
```

### service.go 关键实现

**Page**: 
- `parent_id` 不为空时：`WHERE parent_id = ? OR id = ?`（即同时返回父节点自身和子节点）
- `keyword` 模糊匹配 name
- 按 `sort_code ASC` 排序

```go
func Page(c *gin.Context, param *OrgPageParam) gin.H {
    query := db.Client.SysOrg.Query()
    if param.ParentID != "" {
        query = query.Where(sysorg.Or(sysorg.ParentID(param.ParentID), sysorg.ID(param.ParentID)))
    }
    if param.Keyword != "" {
        query = query.Where(sysorg.NameContains(param.Keyword))
    }
    total, _ := query.Clone().Count(ctx)
    records, _ := query.Clone().Order(sysorg.BySortCode()).
        Limit(param.Size).Offset((param.Current - 1) * param.Size).All(ctx)
    // ...
}
```

**Tree**: 查询所有组织（按 sort_code ASC），按 category 过滤 → 构建树：
```go
func Tree(c *gin.Context, param *OrgTreeParam) []map[string]interface{} {
    records, _ := db.Client.SysOrg.Query().Order(sysorg.BySortCode()).All(ctx)
    
    var filtered []*gen.SysOrg
    if param.Category != "" {
        for _, r := range records {
            if r.Category == param.Category { filtered = append(filtered, r) }
        }
    } else {
        filtered = records
    }
    
    nodeMap := make(map[string]map[string]interface{})
    for _, r := range filtered {
        node := orgToVOMap(r)
        node["children"] = make([]map[string]interface{}, 0)
        nodeMap[r.ID] = node
    }
    
    roots := make([]map[string]interface{}, 0)
    for _, node := range nodeMap {
        pid := ""
        if p, ok := node["parent_id"].(*string); ok && p != nil { pid = *p }
        if parent, ok := nodeMap[pid]; ok {
            parent["children"] = append(parent["children"].([]map[string]interface{}), node)
        } else {
            roots = append(roots, node)
        }
    }
    
    sortOrgTree(roots) // 递归按 sort_code 排序
    return roots
}
```

**Create**: 标准创建

**Modify**:
- 先查存在性，不存在 panic `"数据不存在"`
- 如果 `parent_id` 变化，检查循环引用：

```go
func checkCircularRef(ctx context.Context, entityID string, newParentID *string) {
    if newParentID == nil || *newParentID == "" { return }
    all, _ := db.Client.SysOrg.Query().All(ctx)
    parentMap := make(map[string]string)
    for _, o := range all {
        if o.ParentID != nil { parentMap[o.ID] = *o.ParentID }
    }
    current := *newParentID
    for current != "" {
        if current == entityID {
            panic(exception.NewBusinessError("父级不能选择自身或子节点", 400))
        }
        current = parentMap[current]
    }
}
```

**Remove**: 收集所有子孙节点 → 检查关联 → 断开引用 → 删除

```go
func Remove(c *gin.Context, ids []string) {
    allIDs := collectDescendantOrgIDs(ids)
    
    // 检查 SysUser 关联
    count, _ := db.Client.SysUser.Query().Where(sysuser.OrgIDIn(allIDs...)).Count(ctx)
    if count > 0 { panic(exception.NewBusinessError("组织存在关联用户，无法删除", 400)) }
    
    // 检查 SysGroup 关联
    count, _ = db.Client.SysGroup.Query().Where(sysgroup.OrgIDIn(allIDs...)).Count(ctx)
    if count > 0 { panic(exception.NewBusinessError("组织下存在用户组，无法删除", 400)) }
    
    // 断开 SysPosition 的 org_id 引用
    db.Client.SysPosition.Update().Where(sysposition.OrgIDIn(allIDs...)).ClearOrgID().Exec(ctx)
    
    // 删除
    db.Client.SysOrg.Delete().Where(sysorg.IDIn(allIDs...)).Exec(ctx)
}
```

**collectDescendantOrgIDs**: 从所有记录构建 `childrenMap`，DFS/BFS 收集所有子孙节点 ID

```go
func collectDescendantOrgIDs(ids []string) []string {
    all, _ := db.Client.SysOrg.Query().All(ctx)
    childrenMap := make(map[string][]string)
    for _, o := range all {
        pid := ""
        if o.ParentID != nil { pid = *o.ParentID }
        childrenMap[pid] = append(childrenMap[pid], o.ID)
    }
    collected := make(map[string]bool)
    var dfs func(id string)
    dfs = func(id string) {
        if collected[id] { return }
        collected[id] = true
        for _, child := range childrenMap[id] {
            dfs(child)
        }
    }
    for _, id := range ids { dfs(id) }
    result := make([]string, 0, len(collected))
    for id := range collected { result = append(result, id) }
    return result
}
```

---

## 4. file 模块 (`modules/sys/file/`)

### Endpoints（6 个）

| 方法 | 路径 | 权限 | SysLog | 处理器 |
|------|------|------|--------|--------|
| POST | `/api/v1/sys/file/upload` | `sys:file:upload`(手动检查) | 上传文件 | fileUpload |
| GET | `/api/v1/sys/file/download` | `sys:file:download` | - | fileDownload |
| GET | `/api/v1/sys/file/page` | `sys:file:page` | - | filePage |
| GET | `/api/v1/sys/file/detail` | `sys:file:detail` | - | fileDetail |
| POST | `/api/v1/sys/file/remove` | `sys:file:remove` | 删除文件 | fileRemove |
| POST | `/api/v1/sys/file/remove-absolute` | `sys:file:remove` | 物理删除文件 | fileRemoveAbsolute |

**上传权限检查特殊性**: FastAPI 版 upload 端点使用**手动**权限检查（`HeiPermissionTool.hasPermissionAnd`），同时用 `upload._hei_permission = "sys:file:upload"` 属性注册权限。推荐 Gin 版也使用手动检查或调整中间件。

**注意**: upload 和 remove-absolute 端点涉及文件存储操作，需要处理 multipart/form-data。

### params.go

```go
package file

type FileVO struct {
    ID             string  `json:"id,omitempty"`
    Engine         *string `json:"engine,omitempty"`
    Bucket         *string `json:"bucket,omitempty"`
    FileKey        *string `json:"file_key,omitempty"`
    Name           *string `json:"name,omitempty"`
    Suffix         *string `json:"suffix,omitempty"`
    SizeKB         *int64  `json:"size_kb,omitempty"`
    SizeInfo       *string `json:"size_info,omitempty"`
    ObjName        *string `json:"obj_name,omitempty"`
    StoragePath    *string `json:"storage_path,omitempty"`
    DownloadPath   *string `json:"download_path,omitempty"`
    IsDownloadAuth *int    `json:"is_download_auth,omitempty"`
    Thumbnail      *string `json:"thumbnail,omitempty"`
    Extra          *string `json:"extra,omitempty"`
    CreatedAt      string  `json:"created_at,omitempty"`
    CreatedBy      *string `json:"created_by,omitempty"`
}

type FilePageParam struct {
    Current        int    `json:"current" form:"current"`
    Size           int    `json:"size" form:"size"`
    Engine         string `json:"engine,omitempty" form:"engine"`
    Keyword        string `json:"keyword,omitempty" form:"keyword"`
    DateRangeStart string `json:"date_range_start,omitempty" form:"date_range_start"`
    DateRangeEnd   string `json:"date_range_end,omitempty" form:"date_range_end"`
}

type FileIdParam struct {
    ID string `json:"id"`
}
```

**注意**: FastAPI 版 `FileVO` 中 `is_download_auth` 是 `Optional[int]`（0/1），不是 bool。`size_kb` 是 `Optional[int]`（ent 中是 `*int64`）。

**注意**: FastAPI 版 `FileVO` 不包含 `updated_at`/`updated_by` 字段，Gin 版也不需要。

### service.go 关键实现

**SysFile ent 模型**: 所有字段均为指针类型，注意以下映射：

| 字段 | ent 类型 | Gin VO 类型 | 说明 |
|------|----------|-------------|------|
| SizeKB | *int64 | *int64 | ent 生成为 *int64 |
| IsDownloadAuth | *bool | *int | FastAPI 是 *int，需做转换 |
| Engine/Bucket/Name 等 | *string | *string | 直接映射 |

**Upload 第一阶段简化实现**:
```go
func Upload(c *gin.Context) gin.H {
    // 1. 解析 multipart form
    file, header, err := c.Request.FormFile("file")
    engine := c.Request.FormValue("engine") // 可选
    
    // 2. 读取文件内容
    data, _ := io.ReadAll(file)
    sizeBytes := len(data)
    suffix := strings.ToLower(filepath.Ext(header.Filename))
    fileID := utils.GenerateID()
    
    // 3. 保存到本地
    savePath := "uploads/" + fileID + suffix
    os.MkdirAll(filepath.Dir(savePath), 0755)
    os.WriteFile(savePath, data, 0644)
    
    // 4. 生成缩略图（图片文件）
    // 5. 创建 DB 记录
    entity := db.Client.SysFile.Create().
        SetID(fileID).
        SetName(header.Filename).
        SetEngine("LOCAL").
        SetSuffix(suffix).
        SetSizeKB(int64(sizeBytes / 1024)).
        SetSizeInfo(formatSize(sizeBytes)).
        SetStoragePath(savePath).
        SetDownloadPath(fmt.Sprintf("/api/v1/sys/file/download?id=%s", fileID)).
        SetCreatedAt(time.Now())
    // ...
    
    return gin.H{
        "id": fileID,
        "name": header.Filename,
        "engine": "LOCAL",
        "download_path": downloadPath,
        "size_info": sizeInfo,
    }
}
```

**Download**:
```go
func Download(c *gin.Context, id string) {
    entity, err := db.Client.SysFile.Get(ctx, id)
    if err != nil { panic(exception.NewBusinessError("文件不存在", 404)) }
    
    data, _ := os.ReadFile(*entity.StoragePath)
    filename := url.QueryEscape(*entity.Name)
    
    c.Header("Content-Disposition", `attachment; filename*=UTF-8''`+filename)
    c.Data(200, "application/octet-stream", data)
}
```

**Page**: 支持 engine、keyword（name LIKE）、date_range_start/end 过滤，按 `created_at DESC` 排序

**Remove**（逻辑删除）: 仅删 DB 记录：`db.Client.SysFile.Delete().Where(sysfile.IDIn(ids...)).Exec(ctx)`

**RemoveAbsolute**（物理删除）: 先查实体获取 storage_path → 删本地文件 → 删 DB 记录

---

## 5. log 模块 (`modules/sys/log/`)

### Endpoints（10 个）

| 方法 | 路径 | 权限 | SysLog | NoRepeat | 处理器 |
|------|------|------|--------|----------|--------|
| GET | `/api/v1/sys/log/page` | `sys:log:page` | - | - | logPage |
| POST | `/api/v1/sys/log/create` | `sys:log:create` | - | - | logCreate |
| POST | `/api/v1/sys/log/modify` | `sys:log:modify` | - | - | logModify |
| POST | `/api/v1/sys/log/remove` | `sys:log:remove` | 删除操作日志 | - | logRemove |
| GET | `/api/v1/sys/log/detail` | `sys:log:detail` | - | - | logDetail |
| POST | `/api/v1/sys/log/delete-by-category` | `sys:log:remove` | 按分类清空日志 | 5000 | logDeleteByCategory |
| GET | `/api/v1/sys/log/vis/line-chart-data` | `sys:log:page` | - | - | logVisLineChart |
| GET | `/api/v1/sys/log/vis/pie-chart-data` | `sys:log:page` | - | - | logVisPieChart |
| GET | `/api/v1/sys/log/op/bar-chart-data` | `sys:log:page` | - | - | logOpBarChart |
| GET | `/api/v1/sys/log/op/pie-chart-data` | `sys:log:page` | - | - | logOpPieChart |

**关键发现**:
1. FastAPI 版 `create`/`modify` 端点在 API 中有定义但 service 中**没有实现**，运行时出错。Gin 版需补全。
2. FastAPI 版 `remove` 端点也缺少 service 层 `remove()` 方法，但 DAO 层有 `delete_by_ids`。
3. `create`/`modify` 端点**没有** `@SysLog` 装饰器（避免循环记录）。
4. `delete-by-category` 有 `@NoRepeat(interval=5000)`（5秒防重复）。
5. 图表端点都使用 `sys:log:page` 权限码。
6. `vis` 系列 = LOGIN/LOGOUT，`op` 系列 = OPERATE/EXCEPTION。
7. page 查询排除 `param_json`, `result_json`, `exe_message`, `sign_data` 四个大字段（仅在 detail 时返回）。

### params.go

```go
package log

type LogVO struct {
    ID         string `json:"id,omitempty"`
    Category   string `json:"category,omitempty"`
    Name       string `json:"name,omitempty"`
    ExeStatus  string `json:"exe_status,omitempty"`
    ExeMessage string `json:"exe_message,omitempty"`
    OpIP       string `json:"op_ip,omitempty"`
    OpAddress  string `json:"op_address,omitempty"`
    OpBrowser  string `json:"op_browser,omitempty"`
    OpOs       string `json:"op_os,omitempty"`
    ClassName  string `json:"class_name,omitempty"`
    MethodName string `json:"method_name,omitempty"`
    ReqMethod  string `json:"req_method,omitempty"`
    ReqURL     string `json:"req_url,omitempty"`
    ParamJSON  string `json:"param_json,omitempty"`
    ResultJSON string `json:"result_json,omitempty"`
    OpTime     string `json:"op_time,omitempty"`
    TraceID    string `json:"trace_id,omitempty"`
    OpUser     string `json:"op_user,omitempty"`
    SignData   string `json:"sign_data,omitempty"`
    CreatedAt  string `json:"created_at,omitempty"`
    CreatedBy  string `json:"created_by,omitempty"`
    UpdatedAt  string `json:"updated_at,omitempty"`
    UpdatedBy  string `json:"updated_by,omitempty"`
}

type LogPageParam struct {
    Current   int    `json:"current" form:"current"`
    Size      int    `json:"size" form:"size"`
    Keyword   string `json:"keyword,omitempty" form:"keyword"`
    Category  string `json:"category,omitempty" form:"category"`
    ExeStatus string `json:"exe_status,omitempty" form:"exe_status"`
}

type LogDeleteByCategoryParam struct {
    Category string `json:"category"`
}

// 图表数据结构
type BarChartData struct {
    Days   []string         `json:"days"`
    Series []CategorySeries `json:"series"`
}

type CategorySeries struct {
    Name string `json:"name"`
    Data []int  `json:"data"`
}

type PieChartData struct {
    Data []CategoryTotal `json:"data"`
}

type CategoryTotal struct {
    Category string `json:"category"`
    Total    int    `json:"total"`
}
```

### service.go 关键实现

**Page**: 支持 keyword（name LIKE）、category、exe_status 过滤，按 `op_time DESC` 排序。Page 列表排除大字段（param_json/result_json/exe_message/sign_data），仅 detail 时返回全部。

```go
func Page(c *gin.Context, param *LogPageParam) gin.H {
    query := db.Client.SysLog.Query()
    if param.Keyword != "" { query = query.Where(syslog.NameContains(param.Keyword)) }
    if param.Category != "" { query = query.Where(syslog.CategoryEQ(param.Category)) }
    if param.ExeStatus != "" { query = query.Where(syslog.ExeStatusEQ(param.ExeStatus)) }
    total, _ := query.Clone().Count(ctx)
    records, _ := query.Clone().Order(syslog.ByOpTime(entsql.OrderDesc())).
        Limit(param.Size).Offset((param.Current - 1) * param.Size).All(ctx)
    // ...
}
```

**注意**: Page 需要排除大字段。ent 不支持 select 特定字段，但可以返回全部字段后在前端处理，或使用 `Select` 方法：
```go
records, _ := query.Clone().Select(
    syslog.FieldID, syslog.FieldCategory, syslog.FieldName, 
    syslog.FieldExeStatus, /* 不选 ExeMessage, ParamJSON, ResultJSON, SignData */
).All(ctx)
```

**Create**: 接收 LogVO 所有字段，插入 sys_log 表

**Modify**: 按 id 更新。先查存在性。

**Detail**: 按 id 查询，返回完整 LogVO（包括大字段）

**Remove**: 按 ids 删除（DAO 层 `delete_by_ids`）

**DeleteByCategory**: `db.Client.SysLog.Delete().Where(syslog.CategoryEQ(param.Category)).Exec(ctx)`

**VisLineChart**: 近7天 LOGIN/LOGOUT 每日数量
```go
func VisLineChart(c *gin.Context) *BarChartData {
    since := time.Now().AddDate(0, 0, -6)
    since = time.Date(since.Year(), since.Month(), since.Day(), 0, 0, 0, 0, time.Local)
    
    // 查询近7天 LOGIN/LOGOUT 每天的计数
    // ent 不支持直接 GROUP BY DATE，需要手动查询
    records, _ := db.Client.SysLog.Query().
        Where(syslog.CategoryIn("LOGIN", "LOGOUT"), syslog.OpTimeGTE(since)).
        All(ctx)
    
    days := lastNDays(7)
    dayMap := make(map[string]map[string]int)
    for _, r := range records {
        if r.OpTime != nil {
            day := r.OpTime.Format("2006-01-02")
            if dayMap[day] == nil { dayMap[day] = make(map[string]int) }
            dayMap[day][r.Category]++
        }
    }
    
    return &BarChartData{
        Days: days,
        Series: []CategorySeries{
            {Name: "登录", Data: dailyData(days, dayMap, "LOGIN")},
            {Name: "登出", Data: dailyData(days, dayMap, "LOGOUT")},
        },
    }
}
```

**VisPieChart**: LOGIN/LOGOUT 总数量
```go
func VisPieChart(c *gin.Context) *PieChartData {
    loginTotal, _ := db.Client.SysLog.Query().Where(syslog.CategoryEQ("LOGIN")).Count(ctx)
    logoutTotal, _ := db.Client.SysLog.Query().Where(syslog.CategoryEQ("LOGOUT")).Count(ctx)
    return &PieChartData{
        Data: []CategoryTotal{
            {Category: "登录", Total: loginTotal},
            {Category: "登出", Total: logoutTotal},
        },
    }
}
```

**OpBarChart**: 近7天 OPERATE/EXCEPTION 每日数量（逻辑同 VisLineChart，但用 "操作"/"异常" 标签）

**OpPieChart**: OPERATE/EXCEPTION 总数量（逻辑同 VisPieChart，但用 "操作"/"异常" 标签）

### 更新 SysLog 中间件

当前 `core/log/syslog.go` 只打印到控制台，需要写入 `sys_log` 表：

```go
func saveLog(c *gin.Context, name, category, exeStatus, exeMessage, paramsJSON string, startTime time.Time) {
    userAgent := c.GetHeader("User-Agent")
    browser, osName := utils.ParseUserAgent(userAgent)
    opIP := utils.GetClientIP(c)
    cityInfo := utils.GetCityInfo(opIP)
    traceID := utils.GetTraceID()
    
    opUserStr, _ := c.Get("loginUser")
    opUser, _ := opUserStr.(string)
    
    ctx := context.Background()
    db.Client.SysLog.Create().
        SetID(utils.GenerateID()).
        SetCategory(category).
        SetName(name).
        SetExeStatus(exeStatus).
        SetNillableExeMessage(&exeMessage).
        SetOpIP(opIP).
        SetOpAddress(cityInfo).
        SetOpBrowser(browser).
        SetOpOs(osName).
        SetReqMethod(c.Request.Method).
        SetReqURL(c.Request.URL.String()).
        SetParamJSON(paramsJSON).
        SetOpTime(time.Now()).
        SetTraceID(traceID).
        SetOpUser(opUser).
        SetCreatedAt(time.Now()).
        SetUpdatedAt(time.Now()).
        Exec(ctx)
}
```

**注意**: SysLog 中间件所有字段均为 `*string` 类型，需要使用 `SetXxx` 或 `SetNillableXxx`。

---

## 6. session 模块 (`modules/sys/session/`)

**无 DB 表**，纯 Redis 操作。通过 `auth.Kickout()` / `auth.KickoutToken()` 等工具函数操作 Redis 中的 token/session 数据。

**Redis 键结构**:
- `hei:auth:BUSINESS:token:<token>` → `{"user_id":"...", "type":"BUSINESS", "created_at":"...", "extra":{...}}`
- `hei:auth:BUSINESS:session:<userID>` → Set of tokens
- `hei:auth:BUSINESS:disable:<loginID>` → disable flag
- 同理 CONSUMER 前缀

**常量**（已在 `core/constants/cache_keys.go` 中定义）:
- `TOKEN_PREFIX_BUSINESS`, `SESSION_PREFIX_BUSINESS`
- `TOKEN_PREFIX_CONSUMER`, `SESSION_PREFIX_CONSUMER`

### Endpoints（6 个）

| 方法 | 路径 | 权限 | 处理器 | 说明 |
|------|------|------|--------|------|
| GET | `/api/v1/sys/session/analysis` | `sys:session:page` | sessionAnalysis | 会话分析统计 |
| GET | `/api/v1/sys/session/page` | `sys:session:page` | sessionPage | B端在线用户分页 |
| POST | `/api/v1/sys/session/exit` | `sys:session:exit` | sessionExit | 强退B端用户 |
| GET | `/api/v1/sys/session/tokens` | `sys:session:page` | sessionTokens | 用户令牌列表 |
| POST | `/api/v1/sys/session/exit-token` | `sys:session:exit` | sessionExitToken | 强退指定令牌 |
| GET | `/api/v1/sys/session/chart-data` | `sys:session:page` | sessionChartData | 会话图表数据 |

**注意**: 所有 session 端点**没有** `SysLog` 和 `NoRepeat` 装饰器，只有 `HeiCheckPermission`。

### params.go

```go
package session

type SessionAnalysisResult struct {
    TotalCount        int    `json:"total_count"`
    MaxTokenCount     int    `json:"max_token_count"`
    OneHourNewlyAdded int    `json:"one_hour_newly_added"`
    ProportionOfBAndC string `json:"proportion_of_b_and_c"`
}

type SessionPageResult struct {
    UserID                string  `json:"user_id,omitempty"`
    Username              *string `json:"username,omitempty"`
    Nickname              *string `json:"nickname,omitempty"`
    Avatar                *string `json:"avatar,omitempty"`
    Status                string  `json:"status,omitempty"`
    LastLoginIP           *string `json:"last_login_ip,omitempty"`
    LastLoginAddress      *string `json:"last_login_address,omitempty"`
    LastLoginTime         string  `json:"last_login_time,omitempty"`
    SessionCreateTime     string  `json:"session_create_time,omitempty"`
    SessionTimeout        string  `json:"session_timeout,omitempty"`
    SessionTimeoutSeconds int     `json:"session_timeout_seconds,omitempty"`
    TokenCount            int     `json:"token_count"`
}

type SessionExitParam struct {
    UserID string `json:"user_id"`
}

type SessionExitTokenParam struct {
    UserID string `json:"user_id"`
    Token  string `json:"token"`
}

type SessionTokenResult struct {
    Token          string `json:"token,omitempty"`
    CreatedAt      string `json:"created_at,omitempty"`
    Timeout        string `json:"timeout,omitempty"`
    TimeoutSeconds int    `json:"timeout_seconds,omitempty"`
    DeviceType     string `json:"device_type,omitempty"`
    DeviceID       string `json:"device_id,omitempty"`
}

type SessionPageParam struct {
    Current int    `json:"current" form:"current"`
    Size    int    `json:"size" form:"size"`
    Keyword string `json:"keyword,omitempty" form:"keyword"`
}

type SessionChartData struct {
    BarChart BarChartData `json:"bar_chart"`
    PieChart PieChartData `json:"pie_chart"`
}

// 图表结构复用 log 模块的定义
type BarChartData struct {
    Days   []string         `json:"days"`
    Series []CategorySeries `json:"series"`
}

type PieChartData struct {
    Data []CategoryTotal `json:"data"`
}

type CategorySeries struct {
    Name string `json:"name"`
    Data []int  `json:"data"`
}

type CategoryTotal struct {
    Category string `json:"category"`
    Total    int    `json:"total"`
}
```

### service.go 关键实现

所有的 session service 函数使用包级函数（同 banner 风格），参数为 `*gin.Context`。

**Analysis**: 统计 B 端和 C 端的 token 数据
```go
func Analysis(c *gin.Context) *SessionAnalysisResult {
    ctx := context.Background()
    redisClient := db.Redis
    
    // SCAN B端 session keys
    bKeys, _ := scanKeys(ctx, redisClient, constants.SESSION_PREFIX_BUSINESS+"*")
    cKeys, _ := scanKeys(ctx, redisClient, constants.SESSION_PREFIX_CONSUMER+"*")
    
    bTotal, bNew, bMax := countTokens(ctx, redisClient, bKeys, constants.TOKEN_PREFIX_BUSINESS)
    cTotal, cNew, cMax := countTokens(ctx, redisClient, cKeys, constants.TOKEN_PREFIX_CONSUMER)
    
    return &SessionAnalysisResult{
        TotalCount:        bTotal + cTotal,
        MaxTokenCount:     max(bMax, cMax),
        OneHourNewlyAdded: bNew + cNew,
        ProportionOfBAndC: fmt.Sprintf("%d/%d", bTotal, cTotal),
    }
}
```

**scanKeys**: 使用 Redis SCAN 命令遍历匹配的 key
```go
func scanKeys(ctx context.Context, redis *redis.Client, pattern string) ([]string, error) {
    var keys []string
    var cursor uint64
    for {
        var batch []string
        var err error
        cursor, batch, err = redis.Scan(ctx, cursor, pattern, 200).Result()
        if err != nil { return nil, err }
        keys = append(keys, batch...)
        if cursor == 0 { break }
    }
    return keys, nil
}
```

**countTokens**: 统计 token 总数、1小时新增数、每人最大 token 数
```go
func countTokens(ctx context.Context, redis *redis.Client, sessionKeys []string, tokenPrefix string) (total, newTotal, maxPerUser int) {
    oneHourAgo := time.Now().Add(-1 * time.Hour)
    for _, key := range keys {
        tokens, _ := redis.SMembers(ctx, key).Result()
        userCount := 0
        for _, t := range tokens {
            dataStr, err := redis.Get(ctx, tokenPrefix+t).Result()
            if err != nil { continue }
            total++
            userCount++
            // 解析 created_at 判断是否在一小时内
            var data map[string]interface{}
            json.Unmarshal([]byte(dataStr), &data)
            if created, ok := data["created_at"].(string); ok {
                createdTime, err := time.Parse(time.RFC3339, created)
                if err == nil && createdTime.After(oneHourAgo) { newTotal++ }
            }
        }
        if userCount > maxPerUser { maxPerUser = userCount }
    }
    return
}
```

**Page**: 收集 B 端在线会话 + 关键词过滤 + 分页
```go
func Page(c *gin.Context, param *SessionPageParam) gin.H {
    ctx := context.Background()
    redisClient := db.Redis
    
    sessions, _ := collectSessions(ctx, redisClient, 
        constants.SESSION_PREFIX_BUSINESS, constants.TOKEN_PREFIX_BUSINESS, param.Keyword)
    
    total := len(sessions)
    current := max(1, param.Current)
    size := max(1, param.Size)
    start := (current - 1) * size
    end := start + size
    if end > total { end = total }
    
    pageRecords := make([]*SessionPageResult, 0)
    if start < total {
        pageRecords = sessions[start:end]
    }
    
    // session 分页直接返回 map（手动构建分页响应）
    return gin.H{
        "code": 200, "message": "请求成功", "success": true,
        "data": gin.H{
            "records": pageRecords,
            "total":   total,
            "page":    current,
            "size":    size,
            "pages":   (total + size - 1) / size,
        },
    }
}
```

**collectSessions**: 扫描所有 session key → 获取 tokens → 获取 token data → 查 SysUser 表获取用户信息
```go
func collectSessions(ctx context.Context, redis *redis.Client, sessionPrefix, tokenPrefix, keyword string) ([]*SessionPageResult, error) {
    sessionKeys, _ := scanKeys(ctx, redis, sessionPrefix+"*")
    var results []*SessionPageResult
    userCache := make(map[string]*gen.SysUser)
    
    for _, sk := range sessionKeys {
        userID := sk[strings.LastIndex(sk, ":")+1:]
        tokens, _ := redis.SMembers(ctx, sk).Result()
        
        // 找到第一个有效 token
        var firstToken string
        tokenCount := 0
        for _, t := range tokens {
            exists, _ := redis.Exists(ctx, tokenPrefix+t).Result()
            if exists > 0 {
                if firstToken == "" { firstToken = t }
                tokenCount++
            }
        }
        if firstToken == "" { continue }
        
        // 获取 token data
        dataStr, _ := redis.Get(ctx, tokenPrefix+firstToken).Result()
        if dataStr == "" { continue }
        var tokenData map[string]interface{}
        json.Unmarshal([]byte(dataStr), &tokenData)
        
        extra, _ := tokenData["extra"].(map[string]interface{})
        username, _ := extra["username"].(string)
        
        // keyword 过滤
        if keyword != "" && !strings.Contains(strings.ToLower(username), strings.ToLower(keyword)) {
            continue
        }
        
        ttl, _ := redis.TTL(ctx, tokenPrefix+firstToken).Result()
        createdStr, _ := tokenData["created_at"].(string)
        
        // 查用户信息
        user, ok := userCache[userID]
        if !ok {
            user, _ = db.Client.SysUser.Get(ctx, userID)
            userCache[userID] = user
        }
        
        r := &SessionPageResult{
            UserID:                userID,
            Username:              &username,
            TokenCount:            tokenCount,
            SessionCreateTime:     createdStr,
            SessionTimeoutSeconds: max(0, int(ttl.Seconds())),
            SessionTimeout:        formatTimeout(int(ttl.Seconds())),
        }
        if user != nil {
            r.Nickname = user.Nickname
            r.Avatar = user.Avatar
            r.Status = user.Status
            r.LastLoginIP = user.LastLoginIP
            if user.LastLoginAt != nil {
                r.LastLoginTime = user.LastLoginAt.Format(time.RFC3339)
            }
            if user.LastLoginIP != nil {
                r.LastLoginAddress = utils.GetCityInfo(*user.LastLoginIP)
            }
        }
        results = append(results, r)
    }
    
    // 按 session_create_time DESC 排序
    sort.Slice(results, func(i, j int) bool {
        return results[i].SessionCreateTime > results[j].SessionCreateTime
    })
    return results, nil
}
```

**Exit**: `auth.Kickout(param.UserID)` — 删除所有 token + session

**Tokens**: 获取指定用户的 BUSINESS token 详情
```go
func TokenList(c *gin.Context, userID string) []*SessionTokenResult {
    ctx := context.Background()
    redisClient := db.Redis
    
    sessionKey := constants.SESSION_PREFIX_BUSINESS + userID
    tokens, _ := redisClient.SMembers(ctx, sessionKey).Result()
    
    results := make([]*SessionTokenResult, 0)
    for _, t := range tokens {
        dataStr, _ := redisClient.Get(ctx, constants.TOKEN_PREFIX_BUSINESS+t).Result()
        if dataStr == "" { continue }
        
        var tokenData map[string]interface{}
        json.Unmarshal([]byte(dataStr), &tokenData)
        ttl, _ := redisClient.TTL(ctx, constants.TOKEN_PREFIX_BUSINESS+t).Result()
        
        extra, _ := tokenData["extra"].(map[string]interface{})
        deviceType, _ := extra["device_type"].(string)
        deviceID, _ := extra["device_id"].(string)
        
        results = append(results, &SessionTokenResult{
            Token:          t,
            CreatedAt:      tokenData["created_at"].(string),
            Timeout:        formatTimeout(int(ttl.Seconds())),
            TimeoutSeconds: max(0, int(ttl.Seconds())),
            DeviceType:     deviceType,
            DeviceID:       deviceID,
        })
    }
    return results
}
```

**ExitToken**: `auth.KickoutToken(param.UserID, param.Token)`

**ChartData**: 近7天 B端/C端新增 session 趋势 + 总比例
```go
func ChartData(c *gin.Context) *SessionChartData {
    ctx := context.Background()
    redisClient := db.Redis
    
    bKeys, _ := scanKeys(ctx, redisClient, constants.SESSION_PREFIX_BUSINESS+"*")
    cKeys, _ := scanKeys(ctx, redisClient, constants.SESSION_PREFIX_CONSUMER+"*")
    
    // pie: B vs C total
    bTotal, _, _ := countTokens(ctx, redisClient, bKeys, constants.TOKEN_PREFIX_BUSINESS)
    cTotal, _, _ := countTokens(ctx, redisClient, cKeys, constants.TOKEN_PREFIX_CONSUMER)
    
    // bar: 近7天每日新增
    today := time.Now().Truncate(24 * time.Hour)
    days := make([]string, 7)
    for i := 6; i >= 0; i-- {
        days[6-i] = today.AddDate(0, 0, -i).Format("2006-01-02")
    }
    bDaily := make([]int, 7)
    cDaily := make([]int, 7)
    countDaily(bKeys, constants.TOKEN_PREFIX_BUSINESS, bDaily, today)
    countDaily(cKeys, constants.TOKEN_PREFIX_CONSUMER, cDaily, today)
    
    return &SessionChartData{
        BarChart: BarChartData{
            Days: days,
            Series: []CategorySeries{
                {Name: "B端", Data: bDaily},
                {Name: "C端", Data: cDaily},
            },
        },
        PieChart: PieChartData{
            Data: []CategoryTotal{
                {Category: "B端", Total: bTotal},
                {Category: "C端", Total: cTotal},
            },
        },
    }
}
```

**formatTimeout**: 将秒数格式化为可读字符串
```go
func formatTimeout(seconds int) string {
    if seconds < 0 { return "已过期" }
    if seconds == 0 { return "永久" }
    if seconds < 60 { return fmt.Sprintf("剩余 %d秒", seconds) }
    if seconds < 3600 { return fmt.Sprintf("剩余 %d分钟", seconds/60) }
    if seconds < 86400 { return fmt.Sprintf("剩余 %d小时%d分钟", seconds/3600, (seconds%3600)/60) }
    return fmt.Sprintf("剩余 %d天%d小时", seconds/86400, (seconds%86400)/3600)
}
```

### API 处理器

**注意**: session 的 page 端点返回数据不使用 `result.PageDataResult`，而是手动构建与 FastAPI 一致的分页结构：

```go
func pageHandler(c *gin.Context) {
    var param session.SessionPageParam
    if err := c.ShouldBindQuery(&param); err != nil {
        c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
        return
    }
    data := session.Page(c, &param)
    c.JSON(200, data)
}
```

`session.Page()` 返回的 gin.H 已包含完整的响应结构。

---

## 7. analyze 模块 (`modules/sys/analyze/`)

**无独立 DB 表**，聚合查询多个系统表。

### Endpoints（1 个）

| 方法 | 路径 | 权限 | 处理器 |
|------|------|------|--------|
| GET | `/api/v1/sys/analyze/dashboard` | **无权限要求** | dashboard |

**关键发现**: FastAPI 版 analyze 端点**没有** `@HeiCheckPermission` 装饰器，只有 `Depends(get_db)`。Gin 版也不加任何权限中间件。

### params.go

```go
package analyze

type TrendItem struct {
    Month string `json:"month"`
    Count int    `json:"count"`
}

type OrgUserDistribution struct {
    Name  string `json:"name"`
    Count int    `json:"count"`
}

type CategoryDistribution struct {
    Category string `json:"category"`
    Count    int    `json:"count"`
}

type DashboardStats struct {
    TotalUsers   int `json:"total_users"`
    ActiveUsers  int `json:"active_users"`
    TotalRoles   int `json:"total_roles"`
    TotalOrgs    int `json:"total_orgs"`
    TotalConfigs int `json:"total_configs"`
    TotalNotices int `json:"total_notices"`
}

type SysInfo struct {
    OsName   string `json:"os_name"`
    ServerIP string `json:"server_ip"`
    RunTime  string `json:"run_time"`
}

type ClientStats struct {
    TotalUsers  int `json:"total_users"`
    ActiveUsers int `json:"active_users"`
}

type DashboardVO struct {
    Stats                   DashboardStats         `json:"stats"`
    ClientStats             ClientStats            `json:"client_stats"`
    UserTrend               []TrendItem            `json:"user_trend"`
    ClientTrend             []TrendItem            `json:"client_trend"`
    OrgUserDistribution     []OrgUserDistribution  `json:"org_user_distribution"`
    RoleCategoryDistribution []CategoryDistribution `json:"role_category_distribution"`
    SysInfo                 SysInfo                `json:"sys_info"`
}
```

### service.go 关键实现

```go
package analyze

import (...)

var ServerStartTime = time.Now()

func Dashboard(c *gin.Context) *DashboardVO {
    ctx := context.Background()
    
    // 基础统计
    stats := DashboardStats{
        TotalUsers:   countQuery(ctx, db.Client.SysUser.Query()),
        ActiveUsers:  countQueryWithCondition(ctx, db.Client.SysUser.Query(), sysuser.Status("ACTIVE")),
        TotalRoles:   countQuery(ctx, db.Client.SysRole.Query()),
        TotalOrgs:    countQuery(ctx, db.Client.SysOrg.Query()),
        TotalConfigs: countQuery(ctx, db.Client.SysConfig.Query()),
        TotalNotices: countQuery(ctx, db.Client.SysNotice.Query()),
    }
    
    // 客户端统计
    clientStats := ClientStats{
        TotalUsers:  countQuery(ctx, db.Client.ClientUser.Query()),
        ActiveUsers: countQueryWithCondition(ctx, db.Client.ClientUser.Query(), clientuser.Status("ACTIVE")),
    }
    
    // 用户趋势
    userTrend := getUserTrend(ctx)
    clientTrend := getClientUserTrend(ctx)
    
    // 组织用户分布
    orgDistribution := getOrgUserDistribution(ctx)
    
    // 角色类别分布
    roleDistribution := getRoleCategoryDistribution(ctx)
    
    // 系统信息
    sysInfo := getSysInfo()
    
    return &DashboardVO{
        Stats:                    stats,
        ClientStats:              clientStats,
        UserTrend:                userTrend,
        ClientTrend:              clientTrend,
        OrgUserDistribution:      orgDistribution,
        RoleCategoryDistribution: roleDistribution,
        SysInfo:                  sysInfo,
    }
}

// 用户趋势 — 近12个月每月新增用户数
func getUserTrend(ctx context.Context) []TrendItem {
    users, _ := db.Client.SysUser.Query().All(ctx)
    monthMap := make(map[string]int)
    for _, u := range users {
        if u.CreatedAt != nil {
            month := u.CreatedAt.Format("2006-01")
            monthMap[month]++
        }
    }
    result := make([]TrendItem, 0)
    for i := 11; i >= 0; i-- {
        month := time.Now().AddDate(0, -i, 0).Format("2006-01")
        count := monthMap[month]
        result = append(result, TrendItem{Month: month, Count: count})
    }
    return result
}

// 客户端用户趋势
func getClientUserTrend(ctx context.Context) []TrendItem {
    users, _ := db.Client.ClientUser.Query().All(ctx)
    // 同 getUserTrend 逻辑
}

// 组织用户分布 — LEFT JOIN 查询
func getOrgUserDistribution(ctx context.Context) []OrgUserDistribution {
    orgs, _ := db.Client.SysOrg.Query().All(ctx)
    result := make([]OrgUserDistribution, 0)
    for _, o := range orgs {
        count, _ := db.Client.SysUser.Query().Where(sysuser.OrgID(o.ID)).Count(ctx)
        result = append(result, OrgUserDistribution{Name: o.Name, Count: count})
    }
    sort.Slice(result, func(i, j int) bool { return result[i].Count > result[j].Count })
    return result
}

// 角色类别分布
func getRoleCategoryDistribution(ctx context.Context) []CategoryDistribution {
    roles, _ := db.Client.SysRole.Query().All(ctx)
    catMap := make(map[string]int)
    for _, r := range roles { catMap[r.Category]++ }
    result := make([]CategoryDistribution, 0)
    for cat, count := range catMap {
        result = append(result, CategoryDistribution{Category: cat, Count: count})
    }
    sort.Slice(result, func(i, j int) bool { return result[i].Count > result[j].Count })
    return result
}

// 系统信息
func getSysInfo() SysInfo {
    hostname, _ := os.Hostname()
    return SysInfo{
        OsName:   runtime.GOOS,
        ServerIP: getLocalIP(),
        RunTime:  formatDuration(time.Since(ServerStartTime)),
    }
}

func getLocalIP() string {
    addrs, _ := net.InterfaceAddrs()
    for _, addr := range addrs {
        if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() && ipnet.IP.To4() != nil {
            return ipnet.IP.String()
        }
    }
    return "unknown"
}

func formatDuration(d time.Duration) string {
    days := int(d.Hours()) / 24
    hours := int(d.Hours()) % 24
    minutes := int(d.Minutes()) % 60
    if days > 0 {
        return fmt.Sprintf("%d天 %d小时 %d分钟", days, hours, minutes)
    }
    return fmt.Sprintf("%d小时 %d分钟", hours, minutes)
}
```

---

## 路由注册

在 `core/app/router.go` 中添加 7 个模块的注册：

```go
import (
    analyzeApi "hei-gin/modules/sys/analyze/api/v1"
    fileApi "hei-gin/modules/sys/file/api/v1"
    logApi "hei-gin/modules/sys/log/api/v1"
    noticeApi "hei-gin/modules/sys/notice/api/v1"
    orgApi "hei-gin/modules/sys/org/api/v1"
    positionApi "hei-gin/modules/sys/position/api/v1"
    sessionApi "hei-gin/modules/sys/session/api/v1"
)

func SetupRouters(r *gin.Engine) {
    // ... 已有模块 ...
    
    // 新增模块
    analyzeApi.RegisterRoutes(r)
    fileApi.RegisterRoutes(r)
    logApi.RegisterRoutes(r)
    noticeApi.RegisterRoutes(r)
    orgApi.RegisterRoutes(r)
    positionApi.RegisterRoutes(r)
    sessionApi.RegisterRoutes(r)
}
```

---

## 错误信息对照

| 场景 | 错误消息 | HTTP 状态码 |
|------|----------|-------------|
| 数据不存在(modify) | `"数据不存在"` | 400 |
| 职位存在关联用户 | `"职位存在关联用户，无法删除"` | 400 |
| 组织存在关联用户 | `"组织存在关联用户，无法删除"` | 400 |
| 组织下存在用户组 | `"组织下存在用户组，无法删除"` | 400 |
| 循环父级引用 | `"父级不能选择自身或子节点"` | 400 |
| 文件不存在 | `"文件不存在"` | 404（FastAPI 用 BusinessException，Gin 可统一用 400） |

---

## 分页默认值

```go
if param.Current < 1 { param.Current = 1 }
if param.Size < 1 { param.Size = 10 }
```

---

## 各模块特殊注意事项

### position
- FastAPI 版 page 端点有 `@SysLog("查看职位列表")`，但其他模块的 page 没有 SysLog。保持一致，保留此 SysLog。
- FastAPI 版 service 缺少 `modify` 方法（API 调用了但会运行时错误）。Gin 版按标准 Modify 模式补全。
- page 中 `group_id` 为空时直接返回空数据（与 FastAPI 一致）。

### org
- page 的 `parent_id` 过滤使用 `OR(parent_id = ?, id = ?)` 同时返回父节点和其子节点。
- modify 的循环引用检查需要加载所有组织记录构建 `parent_id` map。
- remove 需要级联收集所有子孙节点，检查 3 种关联（user/group/position）。

### file
- upload 端点的权限检查在 FastAPI 中手动执行（`hasPermissionAnd`），Gin 版不做权限检查或手动检查。
- remove-absolute 需要从存储引擎删除文件 + 删 DB 记录。
- 第一阶段可只实现本地存储引擎（LOCAL）。
- FileVO 中 `is_download_auth` 是 `int` 类型（0/1），ent 生成的是 `*bool`，需要转换。

### log
- create 和 modify 端点在 FastAPI 中定义了但 service 未实现，Gin 版需补全。
- remove 端点无 service 层的 remove 方法，Gin 版需实现（可复用 DeleteByIDs）。
- page 列表需排除大字段（`param_json`, `result_json`, `exe_message`, `sign_data`）。
- chart 数据使用中文标签：vis="登录"/"登出"，op="操作"/"异常"。
- 更新 `core/log/syslog.go` 中的 `saveLog` 函数，将日志写入 `sys_log` 表。

### session
- 所有操作基于 Redis，不需要 DB 表。
- session page 的响应**不**使用 `result.PageDataResult`，需手动构建分页结构。
- `collectSessions` 函数需要从 Redis token data 中的 `extra.username` 获取用户信息（不是直接在 SysUser 表中查询），SysUser 表用于补充 nickname/avatar/status 等。
- `Exit` 使用 `auth.Kickout(userID)`，`ExitToken` 使用 `auth.KickoutToken(userID, token)`。
- `scanKeys` 使用 Redis `SCAN` 命令而非 `KEYS`（避免阻塞）。

### analyze
- 唯一的 dashboard 端点**没有**权限检查。
- 时间格式化用中文格式：`"X天 X小时 X分钟"`（与 FastAPI 一致）。
- 系统信息通过 Go 的 `runtime.GOOS` 获取操作系统（对比 FastAPI 的 `platform.system() + platform.release()`），`SERVER_START_TIME` 在包级变量中记录。

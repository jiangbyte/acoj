# Role, Permission & Authorization Management Design

## Overview

This spec covers the frontend implementation for role management, permission management, and authorization assignment (roles <-> permissions, roles <-> resources, users <-> roles, users <-> groups, groups <-> roles) in the hei-admin-vue project.

**Backend**: hei-fastapi (FastAPI + SQLAlchemy)
**Reference**: snowy-admin-web authorization interaction patterns
**Style**: Consistent with existing hei-admin-vue pages (AppTable, AppTreeTable, AppDrawerForm, etc.)

## Modules

### 1. Permission Management Page (sys/permission/)

A flat-list CRUD page for managing permission codes (e.g., `sys:user:page`).

**Pattern**: Same as `views/sys/role/` (uses AppTable with pagination, AppSearchPanel, import/export modals).

**Columns**: permission code, permission name, module, category (BACKEND/FRONTEND), status, sort, created_at, actions (detail/edit/delete)

**Search**: keyword (name/code), module filter

**Permissions**: `sys:permission:page`, `create`, `modify`, `remove`, `detail`, `import`, `export`

**Files**:
- `src/views/sys/permission/index.vue`
- `src/views/sys/permission/components/form.vue`
- `src/views/sys/permission/components/detail.vue`
- `src/api/permission.ts`

### 2. Role Grant Permission (sys/role/components/grantPermission.vue)

A drawer that shows all API permissions grouped by module prefix, allowing role-permission assignment with data scope control.

**UI**: Table with module prefix grouping (rowSpan), checkbox per permission, data scope radio group (全部/仅自己/所属组织/所属组织及以下)

**API calls**:
- `GET /api/v1/sys/permission/by-module?module=xxx` — list permissions by module
- `GET /api/v1/sys/role/own-permission?role_id=xxx` — get role's current permission IDs
- `POST /api/v1/sys/role/grant-permission` — save permission assignments

**Data flow**:
1. Open drawer → load all available permissions + role's own permissions
2. Echo-check already assigned permissions
3. User toggles checkboxes and selects data scope
4. Save → POST grant-permission with role_id + permission_ids + scope info

### 3. Role Grant Resource (sys/role/components/grantResource.vue)

A drawer showing the menu resource tree organized by module, allowing role-menu/button assignment.

**UI**: Module radio buttons at top, then a table with parent menu column (rowSpan), menu name column, button permission column

**API calls**:
- `GET /api/v1/sys/resource/tree` — get all resource tree
- `GET /api/v1/sys/role/own-resource?role_id=xxx` — get role's current resource IDs
- `POST /api/v1/sys/role/grant-resource` — save resource assignments

### 4. User Grant Role (sys/user/components/grantRole.vue)

A drawer using a-transfer (shuttle/transfer box) for assigning roles to a user.

**API calls**:
- `GET /api/v1/sys/role/page` — list all roles
- `GET /api/v1/sys/user/own-roles?user_id=xxx` — get user's current role IDs
- `POST /api/v1/sys/user/grant-role` — save role assignments

### 5. User Grant Group (sys/user/components/grantGroup.vue)

A drawer using tree-based transfer for assigning groups to a user.

**API calls**:
- `GET /api/v1/sys/group/tree` — list all groups
- `GET /api/v1/sys/user/own-groups?user_id=xxx` — get user's current group IDs
- `POST /api/v1/sys/user/grant-group` — save group assignments

### 6. Group Grant Role (sys/group/components/grantRole.vue)

Same pattern as User Grant Role (transfer box) for assigning roles to a group.

**API calls**:
- `GET /api/v1/sys/role/page` — list all roles
- `GET /api/v1/sys/group/own-roles?group_id=xxx` — get group's current role IDs
- `POST /api/v1/sys/group/grant-role` — save role assignments

## Page Modifications

### Role index page (`src/views/sys/role/index.vue`)
Add 「授权」dropdown in action column with items:
- 授权权限 → opens grantPermission.vue
- 授权资源 → opens grantResource.vue

### User index page (`src/views/sys/user/index.vue`)
Add 「授权」dropdown in action column with items:
- 分配角色 → opens grantRole.vue
- 分配用户组 → opens grantGroup.vue

### Group index page (`src/views/sys/group/index.vue`)
Add 「授权」dropdown in action column with items:
- 分配角色 → opens grantRole.vue

## API File Updates

### `src/api/user.ts`
Add:
- `fetchUserGrantGroup(data)` → POST `/api/v1/sys/user/grant-group`
- `fetchUserOwnGroups(params)` → GET `/api/v1/sys/user/own-groups`

### `src/api/group.ts`
Add:
- `fetchGroupGrantRole(data)` → POST `/api/v1/sys/group/grant-role`
- `fetchGroupOwnRoles(params)` → GET `/api/v1/sys/group/own-roles`

## Implementation Order

1. Permission management page (CRUD, independent module)
2. API file updates (user.ts, group.ts)
3. Role grant components (grantPermission, grantResource) + role page update
4. User grant components (grantRole, grantGroup) + user page update
5. Group grant component (grantRole) + group page update

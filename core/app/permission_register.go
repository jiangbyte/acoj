package app

import (
	"hei-gin/core/auth"
)

func init() {
	// ===== sys:banner =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:banner:page", Module: "sys:banner", Name: "横幅分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:banner:create", Module: "sys:banner", Name: "添加横幅"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:banner:modify", Module: "sys:banner", Name: "编辑横幅"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:banner:remove", Module: "sys:banner", Name: "删除横幅"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:banner:detail", Module: "sys:banner", Name: "横幅详情"})

	// ===== sys:config =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:config:page", Module: "sys:config", Name: "配置分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:config:list", Module: "sys:config", Name: "配置列表"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:config:create", Module: "sys:config", Name: "添加配置"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:config:modify", Module: "sys:config", Name: "编辑配置"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:config:remove", Module: "sys:config", Name: "删除配置"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:config:detail", Module: "sys:config", Name: "配置详情"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:config:edit", Module: "sys:config", Name: "配置编辑"})

	// ===== sys:dict =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:dict:page", Module: "sys:dict", Name: "字典分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:dict:create", Module: "sys:dict", Name: "添加字典"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:dict:modify", Module: "sys:dict", Name: "编辑字典"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:dict:remove", Module: "sys:dict", Name: "删除字典"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:dict:detail", Module: "sys:dict", Name: "字典详情"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:dict:list", Module: "sys:dict", Name: "字典列表"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:dict:tree", Module: "sys:dict", Name: "字典树"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:dict:get-label", Module: "sys:dict", Name: "字典标签"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:dict:get-children", Module: "sys:dict", Name: "字典子项"})

	// ===== sys:file =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:file:upload", Module: "sys:file", Name: "上传文件"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:file:download", Module: "sys:file", Name: "下载文件"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:file:page", Module: "sys:file", Name: "文件分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:file:detail", Module: "sys:file", Name: "文件详情"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:file:remove", Module: "sys:file", Name: "删除文件"})

	// ===== sys:group =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:group:page", Module: "sys:group", Name: "分组分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:group:tree", Module: "sys:group", Name: "分组树"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:group:create", Module: "sys:group", Name: "添加分组"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:group:modify", Module: "sys:group", Name: "编辑分组"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:group:remove", Module: "sys:group", Name: "删除分组"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:group:detail", Module: "sys:group", Name: "分组详情"})

	// ===== sys:log =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:log:page", Module: "sys:log", Name: "日志分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:log:create", Module: "sys:log", Name: "添加日志"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:log:modify", Module: "sys:log", Name: "编辑日志"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:log:remove", Module: "sys:log", Name: "删除日志"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:log:detail", Module: "sys:log", Name: "日志详情"})

	// ===== sys:notice =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:notice:page", Module: "sys:notice", Name: "通知分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:notice:create", Module: "sys:notice", Name: "添加通知"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:notice:modify", Module: "sys:notice", Name: "编辑通知"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:notice:remove", Module: "sys:notice", Name: "删除通知"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:notice:detail", Module: "sys:notice", Name: "通知详情"})

	// ===== sys:org =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:org:page", Module: "sys:org", Name: "组织分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:org:tree", Module: "sys:org", Name: "组织树"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:org:create", Module: "sys:org", Name: "添加组织"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:org:modify", Module: "sys:org", Name: "编辑组织"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:org:remove", Module: "sys:org", Name: "删除组织"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:org:detail", Module: "sys:org", Name: "组织详情"})

	// ===== sys:permission =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:permission:modules", Module: "sys:permission", Name: "权限模块列表"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:permission:by-module", Module: "sys:permission", Name: "按模块查询权限"})

	// ===== sys:position =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:position:page", Module: "sys:position", Name: "岗位分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:position:create", Module: "sys:position", Name: "添加岗位"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:position:modify", Module: "sys:position", Name: "编辑岗位"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:position:remove", Module: "sys:position", Name: "删除岗位"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:position:detail", Module: "sys:position", Name: "岗位详情"})

	// ===== sys:module / sys:resource =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:module:page", Module: "sys:module", Name: "模块分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:module:create", Module: "sys:module", Name: "添加模块"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:module:detail", Module: "sys:module", Name: "模块详情"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:module:modify", Module: "sys:module", Name: "编辑模块"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:module:remove", Module: "sys:module", Name: "删除模块"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:resource:page", Module: "sys:resource", Name: "资源分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:resource:tree", Module: "sys:resource", Name: "资源树"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:resource:create", Module: "sys:resource", Name: "添加资源"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:resource:detail", Module: "sys:resource", Name: "资源详情"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:resource:modify", Module: "sys:resource", Name: "编辑资源"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:resource:remove", Module: "sys:resource", Name: "删除资源"})

	// ===== sys:role =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:role:page", Module: "sys:role", Name: "角色分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:role:create", Module: "sys:role", Name: "添加角色"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:role:modify", Module: "sys:role", Name: "编辑角色"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:role:remove", Module: "sys:role", Name: "删除角色"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:role:detail", Module: "sys:role", Name: "角色详情"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:role:grant-permission", Module: "sys:role", Name: "分配角色权限"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:role:grant-resource", Module: "sys:role", Name: "分配角色资源"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:role:own-permission", Module: "sys:role", Name: "角色权限列表"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:role:own-resource", Module: "sys:role", Name: "角色资源列表"})

	// ===== sys:session =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:session:page", Module: "sys:session", Name: "会话分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:session:exit", Module: "sys:session", Name: "强退会话"})

	// ===== sys:user =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:user:page", Module: "sys:user", Name: "用户分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:user:create", Module: "sys:user", Name: "添加用户"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:user:modify", Module: "sys:user", Name: "编辑用户"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:user:remove", Module: "sys:user", Name: "删除用户"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:user:detail", Module: "sys:user", Name: "用户详情"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:user:grant-role", Module: "sys:user", Name: "分配用户角色"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:user:grant-permission", Module: "sys:user", Name: "分配用户权限"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:user:own-permission-detail", Module: "sys:user", Name: "用户权限详情"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "sys:user:own-roles", Module: "sys:user", Name: "用户角色列表"})

	// ===== client:user =====
	auth.RegisterPermission(auth.PermissionEntry{Code: "client:user:page", Module: "client:user", Name: "C端用户分页"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "client:user:create", Module: "client:user", Name: "添加C端用户"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "client:user:modify", Module: "client:user", Name: "编辑C端用户"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "client:user:remove", Module: "client:user", Name: "删除C端用户"})
	auth.RegisterPermission(auth.PermissionEntry{Code: "client:user:detail", Module: "client:user", Name: "C端用户详情"})
}

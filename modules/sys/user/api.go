package user

import (
	"context"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysresource"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/user/page",
		auth.CheckLogin(),
		PageHandler,
	)
	r.POST("/api/v1/sys/user/create",
		log.SysLog("添加用户"),
		auth.CheckPermission("sys:user:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/user/modify",
		log.SysLog("编辑用户"),
		auth.CheckPermission("sys:user:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/user/remove",
		log.SysLog("删除用户"),
		auth.CheckPermission("sys:user:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/user/detail",
		auth.CheckPermission("sys:user:detail"),
		DetailHandler,
	)
	r.POST("/api/v1/sys/user/grant-role",
		log.SysLog("分配用户角色"),
		auth.CheckPermission("sys:user:grant-role"),
		GrantRoleHandler,
	)
	r.POST("/api/v1/sys/user/grant-permission",
		log.SysLog("分配用户权限"),
		auth.CheckPermission("sys:user:grant-permission"),
		GrantPermissionHandler,
	)
	r.GET("/api/v1/sys/user/own-permission-detail",
		auth.CheckPermission("sys:user:own-permission-detail"),
		OwnPermissionDetailHandler,
	)
	r.GET("/api/v1/sys/user/own-roles",
		auth.CheckPermission("sys:user:own-roles"),
		OwnRolesHandler,
	)
	r.GET("/api/v1/sys/user/current",
		auth.CheckLogin(),
		CurrentHandler,
	)
	r.GET("/api/v1/sys/user/menus",
		auth.CheckLogin(),
		MenusHandler,
	)
	r.GET("/api/v1/sys/user/permissions",
		auth.CheckLogin(),
		PermissionsHandler,
	)
	r.POST("/api/v1/sys/user/update-profile",
		log.SysLog("更新个人信息"),
		auth.CheckLogin(),
		norepeat.NoRepeat(3000),
		UpdateProfileHandler,
	)
	r.POST("/api/v1/sys/user/update-avatar",
		log.SysLog("更新头像"),
		auth.CheckLogin(),
		UpdateAvatarHandler,
	)
	r.POST("/api/v1/sys/user/update-password",
		log.SysLog("修改密码"),
		auth.CheckLogin(),
		norepeat.NoRepeat(3000),
		UpdatePasswordHandler,
	)
}

func PageHandler(c *gin.Context) {
	var p PageParam
	if err := c.ShouldBindQuery(&p); err != nil {
		result.ValidationError(c, err)
		return
	}
	if p.Page <= 0 {
		p.Page = 1
	}
	if p.Size <= 0 {
		p.Size = 10
	}

	total, vos, err := Page(p.Page, p.Size, p.Keyword, p.Status, p.OrgID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req UserCreateReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	loginID := auth.AuthTool.GetLoginID(c)
	item, err := Create(&req, loginID)
	if err != nil {
		result.Failure(c, "创建失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func ModifyHandler(c *gin.Context) {
	var req UserModifyReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	loginID := auth.AuthTool.GetLoginID(c)
	item, err := Modify(&req, loginID)
	if err != nil {
		result.Failure(c, "修改失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func RemoveHandler(c *gin.Context) {
	var req RemoveReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	if err := Remove(req.IDs); err != nil {
		result.Failure(c, "删除失败", 500)
		return
	}
	result.Success(c, nil)
}

func DetailHandler(c *gin.Context) {
	var req DetailReq
	if err := c.ShouldBindQuery(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	item, err := Detail(req.ID)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, toVO(item))
}

func GrantRoleHandler(c *gin.Context) {
	var req GrantRoleReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	if err := GrantRole(req.UserID, req.RoleIDs); err != nil {
		result.Failure(c, "分配角色失败", 500)
		return
	}
	result.Success(c, nil)
}

func GrantPermissionHandler(c *gin.Context) {
	var req GrantPermissionReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	if err := GrantPermission(req.UserID, req.Permissions); err != nil {
		result.Failure(c, "分配权限失败", 500)
		return
	}
	result.Success(c, nil)
}

func OwnPermissionDetailHandler(c *gin.Context) {
	userID := c.Query("user_id")
	if userID == "" {
		result.Failure(c, "缺少user_id参数", 400)
		return
	}
	perms, err := OwnPermissionDetail(userID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, perms)
}

func OwnRolesHandler(c *gin.Context) {
	userID := c.Query("user_id")
	if userID == "" {
		userID = auth.AuthTool.GetLoginID(c)
	}
	roleIDs, err := OwnRoles(userID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, roleIDs)
}

func CurrentHandler(c *gin.Context) {
	userID := auth.AuthTool.GetLoginID(c)
	vo, err := Current(userID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, vo)
}

// MenuVO represents a menu tree node.
type MenuVO struct {
	ID        string    `json:"id"`
	Name      string    `json:"name"`
	Icon      string    `json:"icon"`
	Path      string    `json:"path"`
	Component string    `json:"component"`
	Type      string    `json:"type"`
	Children  []*MenuVO `json:"children"`
}

func MenusHandler(c *gin.Context) {
	userID := auth.AuthTool.GetLoginID(c)
	ctx := context.Background()

	isSuper := IsSuperAdmin(userID)

	var allResources []*ent.SysResource
	if isSuper {
		var err error
		allResources, err = db.Client.SysResource.Query().
			Where(sysresource.TypeNotIn("BUTTON")).
			Order(ent.Asc(sysresource.FieldSortCode)).
			All(ctx)
		if err != nil {
			result.Failure(c, "查询失败", 500)
			return
		}
	} else {
		resourceIDs, err := GetUserResourceIDs(userID)
		if err != nil || len(resourceIDs) == 0 {
			result.Success(c, []*MenuVO{})
			return
		}
		allResources, err = db.Client.SysResource.Query().
			Where(
				sysresource.IDIn(resourceIDs...),
				sysresource.TypeNotIn("BUTTON"),
			).
			Order(ent.Asc(sysresource.FieldSortCode)).
			All(ctx)
		if err != nil {
			result.Failure(c, "查询失败", 500)
			return
		}
	}

	tree := buildMenuTree(allResources)
	result.Success(c, tree)
}

func buildMenuTree(items []*ent.SysResource) []*MenuVO {
	childrenMap := make(map[string][]*ent.SysResource)
	var roots []*ent.SysResource
	for _, item := range items {
		if item.ParentID == "" {
			roots = append(roots, item)
		} else {
			childrenMap[item.ParentID] = append(childrenMap[item.ParentID], item)
		}
	}
	var tree []*MenuVO
	for _, root := range roots {
		tree = append(tree, buildMenuNode(root, childrenMap))
	}
	return tree
}

func buildMenuNode(item *ent.SysResource, childrenMap map[string][]*ent.SysResource) *MenuVO {
	node := &MenuVO{
		ID:        item.ID,
		Name:      item.Name,
		Icon:      item.Icon,
		Path:      item.Path,
		Component: item.Component,
		Type:      item.Type,
	}
	for _, child := range childrenMap[item.ID] {
		node.Children = append(node.Children, buildMenuNode(child, childrenMap))
	}
	if node.Children == nil {
		node.Children = []*MenuVO{}
	}
	return node
}

func PermissionsHandler(c *gin.Context) {
	loginID := auth.AuthTool.GetLoginID(c)
	loginType := auth.DetectLoginType(c)
	perms := auth.PermissionInterface.GetPermissionList(loginID, loginType)
	result.Success(c, perms)
}

func UpdateProfileHandler(c *gin.Context) {
	var req UpdateProfileReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	userID := auth.AuthTool.GetLoginID(c)
	item, err := UpdateProfile(userID, &req)
	if err != nil {
		result.Failure(c, "更新失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func UpdateAvatarHandler(c *gin.Context) {
	var req UpdateAvatarReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	userID := auth.AuthTool.GetLoginID(c)
	item, err := UpdateAvatar(userID, req.Avatar)
	if err != nil {
		result.Failure(c, "更新失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func UpdatePasswordHandler(c *gin.Context) {
	var req UpdatePasswordReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	userID := auth.AuthTool.GetLoginID(c)
	if err := UpdatePassword(userID, req.OldPassword, req.NewPassword); err != nil {
		result.Failure(c, "密码修改失败", 400)
		return
	}
	result.Success(c, nil)
}

package user

import (
	"context"
	"strconv"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/sysresource"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/user/page",
		auth.CheckPermission("sys:user:page"),
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
	r.GET("/api/v1/sys/user/export",
		log.SysLog("导出用户数据"),
		auth.CheckPermission("sys:user:export"),
		ExportHandler,
	)
	r.GET("/api/v1/sys/user/template",
		auth.CheckPermission("sys:user:template"),
		TemplateHandler,
	)
	r.POST("/api/v1/sys/user/import",
		log.SysLog("导入用户数据"),
		auth.CheckPermission("sys:user:import"),
		norepeat.NoRepeat(5000),
		ImportHandler,
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
		auth.CheckPermission("sys:user:page"),
		CurrentHandler,
	)
	r.GET("/api/v1/sys/user/menus",
		auth.CheckPermission("sys:user:page"),
		MenusHandler,
	)
	r.GET("/api/v1/sys/user/permissions",
		auth.CheckPermission("sys:user:page"),
		PermissionsHandler,
	)
	r.POST("/api/v1/sys/user/update-profile",
		log.SysLog("更新个人信息"),
		auth.CheckPermission("sys:user:page"),
		norepeat.NoRepeat(3000),
		UpdateProfileHandler,
	)
	r.POST("/api/v1/sys/user/update-avatar",
		log.SysLog("更新头像"),
		auth.CheckPermission("sys:user:page"),
		UpdateAvatarHandler,
	)
	r.POST("/api/v1/sys/user/update-password",
		log.SysLog("修改密码"),
		auth.CheckPermission("sys:user:page"),
		norepeat.NoRepeat(3000),
		UpdatePasswordHandler,
	)
}

func PageHandler(c *gin.Context) {
	var p PageParam
	if err := c.ShouldBindQuery(&p); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
		return
	}
	item, err := Detail(req.ID)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, toVO(item))
}

func ExportHandler(c *gin.Context) {
	items, err := QueryAll()
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}
	var data []map[string]interface{}
	for _, item := range items {
		row := map[string]interface{}{
			"username":   item.Username,
			"nickname":   item.Nickname,
			"email":      item.Email,
			"phone":      item.Phone,
			"status":     item.Status,
			"gender":     item.Gender,
			"sort_code":  item.SortCode,
			"created_at": item.CreatedAt.Format("2006-01-02 15:04:05"),
		}
		data = append(data, row)
	}
	headers := utils.BuildHeaders(UserExportFields, UserExportFieldNames)
	excelBytes, err := utils.ExportExcel(data, headers, "用户数据")
	if err != nil {
		result.Failure(c, "导出失败", 500)
		return
	}
	c.Header("Content-Disposition", `attachment; filename="user_export.xlsx"`)
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func TemplateHandler(c *gin.Context) {
	headers := utils.BuildHeaders(UserExportFields, UserExportFieldNames)
	excelBytes, err := utils.ExportExcel(nil, headers, "用户导入模板")
	if err != nil {
		result.Failure(c, "生成模板失败", 500)
		return
	}
	c.Header("Content-Disposition", `attachment; filename="user_template.xlsx"`)
	c.Header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	c.Header("Content-Length", strconv.Itoa(len(excelBytes)))
	c.Data(200, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes)
}

func ImportHandler(c *gin.Context) {
	file, err := c.FormFile("file")
	if err != nil {
		result.Failure(c, "请上传文件", 400)
		return
	}
	src, err := file.Open()
	if err != nil {
		result.Failure(c, "文件读取失败", 500)
		return
	}
	defer src.Close()

	fileBytes := make([]byte, file.Size)
	if _, err := src.Read(fileBytes); err != nil {
		result.Failure(c, "文件读取失败", 500)
		return
	}

	rows, err := utils.ParseExcel(fileBytes, "用户导入模板")
	if err != nil {
		result.Failure(c, "解析Excel失败", 400)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	success := 0
	for _, row := range rows {
		_, err := Create(&UserCreateReq{
			Username: row["用户名"],
			Password: "123456",
			Nickname: row["昵称"],
			Email:    row["电子邮箱"],
			Phone:    row["手机号码"],
		}, loginID)
		if err == nil {
			success++
		}
	}
	result.Success(c, map[string]int{"success": success, "total": len(rows)})
}

func GrantRoleHandler(c *gin.Context) {
	var req GrantRoleReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
		return
	}
	if err := GrantPermission(req.UserID, req.PermissionCodes); err != nil {
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
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
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
		result.Failure(c, "请求参数格式错误", 400)
		return
	}
	userID := auth.AuthTool.GetLoginID(c)
	if err := UpdatePassword(userID, req.OldPassword, req.NewPassword); err != nil {
		result.Failure(c, "密码修改失败", 400)
		return
	}
	result.Success(c, nil)
}

package role

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/role/page",
		auth.CheckPermission("sys:role:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/role/create",
		log.SysLog("添加角色"),
		auth.CheckPermission("sys:role:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/role/modify",
		log.SysLog("编辑角色"),
		auth.CheckPermission("sys:role:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/role/remove",
		log.SysLog("删除角色"),
		auth.CheckPermission("sys:role:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/role/detail",
		auth.CheckPermission("sys:role:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/role/own-resource",
		auth.CheckPermission("sys:role:own-resource"),
		OwnResourcesHandler,
	)
	r.POST("/api/v1/sys/role/grant-resource",
		log.SysLog("分配角色资源"),
		auth.CheckPermission("sys:role:grant-resource"),
		norepeat.NoRepeat(3000),
		GrantResourceHandler,
	)
	r.GET("/api/v1/sys/role/own-permission",
		auth.CheckPermission("sys:role:own-permission"),
		OwnPermissionsHandler,
	)
	r.GET("/api/v1/sys/role/own-permission-detail",
		auth.CheckPermission("sys:role:own-permission"),
		OwnPermissionDetailHandler,
	)
	r.POST("/api/v1/sys/role/grant-permission",
		log.SysLog("分配角色权限"),
		auth.CheckPermission("sys:role:grant-permission"),
		norepeat.NoRepeat(3000),
		GrantPermissionHandler,
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

	total, items, err := Page(p.Page, p.Size, p.Keyword, p.Status)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []RoleVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req RoleCreateReq
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
	var req RoleModifyReq
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

func OwnResourcesHandler(c *gin.Context) {
	roleID := c.Query("role_id")
	if roleID == "" {
		result.Failure(c, "缺少role_id参数", 400)
		return
	}

	ids, err := OwnResources(roleID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, ids)
}

func GrantResourceHandler(c *gin.Context) {
	var req GrantResourceReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	if err := GrantResource(req.RoleID, req.ResourceIDs); err != nil {
		result.Failure(c, "分配资源失败", 500)
		return
	}
	result.Success(c, nil)
}

func OwnPermissionsHandler(c *gin.Context) {
	roleID := c.Query("role_id")
	if roleID == "" {
		result.Failure(c, "缺少role_id参数", 400)
		return
	}

	codes, err := OwnPermissions(roleID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, codes)
}

func GrantPermissionHandler(c *gin.Context) {
	var req GrantPermissionReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	if err := GrantPermission(req.RoleID, req.Permissions); err != nil {
		result.Failure(c, "分配权限失败", 500)
		return
	}
	result.Success(c, nil)
}

func OwnPermissionDetailHandler(c *gin.Context) {
	roleID := c.Query("role_id")
	if roleID == "" {
		result.Failure(c, "缺少role_id参数", 400)
		return
	}
	data, err := OwnPermissionDetail(roleID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}

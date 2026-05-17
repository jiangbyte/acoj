package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/result"
	role "hei-gin/modules/sys/role"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all role routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/role/page
	r.GET("/api/v1/sys/role/page",
		middleware.HeiCheckPermission([]string{"sys:role:page"}),
		rolePage,
	)

	// POST /api/v1/sys/role/create
	r.POST("/api/v1/sys/role/create",
		middleware.HeiCheckPermission([]string{"sys:role:create"}),
		log.SysLog("添加角色"),
		middleware.NoRepeat(3000),
		roleCreate,
	)

	// POST /api/v1/sys/role/modify
	r.POST("/api/v1/sys/role/modify",
		middleware.HeiCheckPermission([]string{"sys:role:modify"}),
		log.SysLog("编辑角色"),
		roleModify,
	)

	// POST /api/v1/sys/role/remove
	r.POST("/api/v1/sys/role/remove",
		middleware.HeiCheckPermission([]string{"sys:role:remove"}),
		log.SysLog("删除角色"),
		roleRemove,
	)

	// GET /api/v1/sys/role/detail
	r.GET("/api/v1/sys/role/detail",
		middleware.HeiCheckPermission([]string{"sys:role:detail"}),
		roleDetail,
	)

	// POST /api/v1/sys/role/grant-permission
	r.POST("/api/v1/sys/role/grant-permission",
		middleware.HeiCheckPermission([]string{"sys:role:grant-permission"}),
		log.SysLog("分配角色权限"),
		middleware.NoRepeat(3000),
		roleGrantPermission,
	)

	// POST /api/v1/sys/role/grant-resource
	r.POST("/api/v1/sys/role/grant-resource",
		middleware.HeiCheckPermission([]string{"sys:role:grant-resource"}),
		log.SysLog("分配角色资源"),
		middleware.NoRepeat(3000),
		roleGrantResource,
	)

	// GET /api/v1/sys/role/own-permission
	r.GET("/api/v1/sys/role/own-permission",
		middleware.HeiCheckPermission([]string{"sys:role:own-permission"}),
		roleOwnPermission,
	)

	// GET /api/v1/sys/role/own-permission-detail
	r.GET("/api/v1/sys/role/own-permission-detail",
		middleware.HeiCheckPermission([]string{"sys:role:own-permission"}),
		roleOwnPermissionDetail,
	)

	// GET /api/v1/sys/role/own-resource
	r.GET("/api/v1/sys/role/own-resource",
		middleware.HeiCheckPermission([]string{"sys:role:own-resource"}),
		roleOwnResource,
	)
}

// rolePage handles GET /api/v1/sys/role/page
func rolePage(c *gin.Context) {
	var param role.RolePageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := role.RolePage(c, &param)
	c.JSON(200, data)
}

// roleCreate handles POST /api/v1/sys/role/create
func roleCreate(c *gin.Context) {
	var vo role.RoleVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	if vo.Code == "" || vo.Name == "" || vo.Category == "" {
		c.JSON(200, result.Failure(c, "角色编码、名称、类别不能为空", 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	role.RoleCreate(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// roleModify handles POST /api/v1/sys/role/modify
func roleModify(c *gin.Context) {
	var vo role.RoleVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	if vo.ID == "" {
		c.JSON(200, result.Failure(c, "ID不能为空", 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	role.RoleModify(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// roleRemove handles POST /api/v1/sys/role/remove
func roleRemove(c *gin.Context) {
	var param struct {
		IDs []string `json:"ids"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	role.RoleRemove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// roleDetail handles GET /api/v1/sys/role/detail
func roleDetail(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(200, result.Success(c, nil))
		return
	}

	vo := role.RoleDetail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

// roleGrantPermission handles POST /api/v1/sys/role/grant-permission
func roleGrantPermission(c *gin.Context) {
	var param role.GrantPermissionParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	role.RoleGrantPermissions(c, param.RoleID, param.Permissions, userID)
	c.JSON(200, result.Success(c, nil))
}

// roleGrantResource handles POST /api/v1/sys/role/grant-resource
func roleGrantResource(c *gin.Context) {
	var param role.GrantResourceParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	role.RoleGrantResources(c, param.RoleID, param.ResourceIDs, param.Permissions)
	c.JSON(200, result.Success(c, nil))
}

// roleOwnPermission handles GET /api/v1/sys/role/own-permission
func roleOwnPermission(c *gin.Context) {
	roleID := c.Query("role_id")
	codes := role.RoleOwnPermissionCodes(c, roleID)
	c.JSON(200, result.Success(c, codes))
}

// roleOwnPermissionDetail handles GET /api/v1/sys/role/own-permission-detail
func roleOwnPermissionDetail(c *gin.Context) {
	roleID := c.Query("role_id")
	details := role.RoleOwnPermissionDetails(c, roleID)
	c.JSON(200, result.Success(c, details))
}

// roleOwnResource handles GET /api/v1/sys/role/own-resource
func roleOwnResource(c *gin.Context) {
	roleID := c.Query("role_id")
	ids := role.RoleOwnResourceIDs(c, roleID)
	c.JSON(200, result.Success(c, ids))
}

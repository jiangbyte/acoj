package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/result"
	org "hei-gin/modules/sys/org"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all org routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/org/page
	r.GET("/api/v1/sys/org/page",
		middleware.HeiCheckPermission([]string{"sys:org:page"}),
		orgPage,
	)

	// GET /api/v1/sys/org/tree
	r.GET("/api/v1/sys/org/tree",
		middleware.HeiCheckPermission([]string{"sys:org:tree"}),
		orgTree,
	)

	// POST /api/v1/sys/org/create
	r.POST("/api/v1/sys/org/create",
		middleware.HeiCheckPermission([]string{"sys:org:create"}),
		log.SysLog("添加组织"),
		middleware.NoRepeat(3000),
		orgCreate,
	)

	// POST /api/v1/sys/org/modify
	r.POST("/api/v1/sys/org/modify",
		middleware.HeiCheckPermission([]string{"sys:org:modify"}),
		log.SysLog("编辑组织"),
		orgModify,
	)

	// POST /api/v1/sys/org/remove
	r.POST("/api/v1/sys/org/remove",
		middleware.HeiCheckPermission([]string{"sys:org:remove"}),
		log.SysLog("删除组织"),
		orgRemove,
	)

	// GET /api/v1/sys/org/detail
	r.GET("/api/v1/sys/org/detail",
		middleware.HeiCheckPermission([]string{"sys:org:detail"}),
		orgDetail,
	)
}

// orgPage handles GET /api/v1/sys/org/page
func orgPage(c *gin.Context) {
	var param org.OrgPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := org.Page(c, &param)
	c.JSON(200, data)
}

// orgTree handles GET /api/v1/sys/org/tree
func orgTree(c *gin.Context) {
	var param org.OrgTreeParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := org.Tree(c, &param)
	c.JSON(200, result.Success(c, data))
}

// orgCreate handles POST /api/v1/sys/org/create
func orgCreate(c *gin.Context) {
	var vo org.OrgVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	if vo.Code == "" || vo.Name == "" || vo.Category == "" {
		c.JSON(200, result.Failure(c, "组织编码、名称、类别不能为空", 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	org.Create(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// orgModify handles POST /api/v1/sys/org/modify
func orgModify(c *gin.Context) {
	var vo org.OrgVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	if vo.ID == "" {
		c.JSON(200, result.Failure(c, "ID不能为空", 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	org.Modify(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// orgRemove handles POST /api/v1/sys/org/remove
func orgRemove(c *gin.Context) {
	var param struct {
		IDs []string `json:"ids"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	org.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// orgDetail handles GET /api/v1/sys/org/detail
func orgDetail(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(200, result.Success(c, nil))
		return
	}

	vo := org.Detail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

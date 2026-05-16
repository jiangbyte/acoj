package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	dict "hei-gin/modules/sys/dict"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all dict routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/dict/page
	r.GET("/api/v1/sys/dict/page",
		middleware.HeiCheckPermission([]string{"sys:dict:page"}),
		dictPage,
	)

	// POST /api/v1/sys/dict/create
	r.POST("/api/v1/sys/dict/create",
		middleware.HeiCheckPermission([]string{"sys:dict:create"}),
		log.SysLog("添加字典"),
		middleware.NoRepeat(3000),
		dictCreate,
	)

	// POST /api/v1/sys/dict/modify
	r.POST("/api/v1/sys/dict/modify",
		middleware.HeiCheckPermission([]string{"sys:dict:modify"}),
		log.SysLog("编辑字典"),
		dictModify,
	)

	// POST /api/v1/sys/dict/remove
	r.POST("/api/v1/sys/dict/remove",
		middleware.HeiCheckPermission([]string{"sys:dict:remove"}),
		log.SysLog("删除字典"),
		dictRemove,
	)

	// GET /api/v1/sys/dict/detail
	r.GET("/api/v1/sys/dict/detail",
		middleware.HeiCheckPermission([]string{"sys:dict:detail"}),
		dictDetail,
	)

	// GET /api/v1/sys/dict/list
	r.GET("/api/v1/sys/dict/list",
		middleware.HeiCheckPermission([]string{"sys:dict:list"}),
		dictList,
	)

	// GET /api/v1/sys/dict/tree
	r.GET("/api/v1/sys/dict/tree",
		middleware.HeiCheckPermission([]string{"sys:dict:tree"}),
		dictTree,
	)

	// GET /api/v1/sys/dict/get-label
	r.GET("/api/v1/sys/dict/get-label",
		middleware.HeiCheckPermission([]string{"sys:dict:get-label"}),
		dictGetLabel,
	)

	// GET /api/v1/sys/dict/get-children
	r.GET("/api/v1/sys/dict/get-children",
		middleware.HeiCheckPermission([]string{"sys:dict:get-children"}),
		dictGetChildren,
	)
}

// dictPage handles GET /api/v1/sys/dict/page
func dictPage(c *gin.Context) {
	var param dict.DictPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := dict.DictPage(c, &param)
	c.JSON(200, data)
}

// dictList handles GET /api/v1/sys/dict/list
func dictList(c *gin.Context) {
	var param dict.DictListParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := dict.DictList(c, &param)
	c.JSON(200, result.Success(c, data))
}

// dictTree handles GET /api/v1/sys/dict/tree
func dictTree(c *gin.Context) {
	var param dict.DictTreeParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := dict.DictTree(c, &param)
	c.JSON(200, result.Success(c, data))
}

// dictCreate handles POST /api/v1/sys/dict/create
func dictCreate(c *gin.Context) {
	var vo dict.DictVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	dict.DictCreate(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// dictModify handles POST /api/v1/sys/dict/modify
func dictModify(c *gin.Context) {
	var vo dict.DictVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	if vo.ID == "" {
		c.JSON(200, result.Failure(c, "ID不能为空", 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	dict.DictModify(c, &vo, userID)
	c.JSON(200, result.Success(c, nil))
}

// dictRemove handles POST /api/v1/sys/dict/remove
func dictRemove(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	if len(param.IDs) == 0 {
		c.JSON(200, result.Failure(c, "ids不能为空", 400, nil))
		return
	}

	dict.DictRemove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// dictDetail handles GET /api/v1/sys/dict/detail
func dictDetail(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(200, result.Failure(c, "id不能为空", 400, nil))
		return
	}

	vo := dict.DictDetail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

// dictGetLabel handles GET /api/v1/sys/dict/get-label
func dictGetLabel(c *gin.Context) {
	typeCode := c.Query("type_code")
	value := c.Query("value")

	data := dict.DictGetLabel(c, typeCode, value)
	c.JSON(200, data)
}

// dictGetChildren handles GET /api/v1/sys/dict/get-children
func dictGetChildren(c *gin.Context) {
	typeCode := c.Query("type_code")

	data := dict.DictGetChildren(c, typeCode)
	c.JSON(200, result.Success(c, data))
}

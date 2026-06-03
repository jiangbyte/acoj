package v1

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/registry"
	authmw "hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	resource "hei-gin/modules/sys/resource"
)

// RegisterRoutes registers all module and resource routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// ---- Module routes ----
	r.GET("/api/v1/sys/module/page",
		registry.Perm("sys:module:page", "模块分页"),
		modulePage,
	)
	r.POST("/api/v1/sys/module/create",
		registry.Perm("sys:module:create", "添加模块"),
		log.SysLog("添加模块"),
		authmw.NoRepeat(3000),
		moduleCreate,
	)
	r.POST("/api/v1/sys/module/modify",
		registry.Perm("sys:module:modify", "编辑模块"),
		log.SysLog("编辑模块"),
		moduleModify,
	)
	r.POST("/api/v1/sys/module/remove",
		registry.Perm("sys:module:remove", "删除模块"),
		log.SysLog("删除模块"),
		moduleRemove,
	)
	r.GET("/api/v1/sys/module/detail",
		registry.Perm("sys:module:detail", "模块详情"),
		moduleDetail,
	)

	// ---- Resource routes ----
	r.GET("/api/v1/sys/resource/tree",
		registry.Perm("sys:resource:tree", "资源树"),
		resourceTree,
	)
	r.GET("/api/v1/sys/resource/page",
		registry.Perm("sys:resource:page", "资源分页"),
		resourcePage,
	)
	r.POST("/api/v1/sys/resource/create",
		registry.Perm("sys:resource:create", "添加资源"),
		log.SysLog("添加资源"),
		authmw.NoRepeat(3000),
		resourceCreate,
	)
	r.POST("/api/v1/sys/resource/modify",
		registry.Perm("sys:resource:modify", "编辑资源"),
		log.SysLog("编辑资源"),
		resourceModify,
	)
	r.POST("/api/v1/sys/resource/remove",
		registry.Perm("sys:resource:remove", "删除资源"),
		log.SysLog("删除资源"),
		resourceRemove,
	)
	r.GET("/api/v1/sys/resource/detail",
		registry.Perm("sys:resource:detail", "资源详情"),
		resourceDetail,
	)
}

// ---------------------------------------------------------------------------
// Module handlers
// ---------------------------------------------------------------------------

func modulePage(c *gin.Context) {
	param := &resource.ModulePageParam{}
	if err := c.ShouldBindQuery(param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}
	c.JSON(http.StatusOK, resource.ModulePage(c, param))
}

func moduleDetail(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}

	data := resource.ModuleDetail(c, id)
	if data == nil {
		c.JSON(http.StatusOK, result.Success(c, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, data))
}

func moduleCreate(c *gin.Context) {
	vo := &resource.ModuleVO{}
	if err := c.ShouldBindJSON(vo); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	resource.ModuleCreate(c, vo, userID)
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func moduleModify(c *gin.Context) {
	vo := &resource.ModuleVO{}
	if err := c.ShouldBindJSON(vo); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}
	if vo.ID == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	resource.ModuleModify(c, vo, userID)
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func moduleRemove(c *gin.Context) {
	param := &pojo.IdsParam{}
	if err := c.ShouldBindJSON(param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}
	if len(param.IDs) == 0 {
		c.JSON(http.StatusOK, result.Failure(c, "ids不能为空", 400, nil))
		return
	}

	resource.ModuleRemove(c, param.IDs)
	c.JSON(http.StatusOK, result.Success(c, nil))
}

// ---------------------------------------------------------------------------
// Resource handlers
// ---------------------------------------------------------------------------

func resourceTree(c *gin.Context) {
	data := resource.ResourceTree(c, "")
	c.JSON(http.StatusOK, result.Success(c, data))
}

func resourcePage(c *gin.Context) {
	param := &resource.ResourcePageParam{}
	if err := c.ShouldBindQuery(param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}
	c.JSON(http.StatusOK, resource.ResourcePage(c, param))
}

func resourceDetail(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}

	data := resource.ResourceDetail(c, id)
	if data == nil {
		c.JSON(http.StatusOK, result.Success(c, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, data))
}

func resourceCreate(c *gin.Context) {
	vo := &resource.ResourceVO{}
	if err := c.ShouldBindJSON(vo); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	resource.ResourceCreate(c, vo, userID)
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func resourceModify(c *gin.Context) {
	vo := &resource.ResourceVO{}
	if err := c.ShouldBindJSON(vo); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}
	if vo.ID == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	resource.ResourceModify(c, vo, userID)
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func resourceRemove(c *gin.Context) {
	param := &pojo.IdsParam{}
	if err := c.ShouldBindJSON(param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}
	if len(param.IDs) == 0 {
		c.JSON(http.StatusOK, result.Failure(c, "ids不能为空", 400, nil))
		return
	}

	resource.ResourceRemove(c, param.IDs)
	c.JSON(http.StatusOK, result.Success(c, nil))
}


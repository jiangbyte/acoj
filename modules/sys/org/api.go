package org

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/org/page",
		auth.CheckPermission("sys:org:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/org/create",
		log.SysLog("添加组织"),
		auth.CheckPermission("sys:org:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/org/modify",
		log.SysLog("编辑组织"),
		auth.CheckPermission("sys:org:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/org/remove",
		log.SysLog("删除组织"),
		auth.CheckPermission("sys:org:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/org/detail",
		auth.CheckPermission("sys:org:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/org/treeselect",
		auth.CheckPermission("sys:org:page"),
		TreeSelectHandler,
	)
	r.GET("/api/v1/sys/org/own-roles",
		auth.CheckPermission("sys:org:grant"),
		OwnRolesHandler,
	)
	r.POST("/api/v1/sys/org/grant-role",
		log.SysLog("分配角色"),
		auth.CheckPermission("sys:org:grant"),
		GrantRoleHandler,
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

	var vos []OrgVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req OrgCreateReq
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
	var req OrgModifyReq
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

func TreeSelectHandler(c *gin.Context) {
	tree, err := TreeSelect()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, tree)
}

func OwnRolesHandler(c *gin.Context) {
	orgID := c.Query("org_id")
	if orgID == "" {
		result.Failure(c, "缺少org_id参数", 400)
		return
	}

	roleIDs, err := OwnRoles(orgID)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, roleIDs)
}

func GrantRoleHandler(c *gin.Context) {
	var req OrgGrantRoleReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	if err := GrantRole(req.OrgID, req.RoleIDs); err != nil {
		result.Failure(c, "分配角色失败", 500)
		return
	}
	result.Success(c, nil)
}

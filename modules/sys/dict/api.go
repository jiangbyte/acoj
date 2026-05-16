package dict

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	syslog "hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/dict/page",
		auth.CheckPermission("sys:dict:page"),
		PageHandler,
	)
	r.GET("/api/v1/sys/dict/list",
		auth.CheckPermission("sys:dict:list"),
		ListHandler,
	)
	r.GET("/api/v1/sys/dict/tree",
		auth.CheckPermission("sys:dict:tree"),
		TreeHandler,
	)
	r.POST("/api/v1/sys/dict/create",
		syslog.SysLog("添加字典"),
		auth.CheckPermission("sys:dict:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/dict/modify",
		syslog.SysLog("编辑字典"),
		auth.CheckPermission("sys:dict:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/dict/remove",
		syslog.SysLog("删除字典"),
		auth.CheckPermission("sys:dict:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/dict/detail",
		auth.CheckPermission("sys:dict:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/dict/get-label",
		auth.CheckPermission("sys:dict:get-label"),
		GetLabelHandler,
	)
	r.GET("/api/v1/sys/dict/get-children",
		auth.CheckPermission("sys:dict:get-children"),
		GetChildrenHandler,
	)
}

// ========================================================================
//  Page
// ========================================================================

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

	total, items, err := Page(p.Page, p.Size, p.Keyword, p.Category)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []DictVO
	for _, item := range items {
		vos = append(vos, dictToVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

// ========================================================================
//  List
// ========================================================================

func ListHandler(c *gin.Context) {
	var p PageParam
	if err := c.ShouldBindQuery(&p); err != nil {
		result.ValidationError(c, err)
		return
	}

	items, err := List(p.Keyword, p.Category)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []DictVO
	for _, item := range items {
		vos = append(vos, dictToVO(item))
	}
	result.Success(c, vos)
}

// ========================================================================
//  Tree
// ========================================================================

func TreeHandler(c *gin.Context) {
	tree, err := Tree()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, tree)
}

// ========================================================================
//  Create
// ========================================================================

func CreateHandler(c *gin.Context) {
	var req DictCreateReq
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
	result.Success(c, toTreeNode(item))
}

// ========================================================================
//  Modify
// ========================================================================

func ModifyHandler(c *gin.Context) {
	var req DictModifyReq
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
	result.Success(c, toTreeNode(item))
}

// ========================================================================
//  Remove
// ========================================================================

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

// ========================================================================
//  Detail
// ========================================================================

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
	result.Success(c, toTreeNode(item))
}

// ========================================================================
//  GetLabel
// ========================================================================

func GetLabelHandler(c *gin.Context) {
	var req GetLabelReq
	if err := c.ShouldBindQuery(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	label, err := GetLabel(req.TypeCode, req.Value)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, gin.H{"label": label})
}

// ========================================================================
//  GetChildren
// ========================================================================

func GetChildrenHandler(c *gin.Context) {
	var req GetChildrenReq
	if err := c.ShouldBindQuery(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	node, err := GetChildren(req.TypeCode)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, node)
}

// ========================================================================
//  Export
// ========================================================================

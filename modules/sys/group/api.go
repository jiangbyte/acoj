package group

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/group/page",
		auth.CheckPermission("sys:group:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/group/create",
		log.SysLog("添加用户组"),
		auth.CheckPermission("sys:group:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/group/modify",
		log.SysLog("编辑用户组"),
		auth.CheckPermission("sys:group:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/group/remove",
		log.SysLog("删除用户组"),
		auth.CheckPermission("sys:group:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/group/detail",
		auth.CheckPermission("sys:group:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/group/treeselect",
		auth.CheckPermission("sys:group:treeselect"),
		TreeSelectHandler,
	)
	r.GET("/api/v1/sys/group/tree",
		auth.CheckPermission("sys:group:tree"),
		TreeHandler,
	)
	r.GET("/api/v1/sys/group/union-tree",
		auth.CheckPermission("sys:group:tree"),
		UnionTreeHandler,
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

	var vos []GroupVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req GroupCreateReq
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
	var req GroupModifyReq
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

func TreeHandler(c *gin.Context) {
	orgID := c.Query("org_id")
	keyword := c.Query("keyword")

	tree, err := Tree(orgID, keyword)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	if tree == nil {
		tree = []*TreeGroup{}
	}
	result.Success(c, tree)
}

func UnionTreeHandler(c *gin.Context) {
	tree, err := UnionTree()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	if tree == nil {
		tree = []*TreeGroup{}
	}
	result.Success(c, tree)
}

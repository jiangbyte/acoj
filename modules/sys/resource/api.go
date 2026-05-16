package resource

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/resource/page",
		auth.CheckPermission("sys:resource:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/resource/create",
		log.SysLog("添加资源"),
		auth.CheckPermission("sys:resource:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/resource/modify",
		log.SysLog("编辑资源"),
		auth.CheckPermission("sys:resource:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/resource/remove",
		log.SysLog("删除资源"),
		auth.CheckPermission("sys:resource:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/resource/detail",
		auth.CheckPermission("sys:resource:detail"),
		DetailHandler,
	)
	r.GET("/api/v1/sys/resource/treeselect",
		auth.CheckPermission("sys:resource:page"),
		TreeSelectHandler,
	)
	r.GET("/api/v1/sys/resource/build-bootstrap-menus",
		auth.CheckPermission("sys:resource:page"),
		BuildBootstrapMenusHandler,
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

	total, items, err := Page(p.Page, p.Size, p.Keyword, p.Type, p.Status)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []ResourceVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req ResourceCreateReq
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
	var req ResourceModifyReq
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

func BuildBootstrapMenusHandler(c *gin.Context) {
	menus, err := BuildBootstrapMenus()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, menus)
}

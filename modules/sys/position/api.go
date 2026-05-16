package position

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/position/page",
		log.SysLog("查看职位列表"),
		auth.CheckPermission("sys:position:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/position/create",
		log.SysLog("添加岗位"),
		auth.CheckPermission("sys:position:create"),
		norepeat.NoRepeat(3000),
		CreateHandler,
	)
	r.POST("/api/v1/sys/position/modify",
		log.SysLog("编辑岗位"),
		auth.CheckPermission("sys:position:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/sys/position/remove",
		log.SysLog("删除岗位"),
		auth.CheckPermission("sys:position:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/position/detail",
		auth.CheckPermission("sys:position:detail"),
		DetailHandler,
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

	var vos []PositionVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req PositionCreateReq
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
	var req PositionModifyReq
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

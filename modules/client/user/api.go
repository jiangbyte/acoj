package user

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/client-user/page",
		auth.CheckPermission("client:user:page"),
		PageHandler,
	)
	r.POST("/api/v1/client-user/create",
		auth.CheckPermission("client:user:create"),
		CreateHandler,
	)
	r.POST("/api/v1/client-user/modify",
		auth.CheckPermission("client:user:modify"),
		ModifyHandler,
	)
	r.POST("/api/v1/client-user/remove",
		auth.CheckPermission("client:user:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/client-user/detail",
		auth.CheckPermission("client:user:detail"),
		DetailHandler,
	)
	// Current user
	r.GET("/api/v1/client-user/current",
		auth.CheckPermission("client:user:page"),
		CurrentHandler,
	)
	// Extra endpoints
	r.POST("/api/v1/client-user/update-profile",
		log.SysLog("C端用户更新个人信息"),
		auth.CheckPermission("client:user:page"),
		norepeat.NoRepeat(3000),
		UpdateProfileHandler,
	)
	r.POST("/api/v1/client-user/update-avatar",
		log.SysLog("C端用户更新头像"),
		auth.CheckPermission("client:user:page"),
		UpdateAvatarHandler,
	)
	r.POST("/api/v1/client-user/update-password",
		log.SysLog("C端用户修改密码"),
		auth.CheckPermission("client:user:page"),
		norepeat.NoRepeat(3000),
		UpdatePasswordHandler,
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

	var vos []ClientUserVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func CreateHandler(c *gin.Context) {
	var req CreateReq
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
	var req ModifyReq
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

func UpdateProfileHandler(c *gin.Context) {
	var req UpdateProfileReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	loginID := auth.ClientAuthTool.GetLoginID(c)
	if loginID == "" {
		result.Failure(c, "未登录", 401)
		return
	}

	item, err := UpdateProfile(loginID, &req)
	if err != nil {
		result.Failure(c, "更新失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func UpdateAvatarHandler(c *gin.Context) {
	var req UpdateAvatarReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	loginID := auth.ClientAuthTool.GetLoginID(c)
	if loginID == "" {
		result.Failure(c, "未登录", 401)
		return
	}

	item, err := UpdateAvatar(loginID, req.Avatar)
	if err != nil {
		result.Failure(c, "更新失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func UpdatePasswordHandler(c *gin.Context) {
	var req UpdatePasswordReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	loginID := auth.ClientAuthTool.GetLoginID(c)
	if loginID == "" {
		result.Failure(c, "未登录", 401)
		return
	}

	if err := UpdatePassword(loginID, &req); err != nil {
		result.Failure(c, "更新失败: "+err.Error(), 500)
		return
	}
	result.Success(c, nil)
}

func CurrentHandler(c *gin.Context) {
	loginID := auth.ClientAuthTool.GetLoginID(c)
	if loginID == "" {
		result.Failure(c, "未登录", 401)
		return
	}

	vo, err := Current(loginID)
	if err != nil {
		result.Failure(c, "未找到用户信息", 404)
		return
	}
	result.Success(c, vo)
}

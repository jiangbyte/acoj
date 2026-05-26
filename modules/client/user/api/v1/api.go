package v1

import (
	middleware "hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	clientuser "hei-gin/modules/client/user"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/client-user/page",
		middleware.HeiCheckPermission([]string{"client:user:page"}),
		pageHandler,
	)

	r.POST("/api/v1/client-user/create",
		middleware.HeiCheckPermission([]string{"client:user:create"}),
		createHandler,
	)

	r.POST("/api/v1/client-user/modify",
		middleware.HeiCheckPermission([]string{"client:user:modify"}),
		modifyHandler,
	)

	r.POST("/api/v1/client-user/remove",
		middleware.HeiCheckPermission([]string{"client:user:remove"}),
		removeHandler,
	)

	r.GET("/api/v1/client-user/detail",
		middleware.HeiCheckPermission([]string{"client:user:detail"}),
		detailHandler,
	)

	r.GET("/api/v1/client-user/current",
		middleware.HeiCheckLogin("CONSUMER"),
		currentHandler,
	)

	r.POST("/api/v1/client-user/update-profile",
		middleware.HeiCheckLogin("CONSUMER"),
		log.SysLog("C端用户更新个人信息"),
		middleware.NoRepeat(3000),
		updateProfileHandler,
	)

	r.POST("/api/v1/client-user/update-avatar",
		middleware.HeiCheckLogin("CONSUMER"),
		log.SysLog("C端用户更新头像"),
		updateAvatarHandler,
	)

	r.POST("/api/v1/client-user/update-password",
		middleware.HeiCheckLogin("CONSUMER"),
		log.SysLog("C端用户修改密码"),
		middleware.NoRepeat(3000),
		updatePasswordHandler,
	)
}

func pageHandler(c *gin.Context) {
	var param clientuser.ClientUserPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := clientuser.ClientUserPage(c, &param)
	c.JSON(200, data)
}

func createHandler(c *gin.Context) {
	var vo clientuser.ClientUserVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	clientuser.ClientUserCreate(c, &vo, "")
	c.JSON(200, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var vo clientuser.ClientUserVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	clientuser.ClientUserModify(c, &vo, "")
	c.JSON(200, result.Success(c, nil))
}

func removeHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	clientuser.ClientUserRemove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := clientuser.ClientUserDetail(c, id)
	c.JSON(200, result.Success(c, vo))
}

func currentHandler(c *gin.Context) {
	vo := clientuser.Current(c)
	c.JSON(200, result.Success(c, vo))
}

func updateProfileHandler(c *gin.Context) {
	var param clientuser.UpdateProfileParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	clientuser.UpdateProfile(c, &param)
	c.JSON(200, result.Success(c, nil))
}

func updateAvatarHandler(c *gin.Context) {
	var param clientuser.UpdateAvatarParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	clientuser.UpdateAvatar(c, &param)
	c.JSON(200, result.Success(c, nil))
}

func updatePasswordHandler(c *gin.Context) {
	var param clientuser.UpdatePasswordParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	clientuser.UpdatePassword(c, &param)
	c.JSON(200, result.Success(c, nil))
}

package v1

import (
	"net/http"

	"hei-gin/sdk/pojo"
	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	tag "hei-gin/plugins/plugin-judge/tag"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/judge/tag/page",
		registry.Perm("judge:tag:page", "标签分页"),
		pageHandler,
	)
	r.POST("/api/v1/judge/tag/create",
		registry.Perm("judge:tag:create", "创建标签"),
		createHandler,
	)
	r.POST("/api/v1/judge/tag/modify",
		registry.Perm("judge:tag:modify", "编辑标签"),
		modifyHandler,
	)
	r.POST("/api/v1/judge/tag/remove",
		registry.Perm("judge:tag:remove", "删除标签"),
		removeHandler,
	)
	r.GET("/api/v1/judge/tag/list-all",
		registry.Perm("judge:tag:list-all", "标签列表"),
		listAllHandler,
	)
}

func pageHandler(c *gin.Context) {
	var param tag.TagPageParam
	if err := c.ShouldBind(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	c.JSON(http.StatusOK, tag.PageService(c, &param))
}

func createHandler(c *gin.Context) {
	var param tag.TagCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := tag.CreateService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var param tag.TagModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := tag.ModifyService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func removeHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := tag.RemoveService(c, tag.TagRemoveParam(param)); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func listAllHandler(c *gin.Context) {
	tags, err := tag.ListAllService(c)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, tags))
}

func init() {
	registry.RegisterRoute(RegisterRoutes)
}

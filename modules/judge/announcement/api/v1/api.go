package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/result"
	announcement "hei-gin/modules/judge/announcement"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/judge/contest/announcement")
	g.POST("/create", log.SysLog("创建公告"), createHandler)
	g.POST("/modify", log.SysLog("编辑公告"), modifyHandler)
	g.POST("/remove", log.SysLog("删除公告"), deleteHandler)
	g.GET("/list", listHandler)
}

func RegisterPublicRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/public/judge/contest/announcement")
	g.GET("/list", listHandler)
}

func createHandler(c *gin.Context) {
	var param struct {
		ContestID string `json:"contest_id"`
		Title     string `json:"title"`
		Content   string `json:"content"`
		Pinned    bool   `json:"pinned"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	announcement.Create(c, param.ContestID, param.Title, param.Content, param.Pinned, userID)
	c.JSON(200, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var param struct {
		ID      string `json:"id"`
		Title   string `json:"title"`
		Content string `json:"content"`
		Pinned  bool   `json:"pinned"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	announcement.Modify(c, param.ID, param.Title, param.Content, param.Pinned, userID)
	c.JSON(200, result.Success(c, nil))
}

func deleteHandler(c *gin.Context) {
	var param struct{ ID string `json:"id"` }
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	announcement.Remove(c, param.ID)
	c.JSON(200, result.Success(c, nil))
}

func listHandler(c *gin.Context) {
	contestID := c.Query("contest_id")
	data := announcement.List(c, contestID)
	c.JSON(200, data)
}

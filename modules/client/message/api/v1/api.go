package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	message "hei-gin/modules/client/message"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/c/message/page", pageHandler)

	r.GET("/api/v1/c/message/detail", detailHandler)

	r.GET("/api/v1/c/message/unread-count", unreadCountHandler)

	r.POST("/api/v1/c/message/send", sendHandler)

	r.POST("/api/v1/c/message/mark-read", markReadHandler)

	r.POST("/api/v1/c/message/mark-all-read", markAllReadHandler)

	r.POST("/api/v1/c/message/remove", removeHandler)
}

func pageHandler(c *gin.Context) {
	var param message.MessagePageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.Consumer.GetLoginID(c)
	data := message.Page(c, userID, &param)
	c.JSON(200, data)
}

func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := message.Detail(id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

func unreadCountHandler(c *gin.Context) {
	userID := auth.Consumer.GetLoginID(c)
	count := message.UnreadCount(userID)
	c.JSON(200, result.Success(c, message.UnreadCountVO{Count: count}))
}

func sendHandler(c *gin.Context) {
	var param message.MessageSendParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.Consumer.GetLoginID(c)
	message.Send(c, &param, userID)
	c.JSON(200, result.Success(c, nil))
}

func markReadHandler(c *gin.Context) {
	var param pojo.IdParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	message.MarkRead(param.ID)
	c.JSON(200, result.Success(c, nil))
}

func markAllReadHandler(c *gin.Context) {
	userID := auth.Consumer.GetLoginID(c)
	message.MarkAllRead(userID)
	c.JSON(200, result.Success(c, nil))
}

func removeHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	message.Remove(param.IDs)
	c.JSON(200, result.Success(c, nil))
}

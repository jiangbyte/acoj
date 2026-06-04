package v1

import (
	"strconv"

	"hei-gin/sdk/middleware"
	"hei-gin/sdk/auth"
	"hei-gin/sdk/enums"
	"hei-gin/sdk/pojo"
	"hei-gin/sdk/result"
	message "hei-gin/plugins/plugin-im/client_message"
	sys_message "hei-gin/plugins/plugin-im/sys_message"
	"hei-gin/plugins/plugin-im/group"

	"github.com/gin-gonic/gin"
	"hei-gin/sdk/registry"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/c/message/page", pageHandler)
	r.GET("/api/v1/c/message/detail", detailHandler)
	r.GET("/api/v1/c/message/unread-count", unreadCountHandler)
	r.POST("/api/v1/c/message/send", middleware.RateLimiter("c_send", 5, 20), sendHandler)
	r.POST("/api/v1/c/message/mark-read", markReadHandler)
	r.POST("/api/v1/c/message/mark-all-read", markAllReadHandler)
	r.POST("/api/v1/c/message/remove", removeHandler)

	// Conversation-based endpoints
	r.GET("/api/v1/c/message/conversations", conversationsHandler)
	r.GET("/api/v1/c/message/conversation/messages", conversationMessagesHandler)
	r.POST("/api/v1/c/message/conversation/read", conversationReadHandler)
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

func conversationsHandler(c *gin.Context) {
	userID := auth.Consumer.GetLoginID(c)
	cursor := c.Query("cursor")
	size := 20
	if s := c.Query("size"); s != "" {
		if n, err := strconv.Atoi(s); err == nil && n > 0 {
			size = n
		}
	}
	list, hasMore := sys_message.Conversations(userID, string(enums.LoginTypeConsumer), cursor, size)
	c.JSON(200, result.Success(c, gin.H{"records": list, "has_more": hasMore}))
}

func conversationMessagesHandler(c *gin.Context) {
	userID := auth.Consumer.GetLoginID(c)
	cid := c.Query("conversation_id")
	cursor := c.Query("cursor")
	size := 20
	if s := c.Query("size"); s != "" {
		if n, err := strconv.Atoi(s); err == nil && n > 0 {
			size = n
		}
	}

	var messages []sys_message.ConversationMessageVO
	var hasMore bool
	if len(cid) > 6 && cid[:6] == "group:" {
		gid := cid[6:]
		msgs, more := group.Messages(c.Request.Context(), gid, cursor, size)
		messages = make([]sys_message.ConversationMessageVO, len(msgs))
		for i, m := range msgs {
			senderID := m.SenderID
			messages[i] = sys_message.ConversationMessageVO{
				ID: m.ID, SenderID: &senderID, SenderType: m.SenderType,
				Content: m.Content, MsgType: m.MsgType, Extra: m.Extra,
				CreatedAt: m.CreatedAt,
			}
		}
		hasMore = more
	} else {
		messages, hasMore = sys_message.ConsumerConversationMessages(c.Request.Context(), userID, cid, cursor, size)
	}
	c.JSON(200, result.Success(c, gin.H{
		"records":  messages,
		"has_more": hasMore,
	}))
}

func conversationReadHandler(c *gin.Context) {
	var param struct {
		ConversationID string `json:"conversation_id"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.Consumer.GetLoginID(c)
	if len(param.ConversationID) > 6 && param.ConversationID[:6] == "group:" {
		group.MarkConversationRead(c.Request.Context(), param.ConversationID[6:], userID, string(enums.LoginTypeConsumer))
	} else {
		message.MarkConversationRead(userID, param.ConversationID)
	}
	c.JSON(200, result.Success(c, nil))
}
func init() {
	registry.RegisterRoute(RegisterRoutes)
}

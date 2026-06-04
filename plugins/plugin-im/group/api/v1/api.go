package v1

import (
	"context"
	"time"
	"strconv"

	"hei-gin/sdk/auth"
	"hei-gin/sdk/result"
	"hei-gin/plugins/plugin-im/group"

	"github.com/gin-gonic/gin"
)

func RegisterSysRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/sys/im/group")
	{
		g.POST("/create", createHandler)
		g.GET("/detail", detailHandler)
		g.POST("/dissolve", dissolveHandler)

		g.POST("/invite", inviteHandler)
		g.POST("/join", joinHandler)
		g.POST("/leave", leaveHandler)
		g.POST("/kick", kickHandler)
		g.PUT("/set-role", setRoleHandler)

		g.GET("/messages", messagesHandler)
		g.GET("/search", searchHandler)
		g.POST("/send", sendHandler)
		g.POST("/recall", recallHandler)
		g.POST("/mark-read", markReadHandler)
		g.POST("/mute", muteHandler)
		g.POST("/unmute", unmuteHandler)
		g.GET("/members", membersHandler)
	}
}

func RegisterClientRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/c/im/group")
	{
		g.POST("/create", createHandler)
		g.GET("/detail", detailHandler)
		g.POST("/join", joinHandler)
		g.POST("/leave", leaveHandler)
		g.GET("/messages", messagesHandler)
		g.GET("/search", searchHandler)
		g.POST("/send", sendHandler)
		g.POST("/recall", recallHandler)
		g.POST("/mark-read", markReadHandler)
		g.GET("/members", membersHandler)
	}
}

func getLoginID(c *gin.Context) (string, string) {
	uid := auth.GetLoginIDDefaultNull(c)
	if uid != "" {
		return uid, group.UserTypeBusiness
	}
	// Fallback: consumer login
	token := auth.Consumer.GetTokenValue(c)
	uid = auth.Consumer.GetLoginIDByToken(token)
	if uid != "" {
		return uid, group.UserTypeConsumer
	}
	return "", ""
}

func createHandler(c *gin.Context) {
	var p group.CreateParam
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	if userID == "" {
		c.JSON(200, result.Failure(c, "未登录", 401, nil))
		return
	}
	g := group.Create(context.Background(), userID, userType, &p)
	c.JSON(200, result.Success(c, g))
}

func updateHandler(c *gin.Context) {
	var p group.UpdateParam
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	group.Update(context.Background(), userID, userType, &p)
	c.JSON(200, result.Success(c, nil))
}

func dissolveHandler(c *gin.Context) {
	var p struct{ GroupID string `json:"group_id"` }
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, _ := getLoginID(c)
	group.Dissolve(context.Background(), userID, p.GroupID)
	c.JSON(200, result.Success(c, nil))
}

func detailHandler(c *gin.Context) {
	groupID := c.Query("group_id")
	vo := group.Detail(context.Background(), groupID)
	c.JSON(200, result.Success(c, vo))
}

func myGroupsHandler(c *gin.Context) {
	userID, userType := getLoginID(c)
	list := group.MyGroups(context.Background(), userID, userType)
	c.JSON(200, result.Success(c, list))
}

func inviteHandler(c *gin.Context) {
	var p group.InviteParam
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	group.Invite(context.Background(), userID, userType, &p)
	c.JSON(200, result.Success(c, nil))
}

func joinHandler(c *gin.Context) {
	var p struct{ GroupID string `json:"group_id"` }
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	group.Join(context.Background(), userID, userType, p.GroupID)
	c.JSON(200, result.Success(c, nil))
}

func leaveHandler(c *gin.Context) {
	var p struct{ GroupID string `json:"group_id"` }
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	group.Leave(context.Background(), userID, userType, p.GroupID)
	c.JSON(200, result.Success(c, nil))
}

func kickHandler(c *gin.Context) {
	var p group.KickParam
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	group.Kick(context.Background(), userID, userType, &p)
	c.JSON(200, result.Success(c, nil))
}

func setRoleHandler(c *gin.Context) {
	var p group.SetRoleParam
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, _ := getLoginID(c)
	group.SetRole(context.Background(), userID, &p)
	c.JSON(200, result.Success(c, nil))
}

func membersHandler(c *gin.Context) {
	groupID := c.Query("group_id")
	list := group.Members(context.Background(), groupID)
	c.JSON(200, result.Success(c, list))
}

func messagesHandler(c *gin.Context) {
	groupID := c.Query("group_id")
	cursor := c.Query("cursor")
	size := 20
	if s := c.Query("size"); s != "" {
		if n, err := strconv.Atoi(s); err == nil && n > 0 {
			size = n
		}
	}
	msgs, hasMore := group.Messages(context.Background(), groupID, cursor, size)
	c.JSON(200, result.Success(c, gin.H{"records": msgs, "has_more": hasMore}))
}

func searchHandler(c *gin.Context) {
	groupID := c.Query("group_id")
	keyword := c.Query("keyword")
	cursor := c.Query("cursor")
	size := 20
	if s := c.Query("size"); s != "" {
		if n, err := strconv.Atoi(s); err == nil && n > 0 {
			size = n
		}
	}
	msgs, hasMore := group.SearchMessages(context.Background(), groupID, keyword, cursor, size)
	c.JSON(200, result.Success(c, gin.H{"records": msgs, "has_more": hasMore}))
}

func sendHandler(c *gin.Context) {
	var p group.SendMessageParam
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	vo := group.SendMessage(context.Background(), userID, userType, &p)
	c.JSON(200, result.Success(c, vo))
}

func recallHandler(c *gin.Context) {
	var p struct {
		GroupID   string `json:"group_id"`
		MessageID string `json:"message_id"`
	}
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	group.RecallMessage(context.Background(), p.GroupID, p.MessageID, userID, userType)
	c.JSON(200, result.Success(c, nil))
}

func markReadHandler(c *gin.Context) {
	var p struct {
		GroupID   string `json:"group_id"`
		MessageID string `json:"message_id"`
	}
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	group.MarkRead(context.Background(), p.GroupID, userID, userType, p.MessageID)
	c.JSON(200, result.Success(c, nil))
}

func muteHandler(c *gin.Context) {
	var p struct {
		GroupID  string `json:"group_id"`
		UserID   string `json:"user_id"`
		UserType string `json:"user_type"`
		Duration int    `json:"duration"` // minutes
	}
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	duration := 60
	if p.Duration > 0 {
		duration = p.Duration
	}
	kp := group.KickParam{GroupID: p.GroupID, UserID: p.UserID, UserType: p.UserType}
	group.MuteMember(context.Background(), userID, userType, &kp, time.Duration(duration)*time.Minute)
	c.JSON(200, result.Success(c, nil))
}

func unmuteHandler(c *gin.Context) {
	var p struct {
		GroupID  string `json:"group_id"`
		UserID   string `json:"user_id"`
		UserType string `json:"user_type"`
	}
	if err := c.ShouldBindJSON(&p); err != nil {
		c.JSON(200, result.Failure(c, "参数错误", 400, nil))
		return
	}
	userID, userType := getLoginID(c)
	kp := group.KickParam{GroupID: p.GroupID, UserID: p.UserID, UserType: p.UserType}
	group.UnmuteMember(context.Background(), userID, userType, &kp)
	c.JSON(200, result.Success(c, nil))
}

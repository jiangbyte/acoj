package ws

import (
	"hei-gin/core/auth"
	"hei-gin/core/enums"
	"hei-gin/core/registry"
	ws "hei-gin/core/ws"

	"github.com/gin-gonic/gin"
)

func init() {
	registry.RegisterRoute(RegisterRoutes)
}

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/sys/ws", wsHandler)
}

func wsHandler(c *gin.Context) {
	token := c.Query("token")
	if token == "" {
		c.JSON(200, gin.H{"code": 401, "message": "缺少token", "success": false})
		c.Abort()
		return
	}

	userID := auth.GetLoginIDByToken(token)
	if userID == "" {
		c.JSON(200, gin.H{"code": 401, "message": "token无效或已过期", "success": false})
		c.Abort()
		return
	}

	ws.GlobalHub.HandleWebSocket(c.Writer, c.Request, userID, enums.LoginTypeBusiness)
}

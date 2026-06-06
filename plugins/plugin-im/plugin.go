package plugin_im

import (
	"hei-gin/api"
	"hei-gin/sdk/auth"
	"hei-gin/sdk/db"
	"hei-gin/sdk/enums"
	"hei-gin/sdk/module"
	"hei-gin/sdk/registry"
	ws "hei-gin/plugins/plugin-im/ws"

	"github.com/gin-gonic/gin"

	_ "hei-gin/plugins/plugin-im/message"
	_ "hei-gin/plugins/plugin-im/message/api/v1"
	_ "hei-gin/plugins/plugin-im/group"
	_ "hei-gin/plugins/plugin-im/group/api/v1"
	_ "hei-gin/plugins/plugin-im/friend"
	_ "hei-gin/plugins/plugin-im/friend/api/v1"
	_ "hei-gin/plugins/plugin-im/broadcast"
	_ "hei-gin/plugins/plugin-im/broadcast/api/v1"
)

type IMPlugin struct{}

func (p *IMPlugin) Info() api.PluginInfo {
	return api.PluginInfo{
		Name:        "plugin-im",
		Version:     "1.0.0",
		Description: "Instant messaging plugin (WebSocket + group chat + friend + broadcast)",
	}
}

func (p *IMPlugin) Name() string { return "plugin-im" }
func (p *IMPlugin) Init() error {
	ws.GlobalCrossHub = ws.NewCrossHub(ws.GlobalHub, db.Redis)
	return nil
}
func (p *IMPlugin) Start() error { return nil }
func (p *IMPlugin) Stop() error {
	if ws.GlobalCrossHub != nil {
		ws.GlobalCrossHub.Close()
	}
	return nil
}

func init() {
	module.Register(&IMPlugin{})

	registry.RegisterRoute(func(r *gin.Engine) {
		r.GET("/api/v1/sys/im/ws", sysWSHandler)
		r.GET("/api/v1/c/im/ws", clientWSHandler)
	})
}

func sysWSHandler(c *gin.Context) {
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

func clientWSHandler(c *gin.Context) {
	token := c.Query("token")
	if token == "" {
		c.JSON(200, gin.H{"code": 401, "message": "缺少token", "success": false})
		c.Abort()
		return
	}
	userID := auth.Consumer.GetLoginIDByToken(token)
	if userID == "" {
		c.JSON(200, gin.H{"code": 401, "message": "token无效或已过期", "success": false})
		c.Abort()
		return
	}
	ws.GlobalHub.HandleWebSocket(c.Writer, c.Request, userID, enums.LoginTypeConsumer)
}

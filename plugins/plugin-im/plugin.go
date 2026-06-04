package plugin_im

import (
	"hei-gin/api"
	"hei-gin/sdk/auth"
	"hei-gin/sdk/enums"
	"hei-gin/sdk/module"
	"hei-gin/sdk/registry"
	ws "hei-gin/sdk/ws"

	"github.com/gin-gonic/gin"

	_ "hei-gin/plugins/plugin-im/sys_message"
	_ "hei-gin/plugins/plugin-im/sys_message/api/v1"
	_ "hei-gin/plugins/plugin-im/client_message"
	_ "hei-gin/plugins/plugin-im/client_message/api/v1"
)

type IMPlugin struct{}

func (p *IMPlugin) Info() api.PluginInfo {
	return api.PluginInfo{
		Name:        "plugin-im",
		Version:     "1.0.0",
		Description: "Instant messaging plugin (WebSocket + messages + conversations)",
	}
}

func (p *IMPlugin) Name() string { return "plugin-im" }
func (p *IMPlugin) Init() error  { return nil }
func (p *IMPlugin) Start() error { return nil }
func (p *IMPlugin) Stop() error  { return nil }

func init() {
	module.Register(&IMPlugin{})

	registry.RegisterRoute(func(r *gin.Engine) {
		r.GET("/api/v1/sys/ws", sysWSHandler)
		r.GET("/api/v1/c/ws", clientWSHandler)
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

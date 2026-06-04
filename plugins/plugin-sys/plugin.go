package plugin_sys

import (
	"time"
	syslog "hei-gin/plugins/plugin-sys/log"
	stdlog "log"

	"hei-gin/api"
	"hei-gin/sdk/auth"
	"hei-gin/sdk/module"
	"hei-gin/sdk/db"
	"hei-gin/sdk/log"
	"hei-gin/sdk/utils"
	"hei-gin/plugins/plugin-sys/provider"
)

type SysPlugin struct {
	module.NoopModule
	permProvider *provider.PermissionProvider
	userProvider *provider.UserProvider
}

func (p *SysPlugin) Info() api.PluginInfo {
	return api.PluginInfo{
		Name:        "plugin-sys",
		Version:     "1.0.0",
		Description: "System management plugin (user, role, org, permission, etc.)",
	}
}

func (p *SysPlugin) Name() string { return "plugin-sys" }

func (p *SysPlugin) Init() error {
	p.permProvider = &provider.PermissionProvider{}
	p.userProvider = &provider.UserProvider{}

	auth.RegisterInterface(p.permProvider)

	log.LogPersistence = func(ctx interface{}, category, name, exeStatus, exeMessage, opIP, opAddress, opBrowser, opOS, opUser, traceID, signData, method, url, params string, opTime interface{}) {
		now := time.Now()
		entry := syslog.SysLog{
			ID:         utils.GenerateID(),
			Category:   &category,
			Name:       &name,
			ExeStatus:  &exeStatus,
			ExeMessage: &exeMessage,
			OpIP:       &opIP,
			OpAddress:  &opAddress,
			OpBrowser:  &opBrowser,
			OpOs:       &opOS,
			OpUser:     &opUser,
			TraceID:    &traceID,
			SignData:   &signData,
			ReqMethod:  &method,
			ReqURL:     &url,
			ParamJSON:  &params,
			CreatedAt:  &now,
			UpdatedAt:  &now,
		}
		if t, ok := opTime.(time.Time); ok {
			entry.OpTime = &t
		} else if s, ok := opTime.(string); ok && s != "" {
			if parsed, err := time.Parse("2006-01-02 15:04:05", s); err == nil {
				entry.OpTime = &parsed
			}
		}
		if err := db.DB.Create(&entry).Error; err != nil {
			stdlog.Printf("[SYSLOG] Failed to persist log: %v", err)
		}
	}
	stdlog.Println("[plugin-sys] initialized")
	return nil
}

func init() {
	module.Register(&SysPlugin{})
}

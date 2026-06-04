package plugin_sys

import (
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
		db.DB.Exec("INSERT INTO sys_log (id, category, name, exe_status, exe_message, op_ip, op_address, op_browser, op_os, op_user, trace_id, sign_data, req_method, req_url, param_json, op_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
			utils.GenerateID(), category, name, exeStatus, exeMessage, opIP, opAddress, opBrowser, opOS, opUser, traceID, signData, method, url, params, opTime)
	}
	stdlog.Println("[plugin-sys] initialized")
	return nil
}

func init() {
	module.Register(&SysPlugin{})
}

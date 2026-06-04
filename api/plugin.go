package api

type PluginInfo struct {
	Name         string
	Version      string
	Description  string
	Dependencies []string
}

type Plugin interface {
	Info() PluginInfo
	Init() error
	Start() error
	Stop() error
}

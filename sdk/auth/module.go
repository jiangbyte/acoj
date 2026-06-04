package auth

import (
	"log"

	"hei-gin/sdk/config"
	"hei-gin/sdk/module"
)

type authModule struct{ module.NoopModule }

func (m *authModule) Name() string { return "auth" }

func (m *authModule) Init() error {
	Init(config.C.Token.ExpireSeconds, config.C.Token.TokenName)
	Consumer.Init(config.C.Token.ExpireSeconds, config.C.Token.TokenName)
	log.Println("[auth] module initialized")
	return nil
}

func (m *authModule) Start() error {
	if err := RunPermissionScan(); err != nil {
		return err
	}
	return nil
}

func init() {
	module.Register(&authModule{})
}

package auth

import (
	"log"

	"hei-gin/config"
	"hei-gin/core/module"
)

type authModule struct{ module.NoopModule }

func (m *authModule) Name() string { return "auth" }

func (m *authModule) Init() error {
	Init(config.C.Token.ExpireSeconds, config.C.Token.TokenName)
	Consumer.Init(config.C.Token.ExpireSeconds, config.C.Token.TokenName)

	RegisterInterface(&HeiPermissionInterfaceImpl{})

	if err := RunPermissionScan(); err != nil {
		return err
	}

	log.Println("[auth] module initialized")
	return nil
}

func init() { module.Register(&authModule{}) }

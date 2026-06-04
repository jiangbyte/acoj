package utils

import (
	"hei-gin/sdk/config"
	"hei-gin/sdk/module"
)

type utilsModule struct{ module.NoopModule }

func (m *utilsModule) Name() string { return "utils" }

func (m *utilsModule) Init() error {
	Init(config.C.SM2.PrivateKey, config.C.SM2.PublicKey)
	return nil
}

func init() { module.Register(&utilsModule{}) }

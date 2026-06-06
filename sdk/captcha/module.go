package captcha

import (
	"hei-gin/sdk/db"
	"hei-gin/sdk/module"
)

type captchaModule struct{ module.NoopModule }

func (m *captchaModule) Name() string { return "captcha" }

func (m *captchaModule) Init() error {
	BCaptcha.Init(db.Redis)
	CCaptcha.Init(db.Redis)
	return nil
}

func init() { module.Register(&captchaModule{}) }

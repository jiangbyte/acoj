package scheduler

import (
	"hei-gin/core/module"
)

type schedulerModule struct{ module.NoopModule }

func (m *schedulerModule) Name() string { return "scheduler" }

func (m *schedulerModule) Start() error { Start(); return nil }

func (m *schedulerModule) Stop() error { Stop(); return nil }

func init() { module.Register(&schedulerModule{}) }

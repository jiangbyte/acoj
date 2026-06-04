package api

type ModuleManager interface {
	Register(p Plugin)
	InitAll() error
	StartAll()
	StopAll()
}

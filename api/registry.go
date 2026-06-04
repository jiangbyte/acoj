package api

type Registry interface {
	RegisterRoute(fn any)
	RegisterPerm(code, name string) any
	RegisterMiddleware(fn any)
}

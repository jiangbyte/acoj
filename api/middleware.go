package api

type MiddlewareAPI interface {
	AuthCheck() any
	Trace() any
	Recovery() any
	CORS() any
}

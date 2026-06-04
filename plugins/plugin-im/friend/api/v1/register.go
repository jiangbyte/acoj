package v1

import "hei-gin/sdk/registry"

func init() {
	registry.RegisterRoute(RegisterSysRoutes)
	registry.RegisterRoute(RegisterClientRoutes)
}

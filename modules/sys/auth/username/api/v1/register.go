package username_api

import "hei-gin/core/registry"

func init() {
	registry.RegisterRoute(RegisterRoutes)
}

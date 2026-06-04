package username_api

import "hei-gin/sdk/registry"

func init() {
	registry.RegisterRoute(RegisterRoutes)
}

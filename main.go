package main

import (
	"hei-gin/sdk/app"

	// Plugin route/permission self-registration
	_ "hei-gin/plugins/plugin-client"
	_ "hei-gin/plugins/plugin-im"
	_ "hei-gin/plugins/plugin-sys"
)

func main() {
	app.Run()
}

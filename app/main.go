package main

import (
	"hei-gin/sdk/app"

	// Select which plugins to include
	_ "hei-gin/plugins/plugin-sys"
	_ "hei-gin/plugins/plugin-client"
	_ "hei-gin/plugins/plugin-im"
)

func main() {
	app.Run()
}

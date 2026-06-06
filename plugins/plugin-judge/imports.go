package plugin_judge

import (
	// Model registrations (migrate.go)
	_ "hei-gin/plugins/plugin-judge/sandbox"
	_ "hei-gin/plugins/plugin-judge/judge"
	_ "hei-gin/plugins/plugin-judge/problem"
	_ "hei-gin/plugins/plugin-judge/testcase"
	_ "hei-gin/plugins/plugin-judge/submission"
	_ "hei-gin/plugins/plugin-judge/contest"
	_ "hei-gin/plugins/plugin-judge/problemset"
	_ "hei-gin/plugins/plugin-judge/tag"

	// Route registrations (api/v1/api.go)
	_ "hei-gin/plugins/plugin-judge/problem/api/v1"
	_ "hei-gin/plugins/plugin-judge/testcase/api/v1"
	_ "hei-gin/plugins/plugin-judge/judge/api/v1"
	_ "hei-gin/plugins/plugin-judge/submission/api/v1"
	_ "hei-gin/plugins/plugin-judge/contest/api/v1"
	_ "hei-gin/plugins/plugin-judge/problemset/api/v1"
	_ "hei-gin/plugins/plugin-judge/tag/api/v1"
)

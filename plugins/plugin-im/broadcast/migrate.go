package broadcast

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&Broadcast{})
	db.RegisterModel(&BroadcastRead{})
}

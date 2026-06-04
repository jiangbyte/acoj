package friend

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&FriendRequest{})
	db.RegisterModel(&Friendship{})
}

package model

import "hei-gin/sdk/db"

func init() {
	db.RegisterModel(&Message{})
	db.RegisterModel(&Conversation{})
	db.RegisterModel(&ConversationUnread{})
	db.RegisterModel(&ImFile{})
	db.RegisterModel(&Broadcast{})
	db.RegisterModel(&BroadcastRead{})
	db.RegisterModel(&FriendRequest{})
	db.RegisterModel(&Friendship{})
	db.RegisterModel(&FriendBlock{})
	db.RegisterModel(&Group{})
	db.RegisterModel(&GroupMember{})
	db.RegisterModel(&GroupJoinRequest{})
	db.RegisterModel(&GroupMessage{})
	db.RegisterModel(&GroupMessageRead{})
}

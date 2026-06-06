package message

import (
	imModel "hei-gin/plugins/plugin-im/model"
	"hei-gin/sdk/db"
)

func init() {
	db.RegisterModel(&imModel.Message{})
	db.RegisterModel(&imModel.Conversation{})
	db.RegisterModel(&imModel.ConversationUnread{})
}

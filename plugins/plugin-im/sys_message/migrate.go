package sys_message

import (
	"hei-gin/sdk/db"
	imModel "hei-gin/plugins/plugin-im/model"
)

func init() {
	db.RegisterModel(&imModel.SysMessage{})
	db.RegisterModel(&imModel.Message{})
	db.RegisterModel(&imModel.Conversation{})
	db.RegisterModel(&imModel.ConversationUnread{})
}

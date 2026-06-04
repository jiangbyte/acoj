package sys_message

import (
	imModel "hei-gin/plugins/plugin-im/model"
)

// SysMessage is an alias for the shared model.
type SysMessage = imModel.SysMessage

// GenerateConversationID delegates to the shared implementation.
var GenerateConversationID = imModel.GenerateConversationID

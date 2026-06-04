package client_message

import (
	imModel "hei-gin/plugins/plugin-im/model"
)

// ClientMessage is an alias for the shared model.
type ClientMessage = imModel.ClientMessage

// GenerateConversationID delegates to the shared implementation.
var GenerateConversationID = imModel.GenerateConversationID

package message

// MessageVO is the view object for a consumer site message.
type MessageVO struct {
	ConversationID string  `json:"conversation_id"`
	ID             string  `json:"id"`
	Title          string  `json:"title"`
	Content        string  `json:"content,omitempty"`
	SenderID       *string `json:"sender_id"`
	SenderType     string  `json:"sender_type"`
	ReceiverID     string  `json:"receiver_id"`
	ReceiverType   string  `json:"receiver_type"`
	MessageType    string  `json:"message_type"`
	Status         string  `json:"status"`
	ReadAt         *string `json:"read_at"`
	CreatedAt      string  `json:"created_at"`
	UpdatedAt      string  `json:"updated_at"`
}

type MessagePageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Status  string `json:"status" form:"status"`
}

type MessageSendParam struct {
	Title        string   `json:"title" binding:"required"`
	Content      string   `json:"content"`
	ReceiverIDs  []string `json:"receiver_ids"`
	ReceiverType string   `json:"receiver_type"`
}

type UnreadCountVO struct {
	Count int64 `json:"count"`
}

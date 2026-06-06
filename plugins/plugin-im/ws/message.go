package ws

// MessageType defines the type of WebSocket message.
type MessageType string

const (
	MsgHeartbeat    MessageType = "heartbeat"
	MsgOnlineCount  MessageType = "online_count"
	MsgNewMessage   MessageType = "new_message"
	MsgUnreadCount  MessageType = "unread_count"
	MsgPresence     MessageType = "presence"
	MsgDeliveryAck  MessageType = "delivery_ack"
	MsgTyping       MessageType = "typing"
	MsgConversation MessageType = "conversation"
)

// Message is the envelope sent over WebSocket.
type Message struct {
	Type    MessageType `json:"type"`
	Payload interface{} `json:"payload,omitempty"`
}

// HeartbeatPayload is sent by the client every 30s.
type HeartbeatPayload struct {
	Timestamp int64 `json:"timestamp"`
}

// OnlineCountPayload is broadcast to all connected clients periodically.
type OnlineCountPayload struct {
	Count int `json:"count"`
}

// NewMessagePayload is pushed to a specific user when a new message arrives.
type NewMessagePayload struct {
	MessageID      string `json:"message_id"`
	ConversationID string `json:"conversation_id,omitempty"`
	Title          string `json:"title"`
	Content        string `json:"content,omitempty"`
	SenderID       string `json:"sender_id,omitempty"`
	SenderType     string `json:"sender_type,omitempty"`
	MsgType        string `json:"msg_type,omitempty"`
	Extra          string `json:"extra,omitempty"`
	CreatedAt      string `json:"created_at"`
}

// UnreadCountPayload is pushed when unread count changes.
type UnreadCountPayload struct {
	Count int `json:"count"`
}

// PresencePayload is pushed when a user's online status changes.
type PresencePayload struct {
	UserID   string `json:"user_id"`
	UserType string `json:"user_type"`
	Online   bool   `json:"online"`
}

// DeliveryAckPayload acknowledges message delivery to the sender.
type DeliveryAckPayload struct {
	MessageID string `json:"message_id"`
	Status    string `json:"status"` // "delivered" | "read"
}

// TypingPayload indicates a user is typing in a conversation.
type TypingPayload struct {
	ConversationID string `json:"conversation_id"`
	UserID         string `json:"user_id"`
	UserType       string `json:"user_type"`
}

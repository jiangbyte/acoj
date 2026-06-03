package ws

// MessageType defines the type of WebSocket message.
type MessageType string

const (
	MsgHeartbeat   MessageType = "heartbeat"
	MsgOnlineCount MessageType = "online_count"
	MsgNewMessage  MessageType = "new_message"
	MsgUnreadCount MessageType = "unread_count"
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
	MessageID string `json:"message_id"`
	Title     string `json:"title"`
	Content   string `json:"content,omitempty"`
	CreatedAt string `json:"created_at"`
}

// UnreadCountPayload is pushed when unread count changes.
type UnreadCountPayload struct {
	Count int `json:"count"`
}

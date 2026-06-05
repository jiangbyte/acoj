package broadcast

// SendBroadcastParam 发送全站通知
type SendBroadcastParam struct {
	Title   string `json:"title" binding:"required"`
	Content string `json:"content"`
	Scope   string `json:"scope"` // ALL | BUSINESS | CONSUMER
}

// BroadcastVO 通知视图
type BroadcastVO struct {
	ID        string `json:"id"`
	Title     string `json:"title"`
	Content   string `json:"content,omitempty"`
	Scope     string `json:"scope"`
	SenderID  string `json:"sender_id"`
	Read      bool   `json:"read"`
	ReadAt    string `json:"read_at,omitempty"`
	CreatedAt string `json:"created_at"`
}

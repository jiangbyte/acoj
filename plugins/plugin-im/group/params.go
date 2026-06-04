package group

type CreateParam struct {
	Name       string   `json:"name" binding:"required"`
	Avatar     string   `json:"avatar"`
	MemberIDs  []string `json:"member_ids"`
	MemberType string   `json:"member_type"` // BUSINESS | CONSUMER
}

type UpdateParam struct {
	GroupID string  `json:"group_id" binding:"required"`
	Name    *string `json:"name"`
	Avatar  *string `json:"avatar"`
	Notice  *string `json:"notice"`
}

type InviteParam struct {
	GroupID  string   `json:"group_id" binding:"required"`
	UserIDs  []string `json:"user_ids" binding:"required"`
	UserType string   `json:"user_type" binding:"required"`
}

type KickParam struct {
	GroupID  string `json:"group_id" binding:"required"`
	UserID   string `json:"user_id" binding:"required"`
	UserType string `json:"user_type" binding:"required"`
}

type SetRoleParam struct {
	GroupID  string `json:"group_id" binding:"required"`
	UserID   string `json:"user_id" binding:"required"`
	UserType string `json:"user_type" binding:"required"`
	Role     string `json:"role" binding:"required"` // admin | owner
}

type SendMessageParam struct {
	GroupID string `json:"group_id" binding:"required"`
	Content string `json:"content"`
	MsgType string `json:"msg_type"`
	Extra   string `json:"extra"` // JSON TEXT
	ReplyTo string `json:"reply_to"`
}

type GroupVO struct {
	ID           string `json:"id"`
	Name         string `json:"name"`
	Avatar       string `json:"avatar"`
	OwnerID      string `json:"owner_id"`
	OwnerType    string `json:"owner_type"`
	GroupType    string `json:"group_type"`
	Notice       string `json:"notice"`
	MemberCount  int    `json:"member_count"`
	LastContent  string `json:"last_content"`
	LastTime     string `json:"last_time"`
	UnreadCount  int64  `json:"unread_count"`
}

type MemberVO struct {
	UserID    string `json:"user_id"`
	UserType  string `json:"user_type"`
	Role      string `json:"role"`
	Nickname  string `json:"nickname"`
	JoinedAt  string `json:"joined_at"`
	IsMuted   bool   `json:"is_muted"`
}

type MessageVO struct {
	ID         string  `json:"id"`
	SenderID   string  `json:"sender_id"`
	SenderType string  `json:"sender_type"`
	Content    string  `json:"content"`
	Extra      string  `json:"extra"`
	MsgType    string  `json:"msg_type"`
	ReplyTo    string  `json:"reply_to"`
	CreatedAt  string  `json:"created_at"`
}

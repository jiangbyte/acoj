package model

// Message type constants (stored in msg_type / message_type column)
const (
	MsgTypeText   = "TEXT"
	MsgTypeImage  = "IMAGE"
	MsgTypeFile   = "FILE"
	MsgTypeSystem = "SYSTEM"
)

// MsgExtraImage holds extra metadata for IMAGE messages.
// Stored as JSON text in the extra column.
type MsgExtraImage struct {
	Width     int    `json:"w,omitempty"`
	Height    int    `json:"h,omitempty"`
	Format    string `json:"format,omitempty"`
	Thumbnail string `json:"thumbnail,omitempty"`
}

// MsgExtraFile holds extra metadata for FILE messages.
type MsgExtraFile struct {
	Name string `json:"name"`
	Size int64  `json:"size"`
	MIME string `json:"mime"`
}

// MsgExtraSystem holds extra metadata for SYSTEM messages (group join/leave, etc.).
type MsgExtraSystem struct {
	Action     string `json:"action"`     // "join" | "leave" | "kick" | "dissolve" | "recalled"
	OperatorID string `json:"operator_id,omitempty"`
	UserID     string `json:"user_id,omitempty"`
	UserType   string `json:"user_type,omitempty"`
}

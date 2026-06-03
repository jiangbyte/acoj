package announcement

type AnnouncementVO struct {
	ID        string `json:"id"`
	ContestID string `json:"contest_id"`
	Title     string `json:"title"`
	Content   string `json:"content"`
	Pinned    bool   `json:"pinned"`
	CreatedAt string `json:"created_at,omitempty"`
}

type AnnouncementCreateParam struct {
	ContestID string `json:"contest_id"`
	Title     string `json:"title"`
	Content   string `json:"content"`
	Pinned    bool   `json:"pinned"`
}

type AnnouncementModifyParam struct {
	ID      string `json:"id"`
	Title   string `json:"title"`
	Content string `json:"content"`
	Pinned  bool   `json:"pinned"`
}

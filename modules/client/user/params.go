package clientuser

type ClientUserVO struct {
	ID          string `json:"id,omitempty"`
	Username    string `json:"username,omitempty"`
	Nickname    string `json:"nickname,omitempty"`
	Avatar      string `json:"avatar,omitempty"`
	Motto       string `json:"motto,omitempty"`
	Gender      string `json:"gender,omitempty"`
	Birthday    string `json:"birthday,omitempty"`
	Email       string `json:"email,omitempty"`
	Github      string `json:"github,omitempty"`
	Status      string `json:"status,omitempty"`
	LastLoginAt string `json:"last_login_at,omitempty"`
	LastLoginIP string `json:"last_login_ip,omitempty"`
	LoginCount  int    `json:"login_count"`
	CreatedAt   string `json:"created_at,omitempty"`
	CreatedBy   string `json:"created_by,omitempty"`
	UpdatedAt   string `json:"updated_at,omitempty"`
	UpdatedBy   string `json:"updated_by,omitempty"`
}

type ClientUserPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword,omitempty" form:"keyword"`
	Status  string `json:"status,omitempty" form:"status"`
}

type ClientUserCreateParam struct {
	Username string  `json:"username"`
	Password string  `json:"password"`
	Nickname *string `json:"nickname,omitempty"`
	Email    *string `json:"email,omitempty"`
	Avatar   *string `json:"avatar,omitempty"`
	Motto    *string `json:"motto,omitempty"`
	Gender   *string `json:"gender,omitempty"`
	Birthday string  `json:"birthday,omitempty"`
	Github   *string `json:"github,omitempty"`
	Status   string  `json:"status,omitempty"`
}

type ClientUserModifyParam struct {
	ID       string  `json:"id"`
	Username string  `json:"username,omitempty"`
	Nickname *string `json:"nickname,omitempty"`
	Email    *string `json:"email,omitempty"`
	Avatar   *string `json:"avatar,omitempty"`
	Motto    *string `json:"motto,omitempty"`
	Gender   *string `json:"gender,omitempty"`
	Birthday string  `json:"birthday,omitempty"`
	Github   *string `json:"github,omitempty"`
	Status   string  `json:"status,omitempty"`
}

type UpdateProfileParam struct {
	Nickname *string `json:"nickname,omitempty"`
	Motto    *string `json:"motto,omitempty"`
	Gender   *string `json:"gender,omitempty"`
	Birthday string  `json:"birthday,omitempty"`
	Email    *string `json:"email,omitempty"`
	Github   *string `json:"github,omitempty"`
}

type UpdateAvatarParam struct {
	Avatar string `json:"avatar"`
}

type UpdatePasswordParam struct {
	CurrentPassword string `json:"current_password"`
	NewPassword     string `json:"new_password"`
}

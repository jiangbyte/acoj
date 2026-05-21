package clientuser

type ClientUserVO struct {
	ID          string `json:"id"`
	Username    string `json:"username"`
	Nickname    string `json:"nickname"`
	Avatar      string `json:"avatar"`
	Motto       string `json:"motto"`
	Gender      string `json:"gender"`
	Birthday    string `json:"birthday"`
	Email       string `json:"email"`
	Github      string `json:"github"`
	Status      string `json:"status"`
	LastLoginAt string `json:"last_login_at"`
	LastLoginIP string `json:"last_login_ip"`
	LoginCount  int    `json:"login_count"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type ClientUserPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword" form:"keyword"`
	Status  string `json:"status" form:"status"`
}

type ClientUserCreateParam struct {
	Username string  `json:"username"`
	Password string  `json:"password"`
	Nickname *string `json:"nickname"`
	Email    *string `json:"email"`
	Avatar   *string `json:"avatar"`
	Motto    *string `json:"motto"`
	Gender   *string `json:"gender"`
	Birthday string  `json:"birthday"`
	Github   *string `json:"github"`
	Status   string  `json:"status"`
}

type ClientUserModifyParam struct {
	ID       string  `json:"id"`
	Username string  `json:"username"`
	Nickname *string `json:"nickname"`
	Email    *string `json:"email"`
	Avatar   *string `json:"avatar"`
	Motto    *string `json:"motto"`
	Gender   *string `json:"gender"`
	Birthday string  `json:"birthday"`
	Github   *string `json:"github"`
	Status   string  `json:"status"`
}

type UpdateProfileParam struct {
	Nickname *string `json:"nickname"`
	Motto    *string `json:"motto"`
	Gender   *string `json:"gender"`
	Birthday string  `json:"birthday"`
	Email    *string `json:"email"`
	Github   *string `json:"github"`
}

type UpdateAvatarParam struct {
	Avatar string `json:"avatar"`
}

type UpdatePasswordParam struct {
	CurrentPassword string `json:"current_password"`
	NewPassword     string `json:"new_password"`
}

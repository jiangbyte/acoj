package auth

// UsernameLoginParam represents the B-end username/password login request.
type UsernameLoginParam struct {
	Username    string `json:"username" form:"username" binding:"required"`
	Password    string `json:"password" form:"password" binding:"required"`
	CaptchaID   string `json:"captcha_id" form:"captcha_id"`
	CaptchaCode string `json:"captcha_code" form:"captcha_code"`
}

// UsernameLoginResult represents the login response.
type UsernameLoginResult struct {
	Token     string `json:"token"`
	TokenName string `json:"token_name"`
	UserID    string `json:"user_id"`
}

// UsernameRegisterParam represents the B-end registration request.
type UsernameRegisterParam struct {
	Username    string `json:"username" form:"username" binding:"required"`
	Password    string `json:"password" form:"password" binding:"required"`
	Nickname    string `json:"nickname" form:"nickname"`
	CaptchaID   string `json:"captcha_id" form:"captcha_id"`
	CaptchaCode string `json:"captcha_code" form:"captcha_code"`
}

// UsernameRegisterResult represents the registration response.
type UsernameRegisterResult struct {
	Token     string `json:"token"`
	TokenName string `json:"token_name"`
	UserID    string `json:"user_id"`
}

// UsernameLogoutResult represents the logout response.
type UsernameLogoutResult struct {
	Message string `json:"message"`
}

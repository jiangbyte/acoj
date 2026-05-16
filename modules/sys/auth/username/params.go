package username

// UsernameLoginParam represents the login request body.
type UsernameLoginParam struct {
	Username    string  `json:"username"`
	Password    string  `json:"password"`
	CaptchaCode string  `json:"captcha_code"`
	CaptchaID   string  `json:"captcha_id"`
	DeviceID    *string `json:"device_id"`
}

// UsernameLoginResult represents the login response body.
type UsernameLoginResult struct {
	Token string `json:"token,omitempty"`
}

// UsernameRegisterParam represents the register request body.
type UsernameRegisterParam struct {
	Username    string `json:"username"`
	Password    string `json:"password"`
	CaptchaCode string `json:"captcha_code"`
	CaptchaID   string `json:"captcha_id"`
}

// UsernameRegisterResult represents the register response body.
type UsernameRegisterResult struct {
	Message string `json:"message,omitempty"`
}

// UsernameLogoutResult represents the logout response body.
type UsernameLogoutResult struct {
	Message string `json:"message,omitempty"`
}

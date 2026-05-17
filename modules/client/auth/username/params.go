package username

// UsernameLoginParam represents the consumer login request body.
type UsernameLoginParam struct {
	Username    string  `json:"username"`
	Password    string  `json:"password"`
	CaptchaCode string  `json:"captcha_code"`
	CaptchaID   string  `json:"captcha_id"`
	DeviceID    *string `json:"device_id"`
}

// UsernameLoginResult represents the consumer login response body.
type UsernameLoginResult struct {
	Token string `json:"token,omitempty"`
}

// UsernameRegisterParam represents the consumer register request body.
type UsernameRegisterParam struct {
	Username    string `json:"username"`
	Password    string `json:"password"`
	CaptchaCode string `json:"captcha_code"`
	CaptchaID   string `json:"captcha_id"`
}

// UsernameRegisterResult represents the consumer register response body.
type UsernameRegisterResult struct {
	Message string `json:"message,omitempty"`
}

// UsernameLogoutResult represents the consumer logout response body.
type UsernameLogoutResult struct {
	Message string `json:"message,omitempty"`
}

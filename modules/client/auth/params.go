package clientauth

type LoginReq struct {
	Username    string `form:"username" json:"username" binding:"required"`
	Password    string `form:"password" json:"password" binding:"required"`
	CaptchaID   string `form:"captcha_id" json:"captcha_id" binding:"required"`
	CaptchaCode string `form:"captcha_code" json:"captcha_code" binding:"required"`
}

type CaptchaResp struct {
	CaptchaID     string `json:"captcha_id"`
	CaptchaBase64 string `json:"captcha_base64"`
}

type LoginResp struct {
	Token    string   `json:"token"`
	UserInfo UserInfo `json:"user_info"`
}

type UserInfo struct {
	ID       string `json:"id"`
	Username string `json:"username"`
	Nickname string `json:"nickname"`
	Avatar   string `json:"avatar"`
	Email    string `json:"email"`
	Phone    string `json:"phone"`
}

type RegisterReq struct {
	Account     string `form:"account" json:"account" binding:"required"`
	Password    string `form:"password" json:"password" binding:"required"`
	CaptchaID   string `form:"captcha_id" json:"captcha_id" binding:"required"`
	CaptchaCode string `form:"captcha_code" json:"captcha_code" binding:"required"`
}

type RegisterResp struct {
	Message string `json:"message"`
}

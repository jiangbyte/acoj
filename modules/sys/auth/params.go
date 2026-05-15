package sysauth

import "mime/multipart"

type LoginReq struct {
	Username    string `form:"username" json:"username" binding:"required"`
	Password    string `form:"password" json:"password" binding:"required"`
	CaptchaID   string `form:"captcha_id" json:"captcha_id" binding:"required"`
	CaptchaCode string `form:"captcha_code" json:"captcha_code" binding:"required"`
}

type RegisterReq struct {
	Username    string `form:"username" json:"username" binding:"required"`
	Password    string `form:"password" json:"password" binding:"required"`
	Nickname    string `form:"nickname" json:"nickname"`
	Email       string `form:"email" json:"email"`
	Phone       string `form:"phone" json:"phone"`
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

// Excel import params
type ImportReq struct {
	File *multipart.FileHeader `form:"file" binding:"required"`
}

type ExportParam struct {
	ExportType  string   `form:"export_type" json:"export_type"`
	Current     int      `form:"current" json:"current"`
	Size        int      `form:"size" json:"size"`
	SelectedIDs []string `form:"selected_ids" json:"selected_ids"`
}

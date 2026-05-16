package result

import (
	"errors"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
)

type Response struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
	Success bool        `json:"success"`
	TraceID string      `json:"trace_id"`
}

type PageData struct {
	Records interface{} `json:"records"`
	Total   int64       `json:"total"`
	Page    int         `json:"page"`
	Size    int         `json:"size"`
	Pages   int         `json:"pages"`
}

func Success(c *gin.Context, data interface{}) {
	c.JSON(200, Response{
		Code:    200,
		Message: "请求成功",
		Data:    data,
		Success: true,
		TraceID: GetTraceID(c),
	})
}

func Failure(c *gin.Context, message string, code int) {
	c.JSON(200, Response{
		Code:    code,
		Message: message,
		Data:    nil,
		Success: false,
		TraceID: GetTraceID(c),
	})
}

func Page(c *gin.Context, records interface{}, total int64, page, size int) {
	pages := 0
	if size > 0 {
		pages = (int(total) + size - 1) / size
	}
	Success(c, PageData{
		Records: records,
		Total:   total,
		Page:    page,
		Size:    size,
		Pages:   pages,
	})
}

func GetTraceID(c *gin.Context) string {
	if id, exists := c.Get("trace_id"); exists {
		return id.(string)
	}
	return ""
}

// ValidationError extracts a user-friendly Chinese error message from a binding/validation error
// and returns it as a Failure response with code 400.
func ValidationError(c *gin.Context, err error) {
	msg := extractValidationMessage(err)
	Failure(c, msg, 400)
}

func extractValidationMessage(err error) string {
	if err == nil {
		return "请求参数格式错误"
	}

	var verrs validator.ValidationErrors
	if errors.As(err, &verrs) {
		if len(verrs) > 0 {
			return translateValidationTag(verrs[0])
		}
	}

	msg := err.Error()
	if strings.Contains(msg, "EOF") {
		return "请求体不能为空"
	}
	if strings.Contains(msg, "ContentType") {
		return "请求内容类型错误"
	}
	return "请求参数格式错误"
}

var validationTagMessages = map[string]string{
	"required": "不能为空",
	"min":      "值太小",
	"max":      "值太大",
	"len":      "长度不正确",
	"email":    "邮箱格式不正确",
	"url":      "URL格式不正确",
	"oneof":    "值不在允许范围内",
	"gte":      "值不能小于",
	"lte":      "值不能大于",
}

func translateValidationTag(fe validator.FieldError) string {
	field := fieldToChinese(fe.Field())
	tag := fe.Tag()
	msg, ok := validationTagMessages[tag]
	if !ok {
		msg = "格式不正确"
	}
	return field + msg
}

func fieldToChinese(field string) string {
	mapping := map[string]string{
		"ID":       "ID",
		"IDs":      "ID列表",
		"id":       "ID",
		"ids":      "ID列表",
		"Code":     "编码",
		"code":     "编码",
		"Name":     "名称",
		"name":     "名称",
		"Username": "用户名",
		"username": "用户名",
		"Password": "密码",
		"password": "密码",
		"Nickname": "昵称",
		"nickname": "昵称",
		"Email":    "邮箱",
		"email":    "邮箱",
		"Phone":    "手机号",
		"phone":    "手机号",
		"Avatar":   "头像",
		"avatar":   "头像",
		"RoleID":   "角色ID",
		"role_id":  "角色ID",
		"UserID":   "用户ID",
		"user_id":  "用户ID",
		"Title":    "标题",
		"title":    "标题",
		"Content":  "内容",
		"content":  "内容",
		"Category": "分类",
		"Status":   "状态",
		"status":   "状态",
		"SortCode": "排序码",
	}
	if cn, ok := mapping[field]; ok {
		return cn
	}
	return field
}

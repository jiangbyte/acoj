package entity

import (
	"github.com/gogf/gf/v2/os/gtime"
	"github.com/gogf/gf/v2/util/gmeta"
)

type SysLog struct {
	gmeta.Meta `orm:"table:sys_log"`
	Id         string      `json:"id"          description:"主键"`
	Category   string      `json:"category"    description:"日志分类"`
	Name       string      `json:"name"        description:"日志名称"`
	ExeStatus  string      `json:"exeStatus"   description:"执行状态"`
	ExeMessage string      `json:"exeMessage"  description:"具体消息"`
	OpIp       string      `json:"opIp"        description:"操作ip"`
	OpAddress  string      `json:"opAddress"   description:"操作地址"`
	OpBrowser  string      `json:"opBrowser"   description:"操作浏览器"`
	OpOs       string      `json:"opOs"        description:"操作系统"`
	ClassName  string      `json:"className"   description:"类名称"`
	MethodName string      `json:"methodName"  description:"方法名称"`
	ReqMethod  string      `json:"reqMethod"   description:"请求方式"`
	ReqUrl     string      `json:"reqUrl"      description:"请求地址"`
	ParamJson  string      `json:"paramJson"   description:"请求参数"`
	ResultJson string      `json:"resultJson"  description:"返回结果"`
	OpTime     *gtime.Time `json:"opTime"      description:"操作时间"`
	TraceId    string      `json:"traceId"     description:"跟踪ID"`
	OpUser     string      `json:"opUser"      description:"操作人姓名"`
	SignData   string      `json:"signData"    description:"签名数据"`
	CreatedAt  *gtime.Time `json:"createdAt"   description:"创建时间"`
	CreatedBy  string      `json:"createdBy"   description:"创建用户"`
	UpdatedAt  *gtime.Time `json:"updatedAt"   description:"更新时间"`
	UpdatedBy  string      `json:"updatedBy"   description:"更新用户"`
}

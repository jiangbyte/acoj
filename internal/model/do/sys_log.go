package do

import "github.com/gogf/gf/v2/util/gmeta"

type SysLog struct {
	gmeta.Meta `orm:"table:sys_log"`
	Id         interface{} `json:"id"`
	Category   interface{} `json:"category"`
	Name       interface{} `json:"name"`
	ExeStatus  interface{} `json:"exeStatus"`
	ExeMessage interface{} `json:"exeMessage"`
	OpIp       interface{} `json:"opIp"`
	OpAddress  interface{} `json:"opAddress"`
	OpBrowser  interface{} `json:"opBrowser"`
	OpOs       interface{} `json:"opOs"`
	ClassName  interface{} `json:"className"`
	MethodName interface{} `json:"methodName"`
	ReqMethod  interface{} `json:"reqMethod"`
	ReqUrl     interface{} `json:"reqUrl"`
	ParamJson  interface{} `json:"paramJson"`
	ResultJson interface{} `json:"resultJson"`
	OpTime     interface{} `json:"opTime"`
	TraceId    interface{} `json:"traceId"`
	OpUser     interface{} `json:"opUser"`
	SignData   interface{} `json:"signData"`
	CreatedAt  interface{} `json:"createdAt"`
	CreatedBy  interface{} `json:"createdBy"`
	UpdatedAt  interface{} `json:"updatedAt"`
	UpdatedBy  interface{} `json:"updatedBy"`
}
